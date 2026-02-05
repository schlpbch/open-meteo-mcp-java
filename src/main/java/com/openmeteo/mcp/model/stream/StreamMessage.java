package com.openmeteo.mcp.model.stream;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * MCP Streamable HTTP Protocol Message.
 * 
 * Represents a single message in a Server-Sent Events (SSE) stream following
 * the MCP streaming protocol specification (ADR-020).
 * 
 * Message types:
 * - data: Actual content chunk
 * - metadata: Stream information and progress
 * - progress: Progress indicator for long operations
 * - error: Error information
 * - complete: Stream completion signal
 * 
 * @param type Message type
 * @param data Message payload
 * @param metadata Stream metadata
 * @param timestamp Message timestamp
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StreamMessage(
        String type,
        Object data,
        StreamMetadata metadata,
        Instant timestamp
) {
    /**
     * Create a data message with content chunk.
     */
    public static StreamMessage data(Object data) {
        return new StreamMessage("data", data, null, Instant.now());
    }

    /**
     * Create a data message with content and metadata.
     */
    public static StreamMessage data(Object data, StreamMetadata metadata) {
        return new StreamMessage("data", data, metadata, Instant.now());
    }

    /**
     * Create a metadata message.
     */
    public static StreamMessage metadata(StreamMetadata metadata) {
        return new StreamMessage("metadata", null, metadata, Instant.now());
    }

    /**
     * Create a progress message.
     */
    public static StreamMessage progress(int current, int total, String message) {
        var progressData = new ProgressData(current, total, message);
        var metadata = new StreamMetadata(null, null, null, current, total);
        return new StreamMessage("progress", progressData, metadata, Instant.now());
    }

    /**
     * Create an error message.
     */
    public static StreamMessage error(String errorMessage, String errorCode) {
        var errorData = new ErrorData(errorMessage, errorCode);
        return new StreamMessage("error", errorData, null, Instant.now());
    }

    /**
     * Create a completion message.
     */
    public static StreamMessage complete() {
        return new StreamMessage("complete", null, null, Instant.now());
    }

    /**
     * Create a completion message with final metadata.
     */
    public static StreamMessage complete(StreamMetadata metadata) {
        return new StreamMessage("complete", null, metadata, Instant.now());
    }

    /**
     * Progress data for progress messages.
     */
    public record ProgressData(int current, int total, String message) {}

    /**
     * Error data for error messages.
     */
    public record ErrorData(String message, String code) {}
}
