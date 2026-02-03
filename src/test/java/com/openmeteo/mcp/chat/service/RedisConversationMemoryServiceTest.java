package com.openmeteo.mcp.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openmeteo.mcp.chat.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisConversationMemoryService.
 * Tests cover session CRUD, message CRUD, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class RedisConversationMemoryServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ListOperations<String, String> listOperations;

    private RedisConversationMemoryService service;

    @BeforeEach
    void setUp() {
        service = new RedisConversationMemoryService(redisTemplate, objectMapper);
    }

    @Nested
    class SessionTests {

        @Test
        void shouldSaveSession() throws Exception {
            // Arrange
            var session = ChatSession.create("test-session-1");
            String sessionJson = "{\"sessionId\":\"test-session-1\"}";

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(objectMapper.writeValueAsString(session)).thenReturn(sessionJson);

            // Act
            var result = service.saveSession(session).get();

            // Assert
            assertThat(result).isEqualTo(session);
            verify(valueOperations).set(eq("chat:session:test-session-1"), eq(sessionJson), any(Duration.class));
        }

        @Test
        void shouldThrow_whenSaveSessionFails() throws Exception {
            // Arrange
            var session = ChatSession.create("test-session-1");
            when(objectMapper.writeValueAsString(session))
                    .thenThrow(new JsonProcessingException("Serialization error") {});

            // Act & Assert
            assertThatThrownBy(() -> service.saveSession(session).get())
                    .hasCauseInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to save session");
        }

        @Test
        void shouldGetSession_whenExists() throws Exception {
            // Arrange
            var session = ChatSession.create("test-session-1");
            String sessionJson = "{\"sessionId\":\"test-session-1\"}";

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("chat:session:test-session-1")).thenReturn(sessionJson);
            when(objectMapper.readValue(sessionJson, ChatSession.class)).thenReturn(session);

            // Act
            var result = service.getSession("test-session-1").get();

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().sessionId()).isEqualTo("test-session-1");
        }

        @Test
        void shouldReturnEmpty_whenSessionNotFound() throws ExecutionException, InterruptedException {
            // Arrange
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("chat:session:not-found")).thenReturn(null);

            // Act
            var result = service.getSession("not-found").get();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenDeserializationFails() throws Exception {
            // Arrange
            String invalidJson = "invalid-json";
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("chat:session:bad-session")).thenReturn(invalidJson);
            when(objectMapper.readValue(invalidJson, ChatSession.class))
                    .thenThrow(new JsonProcessingException("Parse error") {});

            // Act
            var result = service.getSession("bad-session").get();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        void shouldDeleteSession() throws ExecutionException, InterruptedException {
            // Arrange
            String sessionId = "delete-session";
            when(redisTemplate.delete("chat:session:delete-session")).thenReturn(true);
            when(redisTemplate.delete("chat:messages:delete-session")).thenReturn(true);

            // Act
            service.deleteSession(sessionId).get();

            // Assert
            verify(redisTemplate).delete("chat:session:delete-session");
            verify(redisTemplate).delete("chat:messages:delete-session");
        }
    }

    @Nested
    class MessageTests {

        @Test
        void shouldSaveMessage() throws Exception {
            // Arrange
            var message = Message.user("session-1", "Hello");
            String messageJson = "{\"content\":\"Hello\"}";

            when(redisTemplate.opsForList()).thenReturn(listOperations);
            when(objectMapper.writeValueAsString(message)).thenReturn(messageJson);
            when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(true);

            // Act
            var result = service.saveMessage(message).get();

            // Assert
            assertThat(result.content()).isEqualTo("Hello");
            verify(listOperations).rightPush("chat:messages:session-1", messageJson);
            verify(redisTemplate).expire(eq("chat:messages:session-1"), any(Duration.class));
        }

        @Test
        void shouldThrow_whenSaveMessageFails() throws Exception {
            // Arrange
            var message = Message.user("session-1", "Hello");
            when(objectMapper.writeValueAsString(message))
                    .thenThrow(new JsonProcessingException("Serialization error") {});

            // Act & Assert
            assertThatThrownBy(() -> service.saveMessage(message).get())
                    .hasCauseInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to save message");
        }

        @Test
        void shouldGetMessages() throws Exception {
            // Arrange
            String msg1Json = "{\"content\":\"Hello\"}";
            String msg2Json = "{\"content\":\"Hi there\"}";
            var msg1 = Message.user("session-1", "Hello");
            var msg2 = Message.assistant("session-1", "Hi there");

            when(redisTemplate.opsForList()).thenReturn(listOperations);
            when(listOperations.range("chat:messages:session-1", 0, -1))
                    .thenReturn(List.of(msg1Json, msg2Json));
            when(objectMapper.readValue(msg1Json, Message.class)).thenReturn(msg1);
            when(objectMapper.readValue(msg2Json, Message.class)).thenReturn(msg2);

            // Act
            var result = service.getMessages("session-1").get();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).content()).isEqualTo("Hello");
            assertThat(result.get(1).content()).isEqualTo("Hi there");
        }

        @Test
        void shouldReturnEmptyList_whenNoMessages() throws ExecutionException, InterruptedException {
            // Arrange
            when(redisTemplate.opsForList()).thenReturn(listOperations);
            when(listOperations.range("chat:messages:empty-session", 0, -1))
                    .thenReturn(List.of());

            // Act
            var result = service.getMessages("empty-session").get();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        void shouldDeleteMessages() throws ExecutionException, InterruptedException {
            // Arrange
            when(redisTemplate.delete("chat:messages:session-1")).thenReturn(true);

            // Act
            service.deleteMessages("session-1").get();

            // Assert
            verify(redisTemplate).delete("chat:messages:session-1");
        }
    }

    @Nested
    class RecentMessagesTests {

        @Test
        void shouldGetRecentMessages_withLimit() throws Exception {
            // Arrange
            String msg1Json = "{\"content\":\"Message 1\"}";
            String msg2Json = "{\"content\":\"Message 2\"}";
            var msg1 = Message.user("session-1", "Message 1");
            var msg2 = Message.assistant("session-1", "Message 2");

            when(redisTemplate.opsForList()).thenReturn(listOperations);
            when(listOperations.size("chat:messages:session-1")).thenReturn(5L);
            // For limit=2, start should be 5-2=3
            when(listOperations.range("chat:messages:session-1", 3, -1))
                    .thenReturn(List.of(msg1Json, msg2Json));
            when(objectMapper.readValue(msg1Json, Message.class)).thenReturn(msg1);
            when(objectMapper.readValue(msg2Json, Message.class)).thenReturn(msg2);

            // Act
            var result = service.getRecentMessages("session-1", 2).get();

            // Assert
            assertThat(result).hasSize(2);
        }

        @Test
        void shouldReturnAllMessages_whenLessThanLimit() throws Exception {
            // Arrange
            String msgJson = "{\"content\":\"Only message\"}";
            var msg = Message.user("session-1", "Only message");

            when(redisTemplate.opsForList()).thenReturn(listOperations);
            when(listOperations.size("chat:messages:session-1")).thenReturn(1L);
            when(listOperations.range("chat:messages:session-1", 0, -1))
                    .thenReturn(List.of(msgJson));
            when(objectMapper.readValue(msgJson, Message.class)).thenReturn(msg);

            // Act
            var result = service.getRecentMessages("session-1", 10).get();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        void shouldReturnEmptyList_whenNoSize() throws ExecutionException, InterruptedException {
            // Arrange
            when(redisTemplate.opsForList()).thenReturn(listOperations);
            when(listOperations.size("chat:messages:empty-session")).thenReturn(0L);

            // Act
            var result = service.getRecentMessages("empty-session", 10).get();

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class CleanupTests {

        @Test
        void shouldReturnZero_asRedisHandlesExpiration() throws ExecutionException, InterruptedException {
            // Act
            var result = service.cleanupExpiredSessions(60).get();

            // Assert
            assertThat(result).isEqualTo(0);
            // Redis handles cleanup via TTL, so no delete calls expected
            verify(redisTemplate, never()).delete(anyString());
        }
    }
}
