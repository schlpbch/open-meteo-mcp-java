package com.openmeteo.mcp.controller;

import com.openmeteo.mcp.model.stream.StreamChunk;
import com.openmeteo.mcp.model.stream.StreamMessage;
import com.openmeteo.mcp.model.stream.StreamMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
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
     * Streaming status record.
     */
    public record StreamStatus(
            boolean enabled,
            String protocol,
            int maxConcurrentConnections,
            long maxStreamDurationMs
    ) {}
}
