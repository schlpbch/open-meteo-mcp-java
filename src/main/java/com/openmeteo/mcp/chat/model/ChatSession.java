package com.openmeteo.mcp.chat.model;

import java.time.Instant;

/**
 * Represents a chat session with conversation context.
 * 
 * @param sessionId Unique session identifier
 * @param userId User identifier (optional, for multi-user scenarios)
 * @param createdAt When the session was created
 * @param lastActivity Last activity timestamp
 * @param context Conversation context
 * 
 * @since 2.0.0
 */
public record ChatSession(
    String sessionId,
    String userId,
    Instant createdAt,
    Instant lastActivity,
    ConversationContext context
) {
    /**
     * Compact constructor with validation
     */
    public ChatSession {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or blank");
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (lastActivity == null) {
            lastActivity = createdAt;
        }
        if (context == null) {
            context = ConversationContext.EMPTY;
        }
    }
    
    /**
     * Create a new session
     */
    public static ChatSession create(String sessionId) {
        return new ChatSession(
            sessionId,
            null,
            Instant.now(),
            Instant.now(),
            ConversationContext.EMPTY
        );
    }
    
    /**
     * Create a new session with user ID
     */
    public static ChatSession create(String sessionId, String userId) {
        return new ChatSession(
            sessionId,
            userId,
            Instant.now(),
            Instant.now(),
            ConversationContext.EMPTY
        );
    }
    
    /**
     * Update last activity timestamp
     */
    public ChatSession touch() {
        return new ChatSession(sessionId, userId, createdAt, Instant.now(), context);
    }
    
    /**
     * Update context
     */
    public ChatSession withContext(ConversationContext newContext) {
        return new ChatSession(sessionId, userId, createdAt, Instant.now(), newContext);
    }
    
    /**
     * Check if session is expired (based on TTL)
     */
    public boolean isExpired(long ttlMinutes) {
        return Instant.now().isAfter(lastActivity.plusSeconds(ttlMinutes * 60));
    }
}
