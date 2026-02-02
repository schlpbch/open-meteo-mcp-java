package com.openmeteo.mcp.chat.controller;

import com.openmeteo.mcp.chat.model.AiResponse;
import com.openmeteo.mcp.chat.model.ChatSession;
import com.openmeteo.mcp.chat.model.Message;
import com.openmeteo.mcp.chat.service.ChatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST API controller for chat operations.
 * 
 * @since 1.2.0
 */
@RestController
@RequestMapping("/api/chat")
@ConditionalOnProperty(name = "openmeteo.chat.enabled", havingValue = "true")
public class ChatController {
    
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatHandler chatHandler;
    
    public ChatController(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }
    
    /**
     * Send a message to a chat session
     * 
     * POST /api/chat/sessions/{sessionId}/messages
     * Body: { "message": "What's the weather in Zurich?" }
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public CompletableFuture<ResponseEntity<AiResponse>> sendMessage(
        @PathVariable String sessionId,
        @RequestBody ChatRequest request
    ) {
        log.info("Received chat message for session: {}", sessionId);
        
        return chatHandler.chat(sessionId, request.message())
            .thenApply(ResponseEntity::ok)
            .exceptionally(ex -> {
                log.error("Error processing chat message", ex);
                return ResponseEntity.internalServerError().build();
            });
    }
    
    /**
     * Get chat session
     * 
     * GET /api/chat/sessions/{sessionId}
     */
    @GetMapping("/sessions/{sessionId}")
    public CompletableFuture<ResponseEntity<ChatSession>> getSession(
        @PathVariable String sessionId
    ) {
        log.debug("Getting session: {}", sessionId);
        
        return chatHandler.getSession(sessionId)
            .thenApply(ResponseEntity::ok)
            .exceptionally(ex -> {
                log.error("Error getting session", ex);
                return ResponseEntity.notFound().build();
            });
    }
    
    /**
     * Get conversation history
     * 
     * GET /api/chat/sessions/{sessionId}/messages
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public CompletableFuture<ResponseEntity<List<Message>>> getHistory(
        @PathVariable String sessionId
    ) {
        log.debug("Getting history for session: {}", sessionId);
        
        return chatHandler.getHistory(sessionId)
            .thenApply(ResponseEntity::ok)
            .exceptionally(ex -> {
                log.error("Error getting history", ex);
                return ResponseEntity.internalServerError().build();
            });
    }
    
    /**
     * Delete a chat session
     * 
     * DELETE /api/chat/sessions/{sessionId}
     */
    @DeleteMapping("/sessions/{sessionId}")
    public CompletableFuture<ResponseEntity<Void>> deleteSession(
        @PathVariable String sessionId
    ) {
        log.info("Deleting session: {}", sessionId);
        
        return chatHandler.deleteSession(sessionId)
            .thenApply(v -> ResponseEntity.noContent().<Void>build())
            .exceptionally(ex -> {
                log.error("Error deleting session", ex);
                return ResponseEntity.internalServerError().build();
            });
    }
    
    /**
     * Health check endpoint
     * 
     * GET /api/chat/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "ChatHandler",
            "version", "1.2.0"
        ));
    }
    
    /**
     * Request record for chat messages
     */
    public record ChatRequest(String message) {
        public ChatRequest {
            if (message == null || message.isBlank()) {
                throw new IllegalArgumentException("Message cannot be null or blank");
            }
        }
    }
}
