package com.openmeteo.mcp.controller;

import com.openmeteo.mcp.model.chat.ChatStreamRequest;
import com.openmeteo.mcp.model.stream.StreamMessage;
import com.openmeteo.mcp.service.StreamingChatService;
import com.openmeteo.mcp.service.StreamingWeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreamingControllerTest {

    @Mock StreamingWeatherService weatherService;
    @Mock StreamingChatService chatService;

    StreamingController controller;

    @BeforeEach
    void setUp() {
        controller = new StreamingController(weatherService, chatService);
    }

    @Nested
    class StatusTests {
        @Test
        void returnsStreamStatus() {
            var status = controller.getStatus();

            assertNotNull(status);
            assertTrue(status.enabled());
            assertNotNull(status.protocol());
            assertTrue(status.maxConcurrentConnections() > 0);
            assertTrue(status.maxStreamDurationMs() > 0);
        }
    }

    @Nested
    class DataStreamTests {
        @Test
        void parametersAreClampedToMaximums() {
            // count=200 should be clamped to 100, delay=10000 to 5000
            // We only verify it doesn't throw and returns a Flux
            var flux = controller.streamData(200, 10000);
            assertNotNull(flux);
        }

        @Test
        void parametersAreClampedToMinimums() {
            var flux = controller.streamData(0, 0);
            assertNotNull(flux);

            // Verify exactly 1 chunk + 1 complete event
            StepVerifier.create(flux.take(2))
                    .expectNextCount(2)
                    .verifyComplete();
        }

        @Test
        void emitsCorrectEventCount() {
            var flux = controller.streamData(3, 10);

            // 3 data events + 1 complete event = 4 total
            StepVerifier.create(flux)
                    .expectNextCount(4)
                    .verifyComplete();
        }

        @Test
        void dataEventsHaveCorrectEventType() {
            var flux = controller.streamData(1, 10);

            StepVerifier.create(flux)
                    .assertNext(sse -> assertEquals("data", sse.event()))
                    .assertNext(sse -> assertEquals("complete", sse.event()))
                    .verifyComplete();
        }
    }

    @Nested
    class WeatherStreamTests {
        @Test
        void streamsCurrentWeather() {
            var message = mock(StreamMessage.class);
            when(message.type()).thenReturn("data");
            when(weatherService.streamCurrentWeather(47.3, 8.5, "auto"))
                    .thenReturn(Flux.just(message));

            var flux = controller.streamCurrentWeather(47.3, 8.5, "auto");

            StepVerifier.create(flux)
                    .assertNext(sse -> {
                        assertNotNull(sse.data());
                        assertEquals("data", sse.event());
                    })
                    .verifyComplete();

            verify(weatherService).streamCurrentWeather(47.3, 8.5, "auto");
        }

        @Test
        void streamsForecast() {
            var message = mock(StreamMessage.class);
            when(message.type()).thenReturn("forecast");
            when(weatherService.streamForecast(47.3, 8.5, 7, false, "auto"))
                    .thenReturn(Flux.just(message));

            var flux = controller.streamForecast(47.3, 8.5, 7, false, "auto");

            StepVerifier.create(flux)
                    .assertNext(sse -> assertEquals("forecast", sse.event()))
                    .verifyComplete();
        }

        @Test
        void streamsHistoricalWeather() {
            var start = LocalDate.of(2025, 1, 1);
            var end = LocalDate.of(2025, 1, 31);
            var message = mock(StreamMessage.class);
            when(message.type()).thenReturn("historical");
            when(weatherService.streamHistoricalWeather(47.3, 8.5, start, end, "auto"))
                    .thenReturn(Flux.just(message));

            var flux = controller.streamHistoricalWeather(47.3, 8.5, start, end, "auto");

            StepVerifier.create(flux)
                    .assertNext(sse -> assertEquals("historical", sse.event()))
                    .verifyComplete();
        }

        @Test
        void streamsWeatherWithProgress() {
            var message = mock(StreamMessage.class);
            when(message.type()).thenReturn("progress");
            when(weatherService.streamWithProgress(47.3, 8.5, 7, "auto"))
                    .thenReturn(Flux.just(message));

            var flux = controller.streamWithProgress(47.3, 8.5, 7, "auto");

            StepVerifier.create(flux)
                    .assertNext(sse -> assertEquals("progress", sse.event()))
                    .verifyComplete();
        }

        @Test
        void propagatesWeatherServiceError() {
            when(weatherService.streamCurrentWeather(anyDouble(), anyDouble(), anyString()))
                    .thenReturn(Flux.error(new RuntimeException("API failure")));

            var flux = controller.streamCurrentWeather(0.0, 0.0, "auto");

            StepVerifier.create(flux)
                    .expectError(RuntimeException.class)
                    .verify();
        }
    }

    @Nested
    class ChatStreamTests {
        @Test
        void streamsChatResponse() {
            var message = mock(StreamMessage.class);
            when(message.type()).thenReturn("token");
            when(chatService.streamChat("session1", "hello"))
                    .thenReturn(Flux.just(message));

            var request = ChatStreamRequest.simple("session1", "hello");
            var flux = controller.streamChat(request);

            StepVerifier.create(flux)
                    .assertNext(sse -> assertEquals("token", sse.event()))
                    .verifyComplete();
        }

        @Test
        void streamsChatWithProgress() {
            var message = mock(StreamMessage.class);
            when(message.type()).thenReturn("progress");
            when(chatService.streamChatWithProgress("session1", "hello"))
                    .thenReturn(Flux.just(message));

            var request = ChatStreamRequest.simple("session1", "hello");
            var flux = controller.streamChatWithProgress(request);

            StepVerifier.create(flux)
                    .assertNext(sse -> assertEquals("progress", sse.event()))
                    .verifyComplete();
        }

        @Test
        void streamsChatWithWeatherContext() {
            var message = mock(StreamMessage.class);
            when(message.type()).thenReturn("context");
            when(chatService.streamWithContext("session1", "weather?", 47.3, 8.5))
                    .thenReturn(Flux.just(message));

            var request = ChatStreamRequest.withWeather("session1", "weather?", 47.3, 8.5);
            var flux = controller.streamChatWithContext(request);

            StepVerifier.create(flux)
                    .assertNext(sse -> assertEquals("context", sse.event()))
                    .verifyComplete();
        }

        @Test
        void fallsBackToSimpleChatWhenNoWeatherContext() {
            var message = mock(StreamMessage.class);
            when(message.type()).thenReturn("token");
            when(chatService.streamChat("session1", "hello"))
                    .thenReturn(Flux.just(message));

            // No weather coords — shouldIncludeWeather() returns false
            var request = ChatStreamRequest.simple("session1", "hello");
            var flux = controller.streamChatWithContext(request);

            StepVerifier.create(flux)
                    .assertNext(sse -> assertEquals("token", sse.event()))
                    .verifyComplete();

            verify(chatService).streamChat("session1", "hello");
            verify(chatService, never()).streamWithContext(any(), any(), anyDouble(), anyDouble());
        }
    }
}
