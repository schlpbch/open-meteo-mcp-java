package com.openmeteo.mcp.chat.controller;

import com.openmeteo.mcp.chat.service.ChatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Controller for streaming chat responses using Server-Sent Events (SSE).
 * Provides real-time streaming of AI responses for better UX.
 * 
 * @since 1.2.0
 */
@RestController
@RequestMapping("/api/chat/stream")
@ConditionalOnProperty(name = "openmeteo.chat.enabled", havingValue = "true")
public class StreamingChatController {
    
    private static final Logger log = LoggerFactory.getLogger(StreamingChatController.class);
    
    private final ChatHandler chatHandler;
    
    public StreamingChatController(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }
    
    /**
     * Stream chat response using Server-Sent Events.
     * 
     * POST /api/chat/stream/sessions/{sessionId}/messages
     * Body: { "message": "What's the weather in Zurich?" }
     * 
     * Response: SSE stream with events:
     * - "start": Response started
     * - "chunk": Response chunk (can be multiple)
     * - "complete": Response complete with full text
     * - "error": Error occurred
     */
    @PostMapping(value = "/sessions/{sessionId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamMessage(
        @PathVariable String sessionId,
        @RequestBody ChatController.ChatRequest request
    ) {
        log.info("Starting streaming chat for session: {}", sessionId);
        
        return Flux.<ServerSentEvent<String>>create(sink -> {
            // Send start event
            sink.next(ServerSentEvent.<String>builder()
                .event("start")
                .data("{\"status\":\"processing\"}")
                .build());
            
            // Process chat message asynchronously
            chatHandler.chat(sessionId, request.message())
                .thenAccept(response -> {
                    // Simulate streaming by chunking the response
                    // In a real implementation, this would stream from the LLM
                    var content = response.content();
                    var chunkSize = 50;
                    
                    for (int i = 0; i < content.length(); i += chunkSize) {
                        var end = Math.min(i + chunkSize, content.length());
                        var chunk = content.substring(i, end);
                        
                        sink.next(ServerSentEvent.<String>builder()
                            .event("chunk")
                            .data(chunk)
                            .build());
                        
                        // Small delay to simulate streaming
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    
                    // Send complete event with metadata
                    var metadata = response.metadata();
                    sink.next(ServerSentEvent.<String>builder()
                        .event("complete")
                        .data(String.format("{\"content\":\"%s\",\"latencyMs\":%s}", 
                            content.replace("\"", "\\\""),
                            metadata.getOrDefault("latencyMs", 0)))
                        .build());
                    
                    sink.complete();
                })
                .exceptionally(error -> {
                    log.error("Error in streaming chat", error);
                    sink.next(ServerSentEvent.<String>builder()
                        .event("error")
                        .data("{\"error\":\"" + error.getMessage() + "\"}")
                        .build());
                    sink.complete();
                    return null;
                });
        })
        .delayElements(Duration.ofMillis(10)); // Smooth streaming
    }
    
    /**
     * Health check for streaming endpoint
     */
    @GetMapping("/health")
    public Flux<ServerSentEvent<String>> healthStream() {
        return Flux.interval(Duration.ofSeconds(1))
            .take(3)
            .map(seq -> ServerSentEvent.<String>builder()
                .event("ping")
                .data("{\"status\":\"UP\",\"sequence\":" + seq + "}")
                .build());
    }
}
