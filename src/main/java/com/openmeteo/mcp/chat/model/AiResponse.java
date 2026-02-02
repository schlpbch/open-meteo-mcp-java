package com.openmeteo.mcp.chat.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Response from the AI assistant.
 * 
 * @param content Response content
 * @param functionCalls List of function calls made (if any)
 * @param metadata Response metadata (e.g., model used, token usage, latency)
 * @param timestamp When the response was generated
 * 
 * @since 2.0.0
 */
public record AiResponse(
    String content,
    List<FunctionCall> functionCalls,
    Map<String, Object> metadata,
    Instant timestamp
) {
    /**
     * Compact constructor with defaults
     */
    public AiResponse {
        if (content == null) {
            content = "";
        }
        if (functionCalls == null) {
            functionCalls = List.of();
        }
        if (metadata == null) {
            metadata = Map.of();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
    
    /**
     * Create a simple text response
     */
    public static AiResponse of(String content) {
        return new AiResponse(content, List.of(), Map.of(), Instant.now());
    }
    
    /**
     * Create a response with metadata
     */
    public static AiResponse of(String content, Map<String, Object> metadata) {
        return new AiResponse(content, List.of(), metadata, Instant.now());
    }
    
    /**
     * Represents a function call made by the AI
     */
    public record FunctionCall(
        String name,
        Map<String, Object> arguments,
        String result
    ) {
        public FunctionCall {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Function name cannot be null or blank");
            }
            if (arguments == null) {
                arguments = Map.of();
            }
        }
    }
}
