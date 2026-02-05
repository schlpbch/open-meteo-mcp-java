package com.openmeteo.mcp.service;

import com.openmeteo.mcp.chat.service.ChatHandler;
import com.openmeteo.mcp.chat.service.ConversationMemoryService;
import com.openmeteo.mcp.model.stream.StreamMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StreamingChatService (Phase 5).
 * 
 * Tests chat streaming functionality:
 * - Token-by-token chat response delivery
 * - Progress tracking for long responses
 * - Weather context integration
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
class StreamingChatServiceTest {
    
    @Mock
    private ChatModel chatModel;
    
    @Mock
    private ConversationMemoryService memoryService;
    
    @Mock
    private ChatHandler chatHandler;
    
    private StreamingChatService streamingChatService;
    
    @BeforeEach
    void setUp() {
        streamingChatService = new StreamingChatService(chatModel, memoryService, chatHandler);
        when(memoryService.getSession(anyString())).thenReturn(CompletableFuture.completedFuture(Optional.empty()));
    }
    
    @Test
    void shouldStreamChatTokenByToken() {
        String sessionId = "session-123";
        String message = "What's the weather like?";
        
        var mockTokens = Flux.just(
            createChatResponse("The "),
            createChatResponse("weather ")
        );
        when(chatModel.stream(any(Prompt.class))).thenReturn(mockTokens);
        
        Flux<StreamMessage> result = streamingChatService.streamChat(sessionId, message);
        
        StepVerifier.create(result)
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("metadata");
                assertThat(msg.metadata().streamId()).contains(sessionId);
            })
            .expectNextCount(1)
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
            .verifyComplete();
    }
    
    @Test
    void shouldStreamChatWithProgressIndicators() {
        String sessionId = "session-456";
        String message = "Tell me about the weather";
        
        var mockTokens = Flux.just(
            createChatResponse("Weather "),
            createChatResponse("information")
        );
        when(chatModel.stream(any(Prompt.class))).thenReturn(mockTokens);
        
        Flux<StreamMessage> result = streamingChatService.streamChatWithProgress(sessionId, message);
        
        StepVerifier.create(result)
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("metadata"))
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("progress");
                assertThat(((StreamMessage.ProgressData) msg.data()).current()).isEqualTo(25);
            })
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("progress");
                assertThat(((StreamMessage.ProgressData) msg.data()).current()).isEqualTo(50);
            })
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("progress");
                assertThat(((StreamMessage.ProgressData) msg.data()).current()).isEqualTo(75);
            })
            .expectNextCount(1)
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
            .verifyComplete();
    }
    
    @Test
    void shouldStreamChatWithWeatherContext() {
        String sessionId = "session-789";
        String message = "How's the weather?";
        double latitude = 47.3769;
        double longitude = 8.5417;
        
        var mockTokens = Flux.just(
            createChatResponse("Currently "),
            createChatResponse("sunny")
        );
        when(chatModel.stream(any(Prompt.class))).thenReturn(mockTokens);
        
        Flux<StreamMessage> result = streamingChatService.streamWithContext(
            sessionId, message, latitude, longitude
        );
        
        StepVerifier.create(result)
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("metadata");
                assertThat(msg.metadata().streamId()).contains("context");
            })
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("progress"))
            .expectNextCount(1)
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
            .verifyComplete();
    }
    
    @Test
    void shouldHandleErrorInChatStream() {
        String sessionId = "session-error";
        String message = "Test error";
        
        when(chatModel.stream(any(Prompt.class)))
            .thenReturn(Flux.error(new RuntimeException("AI service unavailable")));
        
        Flux<StreamMessage> result = streamingChatService.streamChat(sessionId, message);
        
        StepVerifier.create(result)
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("metadata"))
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("error");
                var errorData = (StreamMessage.ErrorData) msg.data();
                assertThat(errorData.message()).contains("AI service unavailable");
                assertThat(errorData.code()).isEqualTo("CHAT_STREAM_ERROR");
            })
            .verifyError();
    }
    
    @Test
    void shouldSequenceProgressIndicatorsCorrectly() {
        String sessionId = "session-progress";
        String message = "Test sequence";
        
        var mockTokens = Flux.just(createChatResponse("Response"));
        when(chatModel.stream(any(Prompt.class))).thenReturn(mockTokens);
        
        Flux<StreamMessage> result = streamingChatService.streamChatWithProgress(sessionId, message);
        
        StepVerifier.create(result)
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("metadata"))
            .assertNext(msg -> {
                var progressData = (StreamMessage.ProgressData) msg.data();
                assertThat(progressData.current()).isEqualTo(25);
            })
            .assertNext(msg -> {
                var progressData = (StreamMessage.ProgressData) msg.data();
                assertThat(progressData.current()).isEqualTo(50);
            })
            .assertNext(msg -> {
                var progressData = (StreamMessage.ProgressData) msg.data();
                assertThat(progressData.current()).isEqualTo(75);
            })
            .expectNextCount(1)
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
            .verifyComplete();
    }
    
    @Test
    void shouldHandleContextStreamingWithoutLocation() {
        String sessionId = "session-no-location";
        String message = "Test no location";
        
        var mockTokens = Flux.just(createChatResponse("Response"));
        when(chatModel.stream(any(Prompt.class))).thenReturn(mockTokens);
        
        Flux<StreamMessage> result = streamingChatService.streamWithContext(
            sessionId, message, null, null
        );
        
        StepVerifier.create(result)
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("metadata"))
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("progress");
                assertThat(((StreamMessage.ProgressData) msg.data()).message()).contains("existing");
            })
            .expectNextCount(1)
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
            .verifyComplete();
    }
    
    @Test
    void shouldIncludeCorrectMetadataInStreams() {
        String sessionId = "session-metadata";
        String message = "Test metadata";
        var mockTokens = Flux.just(createChatResponse("Response"));
        when(chatModel.stream(any(Prompt.class))).thenReturn(mockTokens);
        
        // Test simple chat
        Flux<StreamMessage> simpleResult = streamingChatService.streamChat(sessionId, message);
        StepVerifier.create(simpleResult)
            .assertNext(msg -> {
                assertThat(msg.metadata().streamId()).contains("chat-stream");
                assertThat(msg.metadata().streamId()).contains(sessionId);
            })
            .expectNextCount(2)
            .verifyComplete();
    }
    
    @Test
    void shouldCompleteAllStreamTypesSuccessfully() {
        String sessionId = "session-complete";
        String message = "Test completion";
        var mockTokens = Flux.just(createChatResponse("Response"));
        when(chatModel.stream(any(Prompt.class))).thenReturn(mockTokens);
        
        StepVerifier.create(streamingChatService.streamChat(sessionId, message))
            .expectNextCount(3)
            .verifyComplete();
        
        StepVerifier.create(streamingChatService.streamChatWithProgress(sessionId, message))
            .expectNextCount(6)
            .verifyComplete();
        
        StepVerifier.create(streamingChatService.streamWithContext(sessionId, message, 47.0, 8.0))
            .expectNextCount(4)
            .verifyComplete();
    }
    
    @Test
    void shouldSendCompletionMessageAtEnd() {
        String sessionId = "session-completion";
        String message = "Test";
        var mockTokens = Flux.just(createChatResponse("Response"));
        when(chatModel.stream(any(Prompt.class))).thenReturn(mockTokens);
        
        Flux<StreamMessage> result = streamingChatService.streamChat(sessionId, message);
        
        StepVerifier.create(result)
            .expectNextCount(2)
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("complete");
                assertThat(msg.data()).isNull();
            })
            .verifyComplete();
    }
    
    private ChatResponse createChatResponse(String content) {
        var generation = mock(Generation.class);
        var output = mock(org.springframework.ai.chat.messages.AssistantMessage.class);
        when(output.getText()).thenReturn(content);
        when(generation.getOutput()).thenReturn(output);
        
        var chatResponse = mock(ChatResponse.class);
        when(chatResponse.getResult()).thenReturn(generation);
        
        return chatResponse;
    }
}
