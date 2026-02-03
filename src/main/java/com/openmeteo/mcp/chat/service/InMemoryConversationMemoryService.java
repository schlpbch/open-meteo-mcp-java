package com.openmeteo.mcp.chat.service;

import com.openmeteo.mcp.chat.model.ChatSession;
import com.openmeteo.mcp.chat.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of ConversationMemoryService.
 * Used for development and testing. Not suitable for production with multiple instances.
 * 
 * @since 2.0.0
 */
@Service
@ConditionalOnProperty(name = "openmeteo.chat.memory.type", havingValue = "inmemory", matchIfMissing = true)
public class InMemoryConversationMemoryService implements ConversationMemoryService {
    
    private static final Logger log = LoggerFactory.getLogger(InMemoryConversationMemoryService.class);
    
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, List<Message>> messages = new ConcurrentHashMap<>();
    
    @Override
    public CompletableFuture<ChatSession> saveSession(ChatSession session) {
        log.debug("Saving session: {}", session.sessionId());
        sessions.put(session.sessionId(), session);
        return CompletableFuture.completedFuture(session);
    }
    
    @Override
    public CompletableFuture<Optional<ChatSession>> getSession(String sessionId) {
        log.debug("Getting session: {}", sessionId);
        return CompletableFuture.completedFuture(Optional.ofNullable(sessions.get(sessionId)));
    }
    
    @Override
    public CompletableFuture<Void> deleteSession(String sessionId) {
        log.debug("Deleting session: {}", sessionId);
        sessions.remove(sessionId);
        messages.remove(sessionId);
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public CompletableFuture<Message> saveMessage(Message message) {
        log.debug("Saving message for session: {}", message.sessionId());
        messages.computeIfAbsent(message.sessionId(), k -> new ArrayList<>()).add(message);
        return CompletableFuture.completedFuture(message);
    }
    
    @Override
    public CompletableFuture<List<Message>> getMessages(String sessionId) {
        log.debug("Getting all messages for session: {}", sessionId);
        return CompletableFuture.completedFuture(
            new ArrayList<>(messages.getOrDefault(sessionId, List.of()))
        );
    }
    
    @Override
    public CompletableFuture<List<Message>> getRecentMessages(String sessionId, int limit) {
        log.debug("Getting {} recent messages for session: {}", limit, sessionId);
        var allMessages = messages.getOrDefault(sessionId, List.of());
        var startIndex = Math.max(0, allMessages.size() - limit);
        return CompletableFuture.completedFuture(
            new ArrayList<>(allMessages.subList(startIndex, allMessages.size()))
        );
    }
    
    @Override
    public CompletableFuture<Void> deleteMessages(String sessionId) {
        log.debug("Deleting all messages for session: {}", sessionId);
        messages.remove(sessionId);
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public CompletableFuture<Integer> cleanupExpiredSessions(long ttlMinutes) {
        log.debug("Cleaning up expired sessions (TTL: {} minutes)", ttlMinutes);
        var expiredSessions = sessions.values().stream()
            .filter(session -> session.isExpired(ttlMinutes))
            .map(ChatSession::sessionId)
            .toList();
        
        expiredSessions.forEach(sessionId -> {
            sessions.remove(sessionId);
            messages.remove(sessionId);
        });
        
        log.info("Cleaned up {} expired sessions", expiredSessions.size());
        return CompletableFuture.completedFuture(expiredSessions.size());
    }
    
    /**
     * Get statistics for monitoring
     */
    public Map<String, Object> getStats() {
        return Map.of(
            "totalSessions", sessions.size(),
            "totalMessages", messages.values().stream().mapToInt(List::size).sum()
        );
    }
}
