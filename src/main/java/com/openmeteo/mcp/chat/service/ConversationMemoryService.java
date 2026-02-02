package com.openmeteo.mcp.chat.service;

import com.openmeteo.mcp.chat.model.ChatSession;
import com.openmeteo.mcp.chat.model.Message;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing conversation memory (sessions and messages).
 * Implementations can be in-memory or persistent (Redis, database, etc.).
 * 
 * @since 1.2.0
 */
public interface ConversationMemoryService {
    
    /**
     * Create or update a chat session
     */
    CompletableFuture<ChatSession> saveSession(ChatSession session);
    
    /**
     * Get a chat session by ID
     */
    CompletableFuture<Optional<ChatSession>> getSession(String sessionId);
    
    /**
     * Delete a chat session
     */
    CompletableFuture<Void> deleteSession(String sessionId);
    
    /**
     * Save a message to a session
     */
    CompletableFuture<Message> saveMessage(Message message);
    
    /**
     * Get all messages for a session
     */
    CompletableFuture<List<Message>> getMessages(String sessionId);
    
    /**
     * Get recent messages for a session (limited by count)
     */
    CompletableFuture<List<Message>> getRecentMessages(String sessionId, int limit);
    
    /**
     * Delete all messages for a session
     */
    CompletableFuture<Void> deleteMessages(String sessionId);
    
    /**
     * Clean up expired sessions
     */
    CompletableFuture<Integer> cleanupExpiredSessions(long ttlMinutes);
}
