package com.openmeteo.mcp.chat.controller;

import com.openmeteo.mcp.chat.model.AiResponse;
import com.openmeteo.mcp.chat.service.ChatHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreamingChatControllerTest {

    @Mock ChatHandler chatHandler;

    StreamingChatController controller;

    @BeforeEach
    void setUp() {
        controller = new StreamingChatController(chatHandler);
    }

    @Nested
    class StreamMessageTests {
        @Test
        void emitsStartAndCompleteEvents() {
            var response = AiResponse.of("Hello world", Map.of("latencyMs", 100L));
            when(chatHandler.chat("session1", "Hi"))
                    .thenReturn(CompletableFuture.completedFuture(response));

            var request = new ChatController.ChatRequest("Hi");
            var flux = controller.streamMessage("session1", request);

            StepVerifier.create(flux)
                    .assertNext(sse -> assertEquals("start", sse.event()))
                    .thenConsumeWhile(sse -> "chunk".equals(sse.event()))
                    .assertNext(sse -> assertEquals("complete", sse.event()))
                    .verifyComplete();

            verify(chatHandler).chat("session1", "Hi");
        }

        @Test
        void emitsErrorEventOnFailure() {
            when(chatHandler.chat("session1", "Hi"))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("LLM error")));

            var request = new ChatController.ChatRequest("Hi");
            var flux = controller.streamMessage("session1", request);

            StepVerifier.create(flux)
                    .assertNext(sse -> assertEquals("start", sse.event()))
                    .assertNext(sse -> {
                        assertEquals("error", sse.event());
                        assertNotNull(sse.data());
                    })
                    .verifyComplete();
        }

        @Test
        void chunksLongResponses() {
            // Response longer than 50 chars will be chunked
            var longContent = "A".repeat(120);
            var response = AiResponse.of(longContent, Map.of("latencyMs", 50L));
            when(chatHandler.chat("session1", "Tell me a story"))
                    .thenReturn(CompletableFuture.completedFuture(response));

            var request = new ChatController.ChatRequest("Tell me a story");
            var flux = controller.streamMessage("session1", request);

            // start + 3 chunks (50+50+20) + complete = 5 events
            StepVerifier.create(flux)
                    .expectNextCount(5)
                    .verifyComplete();
        }
    }

    @Nested
    class HealthStreamTests {
        @Test
        void emitsThreePingEvents() {
            var flux = controller.healthStream();

            StepVerifier.create(flux)
                    .assertNext(sse -> {
                        assertEquals("ping", sse.event());
                        assertNotNull(sse.data());
                        assertTrue(sse.data().contains("UP"));
                    })
                    .assertNext(sse -> assertEquals("ping", sse.event()))
                    .assertNext(sse -> assertEquals("ping", sse.event()))
                    .verifyComplete();
        }
    }
}
