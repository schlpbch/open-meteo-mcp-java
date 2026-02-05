package com.openmeteo.mcp.model.stream;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Individual chunk of streamed data.
 * 
 * Represents a single data chunk in a streaming response, used for both
 * weather data and chat response streaming.
 * 
 * @param chunkId Sequential chunk identifier
 * @param content Chunk content
 * @param isLastChunk Whether this is the final chunk
 * @param timestamp Chunk creation timestamp
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StreamChunk(
        int chunkId,
        Object content,
        boolean isLastChunk,
        Instant timestamp
) {
    /**
     * Create a data chunk.
     */
    public static StreamChunk of(int chunkId, Object content) {
        return new StreamChunk(chunkId, content, false, Instant.now());
    }

    /**
     * Create the final chunk.
     */
    public static StreamChunk last(int chunkId, Object content) {
        return new StreamChunk(chunkId, content, true, Instant.now());
    }

    /**
     * Create an empty final chunk (completion marker).
     */
    public static StreamChunk complete(int chunkId) {
        return new StreamChunk(chunkId, null, true, Instant.now());
    }
}
