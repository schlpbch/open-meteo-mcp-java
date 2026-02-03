package com.openmeteo.mcp.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openmeteo.mcp.chat.model.ChatSession;
import com.openmeteo.mcp.chat.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Redis-backed implementation of ConversationMemoryService.
 * Suitable for production with multiple instances and persistence.
 * 
 * @since 2.0.0
 */
@Service
@ConditionalOnProperty(name = "openmeteo.chat.memory.type", havingValue = "redis")
public class RedisConversationMemoryService implements ConversationMemoryService {
    
    private static final Logger log = LoggerFactory.getLogger(RedisConversationMemoryService.class);
    private static final String SESSION_KEY_PREFIX = "chat:session:";
    private static final String MESSAGES_KEY_PREFIX = "chat:messages:";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public RedisConversationMemoryService(
        RedisTemplate<String, String> redisTemplate,
        ObjectMapper objectMapper
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public CompletableFuture<ChatSession> saveSession(ChatSession session) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Saving session to Redis: {}", session.sessionId());
                var key = SESSION_KEY_PREFIX + session.sessionId();
                var json = objectMapper.writeValueAsString(session);
                redisTemplate.opsForValue().set(key, json, Duration.ofHours(24));
                return session;
            } catch (JsonProcessingException e) {
                log.error("Error serializing session", e);
                throw new RuntimeException("Failed to save session", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<Optional<ChatSession>> getSession(String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Getting session from Redis: {}", sessionId);
                var key = SESSION_KEY_PREFIX + sessionId;
                var json = redisTemplate.opsForValue().get(key);
                if (json == null) {
                    return Optional.empty();
                }
                var session = objectMapper.readValue(json, ChatSession.class);
                return Optional.of(session);
            } catch (JsonProcessingException e) {
                log.error("Error deserializing session", e);
                return Optional.empty();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteSession(String sessionId) {
        return CompletableFuture.runAsync(() -> {
            log.debug("Deleting session from Redis: {}", sessionId);
            var sessionKey = SESSION_KEY_PREFIX + sessionId;
            var messagesKey = MESSAGES_KEY_PREFIX + sessionId;
            redisTemplate.delete(sessionKey);
            redisTemplate.delete(messagesKey);
        });
    }
    
    @Override
    public CompletableFuture<Message> saveMessage(Message message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Saving message to Redis for session: {}", message.sessionId());
                var key = MESSAGES_KEY_PREFIX + message.sessionId();
                var json = objectMapper.writeValueAsString(message);
                redisTemplate.opsForList().rightPush(key, json);
                // Set expiration on messages list
                redisTemplate.expire(key, Duration.ofHours(24));
                return message;
            } catch (JsonProcessingException e) {
                log.error("Error serializing message", e);
                throw new RuntimeException("Failed to save message", e);
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Message>> getMessages(String sessionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Getting all messages from Redis for session: {}", sessionId);
                var key = MESSAGES_KEY_PREFIX + sessionId;
                var jsonList = redisTemplate.opsForList().range(key, 0, -1);
                if (jsonList == null || jsonList.isEmpty()) {
                    return List.of();
                }
                return jsonList.stream()
                    .map(json -> {
                        try {
                            return objectMapper.readValue(json, Message.class);
                        } catch (JsonProcessingException e) {
                            log.error("Error deserializing message", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
            } catch (Exception e) {
                log.error("Error getting messages", e);
                return List.of();
            }
        });
    }
    
    @Override
    public CompletableFuture<List<Message>> getRecentMessages(String sessionId, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Getting {} recent messages from Redis for session: {}", limit, sessionId);
                var key = MESSAGES_KEY_PREFIX + sessionId;
                var size = redisTemplate.opsForList().size(key);
                if (size == null || size == 0) {
                    return List.of();
                }
                var start = Math.max(0, size - limit);
                var jsonList = redisTemplate.opsForList().range(key, start, -1);
                if (jsonList == null || jsonList.isEmpty()) {
                    return List.of();
                }
                return jsonList.stream()
                    .map(json -> {
                        try {
                            return objectMapper.readValue(json, Message.class);
                        } catch (JsonProcessingException e) {
                            log.error("Error deserializing message", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
            } catch (Exception e) {
                log.error("Error getting recent messages", e);
                return List.of();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> deleteMessages(String sessionId) {
        return CompletableFuture.runAsync(() -> {
            log.debug("Deleting all messages from Redis for session: {}", sessionId);
            var key = MESSAGES_KEY_PREFIX + sessionId;
            redisTemplate.delete(key);
        });
    }
    
    @Override
    public CompletableFuture<Integer> cleanupExpiredSessions(long ttlMinutes) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Cleanup for Redis is handled by TTL expiration");
            // Redis automatically handles expiration via TTL
            // This method is a no-op for Redis implementation
            return 0;
        });
    }
}
