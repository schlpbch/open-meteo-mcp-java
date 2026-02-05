package com.openmeteo.mcp.model.stream;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Stream metadata for MCP Streamable HTTP Protocol.
 * 
 * Provides context information about the stream including content type,
 * encoding, progress tracking, and stream identification.
 * 
 * Used in both data and metadata messages to provide stream context.
 * 
 * @param streamId Unique identifier for this stream
 * @param contentType MIME type of streamed content
 * @param encoding Character encoding
 * @param progress Current progress (0-100)
 * @param totalChunks Total expected chunks (if known)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StreamMetadata(
        String streamId,
        String contentType,
        String encoding,
        Integer progress,
        Integer totalChunks
) {
    /**
     * Create basic metadata with stream ID.
     */
    public static StreamMetadata of(String streamId) {
        return new StreamMetadata(streamId, null, null, null, null);
    }

    /**
     * Create metadata with content type.
     */
    public static StreamMetadata of(String streamId, String contentType) {
        return new StreamMetadata(streamId, contentType, "UTF-8", null, null);
    }

    /**
     * Create metadata with progress tracking.
     */
    public static StreamMetadata withProgress(String streamId, int progress, int totalChunks) {
        return new StreamMetadata(streamId, null, null, progress, totalChunks);
    }

    /**
     * Create full metadata.
     */
    public static StreamMetadata full(String streamId, String contentType, String encoding, 
                                     Integer progress, Integer totalChunks) {
        return new StreamMetadata(streamId, contentType, encoding, progress, totalChunks);
    }
}
