package com.openmeteo.mcp.chat.service;

import com.openmeteo.mcp.chat.exception.ChatException;
import com.openmeteo.mcp.chat.model.*;
import com.openmeteo.mcp.chat.observability.ChatMetrics;
import com.openmeteo.mcp.chat.rag.ContextEnrichmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Core ChatHandler service that processes chat messages using Spring AI ChatModel.
 * Integrates conversation memory, context enrichment, and function calling with MCP tools.
 * 
 * @since 2.0.0
 */
@Service
@ConditionalOnProperty(name = "openmeteo.chat.enabled", havingValue = "true")
public class ChatHandler {
    
    private static final Logger log = LoggerFactory.getLogger(ChatHandler.class);
    
    private final ChatModel chatModel;
    private final ConversationMemoryService memoryService;
    private final ContextEnrichmentService contextEnrichment;
    private final ChatMetrics metrics;
    
    public ChatHandler(
        ChatModel chatModel,
        ConversationMemoryService memoryService,
        ContextEnrichmentService contextEnrichment,
        ChatMetrics metrics
    ) {
        this.chatModel = chatModel;
        this.memoryService = memoryService;
        this.contextEnrichment = contextEnrichment;
        this.metrics = metrics;
    }
    
    /**
     * Process a chat message and return AI response.
     * 
     * @param sessionId Session identifier
     * @param userMessage User's message
     * @return AI response
     */
    public CompletableFuture<AiResponse> chat(String sessionId, String userMessage) {
        log.info("Processing chat message for session: {}", sessionId);
        metrics.recordRequest();
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get or create session
                var session = memoryService.getSession(sessionId)
                    .join()
                    .orElseGet(() -> {
                        log.info("Creating new session: {}", sessionId);
                        metrics.incrementActiveSessions();
                        var newSession = ChatSession.create(sessionId);
                        memoryService.saveSession(newSession).join();
                        return newSession;
                    });
                
                // Save user message
                var userMsg = Message.user(sessionId, userMessage);
                memoryService.saveMessage(userMsg).join();
                
                // Extract location from message and update context if found
                var extractedLocation = contextEnrichment.extractLocation(userMessage).join();
                if (extractedLocation != null) {
                    var updatedContext = session.context().withLocation(extractedLocation);
                    session = session.withContext(updatedContext);
                    memoryService.saveSession(session).join();
                    log.debug("Updated session context with location: {}", extractedLocation);
                }
                
                // Get conversation history
                var history = memoryService.getRecentMessages(sessionId, 10).join();
                
                // Enrich prompt with conversation context
                var enrichedPrompt = contextEnrichment.enrichPrompt(userMessage, session.context()).join();
                log.debug("Enriched prompt: {} chars", enrichedPrompt.length());
                
                // Call LLM with enriched prompt
                var startTime = Instant.now();
                var prompt = new Prompt(enrichedPrompt);
                var chatResponse = chatModel.call(prompt);
                var response = chatResponse.getResult().getOutput().getText();
                var endTime = Instant.now();
                
                // Calculate latency
                var latencyMs = endTime.toEpochMilli() - startTime.toEpochMilli();
                metrics.recordResponseTime(latencyMs);
                
                // Save assistant response
                var assistantMsg = Message.assistant(sessionId, response);
                memoryService.saveMessage(assistantMsg).join();
                
                // Update session activity
                var updatedSession = session.touch();
                memoryService.saveSession(updatedSession).join();
                
                // Build AI response with metadata
                var metadata = new java.util.HashMap<String, Object>();
                metadata.put("latencyMs", latencyMs);
                metadata.put("messageCount", history.size() + 2);
                metadata.put("sessionId", sessionId);
                
                log.info("Chat response generated in {}ms for session: {}", latencyMs, sessionId);
                metrics.recordSuccess();
                return AiResponse.of(response, metadata);
                
            } catch (Exception e) {
                log.error("Error processing chat message for session: {}", sessionId, e);
                metrics.recordFailure();
                throw new ChatException("Failed to process chat message", e);
            }
        });
    }
    
    /**
     * Get chat session
     */
    public CompletableFuture<ChatSession> getSession(String sessionId) {
        return memoryService.getSession(sessionId)
            .thenApply(opt -> opt.orElseThrow(() -> 
                new ChatException("Session not found: " + sessionId)));
    }
    
    /**
     * Get conversation history
     */
    public CompletableFuture<java.util.List<Message>> getHistory(String sessionId) {
        return memoryService.getMessages(sessionId);
    }
    
    /**
     * Delete a session and its messages
     */
    public CompletableFuture<Void> deleteSession(String sessionId) {
        log.info("Deleting session: {}", sessionId);
        return memoryService.deleteSession(sessionId);
    }
    
    /**
     * Build conversation context from message history
     */
    private String buildConversationContext(java.util.List<Message> history) {
        if (history.isEmpty()) {
            return "";
        }
        
        var context = new StringBuilder();
        context.append("Previous conversation:\n");
        
        for (var msg : history) {
            var role = switch (msg.type()) {
                case USER -> "User";
                case ASSISTANT -> "Assistant";
                case SYSTEM -> "System";
                case FUNCTION -> "Function";
            };
            context.append(role).append(": ").append(msg.content()).append("\n");
        }
        
        return context.toString();
    }
}
