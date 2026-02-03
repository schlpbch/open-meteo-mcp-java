package com.openmeteo.mcp.chat.service;

import com.openmeteo.mcp.chat.exception.ChatException;
import com.openmeteo.mcp.chat.model.*;
import com.openmeteo.mcp.chat.observability.ChatMetrics;
import com.openmeteo.mcp.chat.rag.ContextEnrichmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChatHandler.
 * Tests cover chat processing, session management, and error handling.
 */
@ExtendWith(MockitoExtension.class)
class ChatHandlerTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ConversationMemoryService memoryService;

    @Mock
    private ContextEnrichmentService contextEnrichment;

    @Mock
    private ChatMetrics metrics;

    private ChatHandler chatHandler;

    @BeforeEach
    void setUp() {
        chatHandler = new ChatHandler(chatModel, memoryService, contextEnrichment, metrics);
    }

    @Nested
    class ChatTests {

        @Test
        void shouldProcessChat_withNewSession() throws ExecutionException, InterruptedException {
            // Arrange
            String sessionId = "new-session-123";
            String userMessage = "What's the weather in Zurich?";
            String aiResponse = "The weather in Zurich is sunny with 20°C.";

            setupNewSessionMocks(sessionId, aiResponse);
            when(contextEnrichment.extractLocation(userMessage))
                    .thenReturn(CompletableFuture.completedFuture("zurich"));

            // Act
            var result = chatHandler.chat(sessionId, userMessage).get();

            // Assert
            assertThat(result.content()).isEqualTo(aiResponse);
            assertThat(result.metadata()).containsKey("sessionId");
            assertThat(result.metadata().get("sessionId")).isEqualTo(sessionId);

            verify(metrics).recordRequest();
            verify(metrics).recordSuccess();
            verify(memoryService, atLeast(1)).saveSession(any(ChatSession.class));
            verify(memoryService, times(2)).saveMessage(any(Message.class)); // User + Assistant
        }

        @Test
        void shouldProcessChat_withExistingSession() throws ExecutionException, InterruptedException {
            // Arrange
            String sessionId = "existing-session-456";
            String userMessage = "What about tomorrow?";
            String aiResponse = "Tomorrow will be cloudy with 18°C.";

            var existingSession = ChatSession.create(sessionId);
            setupExistingSessionMocks(sessionId, existingSession, aiResponse);
            when(contextEnrichment.extractLocation(userMessage))
                    .thenReturn(CompletableFuture.completedFuture(null));

            // Act
            var result = chatHandler.chat(sessionId, userMessage).get();

            // Assert
            assertThat(result.content()).isEqualTo(aiResponse);
            verify(metrics, never()).incrementActiveSessions(); // Session already exists
        }

        @Test
        void shouldProcessChat_andUpdateLocationContext() throws ExecutionException, InterruptedException {
            // Arrange
            String sessionId = "location-session";
            String userMessage = "Weather in Bern please";
            String aiResponse = "Bern weather: 15°C, partly cloudy.";

            setupNewSessionMocks(sessionId, aiResponse);
            when(contextEnrichment.extractLocation(userMessage))
                    .thenReturn(CompletableFuture.completedFuture("bern"));

            // Act
            var result = chatHandler.chat(sessionId, userMessage).get();

            // Assert
            assertThat(result.content()).isEqualTo(aiResponse);
            // Verify session was updated with location
            verify(memoryService, atLeast(2)).saveSession(any(ChatSession.class));
        }

        @Test
        void shouldThrowChatException_whenChatModelFails() {
            // Arrange
            String sessionId = "error-session";
            String userMessage = "This will fail";

            setupNewSessionMocksWithError(sessionId);

            // Act & Assert
            assertThatThrownBy(() -> chatHandler.chat(sessionId, userMessage).get())
                    .hasCauseInstanceOf(ChatException.class)
                    .hasMessageContaining("Failed to process chat message");

            verify(metrics).recordRequest();
            verify(metrics).recordFailure();
        }
    }

    @Nested
    class GetSessionTests {

        @Test
        void shouldReturnSession_whenFound() throws ExecutionException, InterruptedException {
            // Arrange
            String sessionId = "found-session";
            var session = ChatSession.create(sessionId);
            when(memoryService.getSession(sessionId))
                    .thenReturn(CompletableFuture.completedFuture(Optional.of(session)));

            // Act
            var result = chatHandler.getSession(sessionId).get();

            // Assert
            assertThat(result.sessionId()).isEqualTo(sessionId);
        }

        @Test
        void shouldThrowChatException_whenNotFound() {
            // Arrange
            String sessionId = "not-found-session";
            when(memoryService.getSession(sessionId))
                    .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

            // Act & Assert
            assertThatThrownBy(() -> chatHandler.getSession(sessionId).get())
                    .hasCauseInstanceOf(ChatException.class)
                    .hasMessageContaining("Session not found");
        }
    }

    @Nested
    class GetHistoryTests {

        @Test
        void shouldReturnHistory() throws ExecutionException, InterruptedException {
            // Arrange
            String sessionId = "history-session";
            var messages = List.of(
                    Message.user(sessionId, "Hello"),
                    Message.assistant(sessionId, "Hi there!")
            );
            when(memoryService.getMessages(sessionId))
                    .thenReturn(CompletableFuture.completedFuture(messages));

            // Act
            var result = chatHandler.getHistory(sessionId).get();

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get(0).content()).isEqualTo("Hello");
            assertThat(result.get(1).content()).isEqualTo("Hi there!");
        }

        @Test
        void shouldReturnEmptyList_whenNoHistory() throws ExecutionException, InterruptedException {
            // Arrange
            String sessionId = "empty-history-session";
            when(memoryService.getMessages(sessionId))
                    .thenReturn(CompletableFuture.completedFuture(List.of()));

            // Act
            var result = chatHandler.getHistory(sessionId).get();

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class DeleteSessionTests {

        @Test
        void shouldDeleteSession() throws ExecutionException, InterruptedException {
            // Arrange
            String sessionId = "delete-session";
            when(memoryService.deleteSession(sessionId))
                    .thenReturn(CompletableFuture.completedFuture(null));

            // Act
            chatHandler.deleteSession(sessionId).get();

            // Assert
            verify(memoryService).deleteSession(sessionId);
        }
    }

    // ========== Helper Methods ==========

    private void setupNewSessionMocks(String sessionId, String aiResponse) {
        // Session doesn't exist
        when(memoryService.getSession(sessionId))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        // Save operations
        when(memoryService.saveSession(any(ChatSession.class)))
                .thenReturn(CompletableFuture.completedFuture(ChatSession.create(sessionId)));
        when(memoryService.saveMessage(any(Message.class)))
                .thenAnswer(invocation -> CompletableFuture.completedFuture(invocation.getArgument(0)));

        // Get recent messages
        when(memoryService.getRecentMessages(eq(sessionId), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        // Context enrichment
        when(contextEnrichment.enrichPrompt(anyString(), any(ConversationContext.class)))
                .thenReturn(CompletableFuture.completedFuture("Enriched: What's the weather?"));

        // Chat model response
        setupChatModelResponse(aiResponse);
    }

    private void setupExistingSessionMocks(String sessionId, ChatSession session, String aiResponse) {
        // Session exists
        when(memoryService.getSession(sessionId))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(session)));

        // Save operations
        when(memoryService.saveSession(any(ChatSession.class)))
                .thenReturn(CompletableFuture.completedFuture(session));
        when(memoryService.saveMessage(any(Message.class)))
                .thenAnswer(invocation -> CompletableFuture.completedFuture(invocation.getArgument(0)));

        // Get recent messages
        when(memoryService.getRecentMessages(eq(sessionId), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        // Context enrichment
        when(contextEnrichment.extractLocation(anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(contextEnrichment.enrichPrompt(anyString(), any(ConversationContext.class)))
                .thenReturn(CompletableFuture.completedFuture("Enriched prompt"));

        // Chat model response
        setupChatModelResponse(aiResponse);
    }

    private void setupNewSessionMocksWithError(String sessionId) {
        // Session doesn't exist
        when(memoryService.getSession(sessionId))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        // Save operations
        when(memoryService.saveSession(any(ChatSession.class)))
                .thenReturn(CompletableFuture.completedFuture(ChatSession.create(sessionId)));
        when(memoryService.saveMessage(any(Message.class)))
                .thenAnswer(invocation -> CompletableFuture.completedFuture(invocation.getArgument(0)));

        // Get recent messages
        when(memoryService.getRecentMessages(eq(sessionId), anyInt()))
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        // Context enrichment
        when(contextEnrichment.extractLocation(anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(contextEnrichment.enrichPrompt(anyString(), any(ConversationContext.class)))
                .thenReturn(CompletableFuture.completedFuture("Enriched prompt"));

        // Chat model throws error
        when(chatModel.call(any(Prompt.class)))
                .thenThrow(new RuntimeException("LLM Error"));
    }

    private void setupChatModelResponse(String responseText) {
        var assistantMessage = new AssistantMessage(responseText);
        var generation = new Generation(assistantMessage);
        var chatResponse = mock(ChatResponse.class);
        when(chatResponse.getResult()).thenReturn(generation);
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
    }
}
