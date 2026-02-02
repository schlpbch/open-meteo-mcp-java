package com.openmeteo.mcp.chat.model;

import java.time.Instant;
import java.util.Map;

/**
 * Represents a single message in a conversation.
 * Immutable record following ADR-002.
 * 
 * @param id Unique message identifier
 * @param sessionId Session this message belongs to
 * @param type Type of message (USER, ASSISTANT, SYSTEM, FUNCTION)
 * @param content Message content
 * @param timestamp When the message was created
 * @param metadata Additional metadata (e.g., function call details, token usage)
 * 
 * @since 1.2.0
 */
public record Message(
    String id,
    String sessionId,
    MessageType type,
    String content,
    Instant timestamp,
    Map<String, Object> metadata
) {
    /**
     * Compact constructor with validation
     */
    public Message {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Message ID cannot be null or blank");
        }
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Message type cannot be null");
        }
        if (content == null) {
            content = "";
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (metadata == null) {
            metadata = Map.of();
        }
    }
    
    /**
     * Create a user message
     */
    public static Message user(String sessionId, String content) {
        return new Message(
            java.util.UUID.randomUUID().toString(),
            sessionId,
            MessageType.USER,
            content,
            Instant.now(),
            Map.of()
        );
    }
    
    /**
     * Create an assistant message
     */
    public static Message assistant(String sessionId, String content) {
        return new Message(
            java.util.UUID.randomUUID().toString(),
            sessionId,
            MessageType.ASSISTANT,
            content,
            Instant.now(),
            Map.of()
        );
    }
    
    /**
     * Create a system message
     */
    public static Message system(String sessionId, String content) {
        return new Message(
            java.util.UUID.randomUUID().toString(),
            sessionId,
            MessageType.SYSTEM,
            content,
            Instant.now(),
            Map.of()
        );
    }
}
