package com.openmeteo.mcp.controller;

import com.openmeteo.mcp.model.chat.ChatStreamRequest;
import com.openmeteo.mcp.model.stream.StreamChunk;
import com.openmeteo.mcp.model.stream.StreamMessage;
import com.openmeteo.mcp.model.stream.StreamMetadata;
import com.openmeteo.mcp.service.StreamingChatService;
import com.openmeteo.mcp.service.StreamingWeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Streaming Controller for MCP Streamable HTTP Protocol.
 * 
 * Implements Server-Sent Events (SSE) endpoints for real-time streaming
 * as specified in ADR-020.
 * 
 * Endpoints:
 * - /stream/test - Test SSE endpoint with heartbeat
 * - /stream/data - Generic data streaming endpoint
 * - /stream/weather/* - Weather data streaming (Phase 4)
 * - /stream/chat/* - Chat response streaming (Phase 5)
 * 
 * Security: All endpoints protected by Spring Security
 * Authentication: JWT tokens or API keys required
 */
@RestController
@RequestMapping("/stream")
public class StreamingController {

    private static final Logger log = LoggerFactory.getLogger(StreamingController.class);

    private final StreamingWeatherService streamingWeatherService;
    private final StreamingChatService streamingChatService;

    public StreamingController(
        StreamingWeatherService streamingWeatherService,
        StreamingChatService streamingChatService
    ) {
        this.streamingWeatherService = streamingWeatherService;
        this.streamingChatService = streamingChatService;
    }

    /**
     * Test SSE endpoint with heartbeat.
     * 
     * Sends periodic heartbeat messages to test SSE connectivity and
     * demonstrate basic streaming functionality.
     * 
     * @return Flux of ServerSentEvent with heartbeat messages
     */
    @GetMapping(value = "/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Flux<ServerSentEvent<StreamMessage>> testStream() {
        String streamId = UUID.randomUUID().toString();
        log.info("Starting test stream: {}", streamId);

        return Flux.interval(Duration.ofSeconds(1))
                .take(10)
                .map(i -> {
                    StreamMetadata metadata = StreamMetadata.withProgress(streamId, i.intValue(), 10);
                    StreamMessage message = StreamMessage.data("Heartbeat " + i, metadata);
                    return ServerSentEvent.<StreamMessage>builder()
                            .id(String.valueOf(i))
                            .event("message")
                            .data(message)
                            .build();
                })
                .concatWith(Flux.just(
                        ServerSentEvent.<StreamMessage>builder()
                                .id("complete")
                                .event("complete")
                                .data(StreamMessage.complete(StreamMetadata.of(streamId)))
                                .build()
                ))
                .doOnComplete(() -> log.info("Test stream completed: {}", streamId))
                .doOnError(error -> log.error("Test stream error: {}", streamId, error))
                .doOnCancel(() -> log.info("Test stream cancelled: {}", streamId));
    }

    /**
     * Generic data streaming endpoint.
     * 
     * Demonstrates streaming of sequential data chunks with progress tracking.
     * Used as foundation for weather and chat streaming.
     * 
     * @param count Number of chunks to stream (default: 5, max: 100)
     * @param delay Delay between chunks in ms (default: 100, max: 5000)
     * @return Flux of ServerSentEvent with data chunks
     */
    @GetMapping(value = "/data", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('MCP_CLIENT', 'ADMIN')")
    public Flux<ServerSentEvent<StreamMessage>> streamData(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "100") long delay) {
        
        // Validate parameters
        int chunkCount = Math.min(Math.max(count, 1), 100);
        long chunkDelay = Math.min(Math.max(delay, 10), 5000);
        
        String streamId = UUID.randomUUID().toString();
        log.info("Starting data stream: {} (chunks={}, delay={}ms)", streamId, chunkCount, chunkDelay);

        return Flux.range(0, chunkCount)
                .delayElements(Duration.ofMillis(chunkDelay))
                .map(i -> {
                    StreamChunk chunk = i == chunkCount - 1 
                            ? StreamChunk.last(i, "Chunk " + i)
                            : StreamChunk.of(i, "Chunk " + i);
                    
                    StreamMetadata metadata = StreamMetadata.withProgress(streamId, i + 1, chunkCount);
                    StreamMessage message = StreamMessage.data(chunk, metadata);
                    
                    return ServerSentEvent.<StreamMessage>builder()
                            .id(String.valueOf(i))
                            .event("data")
                            .data(message)
                            .build();
                })
                .concatWith(Flux.just(
                        ServerSentEvent.<StreamMessage>builder()
                                .id("complete")
                                .event("complete")
                                .data(StreamMessage.complete(StreamMetadata.of(streamId)))
                                .build()
                ))
                .doOnComplete(() -> log.info("Data stream completed: {}", streamId))
                .doOnError(error -> log.error("Data stream error: {}", streamId, error))
                .doOnCancel(() -> log.info("Data stream cancelled: {}", streamId));
    }

    /**
     * Stream status endpoint.
     * 
     * Returns current streaming configuration and limits.
     * 
     * @return Streaming status information
     */
    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public StreamStatus getStatus() {
        return new StreamStatus(
                true,
                "MCP Streamable HTTP v1.0",
                100,
                10000L
        );
    }

    /**
     * Stream current weather conditions.
     * 
     * Returns current weather as SSE stream with single data chunk.
     * 
     * @param latitude Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @param timezone Timezone (default: auto)
     * @return Flux of ServerSentEvent with current weather
     */
    @GetMapping(value = "/weather/current", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('MCP_CLIENT', 'ADMIN')")
    public Flux<ServerSentEvent<StreamMessage>> streamCurrentWeather(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "auto") String timezone) {
        
        log.info("Streaming current weather: lat={}, lon={}", latitude, longitude);

        return streamingWeatherService.streamCurrentWeather(latitude, longitude, timezone)
                .map(msg -> ServerSentEvent.<StreamMessage>builder()
                        .id(UUID.randomUUID().toString())
                        .event(msg.type())
                        .data(msg)
                        .build())
                .doOnComplete(() -> log.info("Current weather stream completed"))
                .doOnError(error -> log.error("Current weather stream error", error));
    }

    /**
     * Stream weather forecast with chunking.
     * 
     * Returns forecast data as SSE stream, chunked for large datasets.
     * 
     * @param latitude Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @param forecastDays Number of forecast days (1-16, default: 7)
     * @param includeHourly Include hourly data (default: false)
     * @param timezone Timezone (default: auto)
     * @return Flux of ServerSentEvent with forecast chunks
     */
    @GetMapping(value = "/weather/forecast", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('MCP_CLIENT', 'ADMIN')")
    public Flux<ServerSentEvent<StreamMessage>> streamForecast(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "7") int forecastDays,
            @RequestParam(defaultValue = "false") boolean includeHourly,
            @RequestParam(defaultValue = "auto") String timezone) {
        
        log.info("Streaming forecast: lat={}, lon={}, days={}, hourly={}", 
                latitude, longitude, forecastDays, includeHourly);

        return streamingWeatherService.streamForecast(latitude, longitude, forecastDays, includeHourly, timezone)
                .map(msg -> ServerSentEvent.<StreamMessage>builder()
                        .id(UUID.randomUUID().toString())
                        .event(msg.type())
                        .data(msg)
                        .build())
                .doOnComplete(() -> log.info("Forecast stream completed"))
                .doOnError(error -> log.error("Forecast stream error", error));
    }

    /**
     * Stream historical weather data.
     * 
     * Returns historical weather as SSE stream, chunked by time period
     * for large date ranges.
     * 
     * @param latitude Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @param startDate Start date (yyyy-MM-dd)
     * @param endDate End date (yyyy-MM-dd)
     * @param timezone Timezone (default: auto)
     * @return Flux of ServerSentEvent with historical data chunks
     */
    @GetMapping(value = "/weather/historical", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('MCP_CLIENT', 'ADMIN')")
    public Flux<ServerSentEvent<StreamMessage>> streamHistoricalWeather(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "auto") String timezone) {
        
        log.info("Streaming historical weather: lat={}, lon={}, start={}, end={}", 
                latitude, longitude, startDate, endDate);

        return streamingWeatherService.streamHistoricalWeather(latitude, longitude, startDate, endDate, timezone)
                .map(msg -> ServerSentEvent.<StreamMessage>builder()
                        .id(UUID.randomUUID().toString())
                        .event(msg.type())
                        .data(msg)
                        .build())
                .doOnComplete(() -> log.info("Historical weather stream completed"))
                .doOnError(error -> log.error("Historical weather stream error", error));
    }

    /**
     * Stream weather with progress indicators.
     * 
     * Returns weather data with progress updates, useful for long operations.
     * 
     * @param latitude Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @param forecastDays Number of forecast days (default: 7)
     * @param timezone Timezone (default: auto)
     * @return Flux of ServerSentEvent with progress updates
     */
    @GetMapping(value = "/weather/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('MCP_CLIENT', 'ADMIN')")
    public Flux<ServerSentEvent<StreamMessage>> streamWithProgress(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "7") int forecastDays,
            @RequestParam(defaultValue = "auto") String timezone) {
        
        log.info("Streaming weather with progress: lat={}, lon={}, days={}", 
                latitude, longitude, forecastDays);

        return streamingWeatherService.streamWithProgress(latitude, longitude, forecastDays, timezone)
                .map(msg -> ServerSentEvent.<StreamMessage>builder()
                        .id(UUID.randomUUID().toString())
                        .event(msg.type())
                        .data(msg)
                        .build())
                .doOnComplete(() -> log.info("Progress stream completed"))
                .doOnError(error -> log.error("Progress stream error", error));
    }

    // ==================== CHAT STREAMING ENDPOINTS (PHASE 5) ====================

    /**
     * Stream AI chat response token-by-token.
     * 
     * Provides real-time chat streaming with <100ms latency between tokens
     * for natural conversation flow.
     * 
     * @param request Chat stream request with session and message
     * @return Flux of ServerSentEvent with token chunks
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('MCP_CLIENT', 'ADMIN')")
    @ConditionalOnProperty(name = "openmeteo.chat.enabled", havingValue = "true")
    public Flux<ServerSentEvent<StreamMessage>> streamChat(@RequestBody ChatStreamRequest request) {
        log.info("Starting chat stream for session: {}", request.sessionId());
        
        return streamingChatService.streamChat(request.sessionId(), request.message())
                .map(message -> ServerSentEvent.<StreamMessage>builder()
                        .event(message.type().toLowerCase())
                        .data(message)
                        .build())
                .doOnComplete(() -> log.info("Chat stream completed for session: {}", request.sessionId()))
                .doOnError(error -> log.error("Chat stream error for session: {}", request.sessionId(), error))
                .doOnCancel(() -> log.info("Chat stream cancelled for session: {}", request.sessionId()));
    }

    /**
     * Stream AI chat response with progress indicators.
     * 
     * Enhanced streaming with progress tracking for long responses.
     * Shows preparation steps before delivering actual tokens.
     * 
     * @param request Chat stream request with session and message
     * @return Flux of ServerSentEvent with progress and token chunks
     */
    @PostMapping(value = "/chat/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('MCP_CLIENT', 'ADMIN')")
    @ConditionalOnProperty(name = "openmeteo.chat.enabled", havingValue = "true")
    public Flux<ServerSentEvent<StreamMessage>> streamChatWithProgress(@RequestBody ChatStreamRequest request) {
        log.info("Starting chat stream with progress for session: {}", request.sessionId());
        
        return streamingChatService.streamChatWithProgress(request.sessionId(), request.message())
                .map(message -> ServerSentEvent.<StreamMessage>builder()
                        .event(message.type().toLowerCase())
                        .data(message)
                        .build())
                .doOnComplete(() -> log.info("Progress chat stream completed for session: {}", request.sessionId()))
                .doOnError(error -> log.error("Progress chat stream error for session: {}", request.sessionId(), error))
                .doOnCancel(() -> log.info("Progress chat stream cancelled for session: {}", request.sessionId()));
    }

    /**
     * Stream AI chat response with weather context enrichment.
     * 
     * Integrates weather data into chat responses when location is provided.
     * Automatically fetches and includes relevant weather information in context.
     * 
     * @param request Chat stream request with session, message, and optional location
     * @return Flux of ServerSentEvent with context-enriched token chunks
     */
    @PostMapping(value = "/chat/context", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyAuthority('MCP_CLIENT', 'ADMIN')")
    @ConditionalOnProperty(name = "openmeteo.chat.enabled", havingValue = "true")
    public Flux<ServerSentEvent<StreamMessage>> streamChatWithContext(@RequestBody ChatStreamRequest request) {
        log.info("Starting context-enriched chat stream for session: {} with weather: {}", 
            request.sessionId(), request.shouldIncludeWeather());
        
        if (request.shouldIncludeWeather()) {
            return streamingChatService.streamWithContext(
                    request.sessionId(), 
                    request.message(),
                    request.latitude(),
                    request.longitude()
                )
                .map(message -> ServerSentEvent.<StreamMessage>builder()
                        .event(message.type().toLowerCase())
                        .data(message)
                        .build())
                .doOnComplete(() -> log.info("Context chat stream completed for session: {}", request.sessionId()))
                .doOnError(error -> log.error("Context chat stream error for session: {}", request.sessionId(), error))
                .doOnCancel(() -> log.info("Context chat stream cancelled for session: {}", request.sessionId()));
        } else {
            // Fallback to simple chat if no weather context
            return streamChat(request);
        }
    }

    /**
     * Streaming status record.
     */
    public record StreamStatus(
            boolean enabled,
            String protocol,
            int maxConcurrentConnections,
            long maxStreamDurationMs
    ) {}
}
