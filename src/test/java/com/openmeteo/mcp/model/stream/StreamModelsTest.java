package com.openmeteo.mcp.model.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Stream Protocol Models.
 * 
 * Tests StreamMessage, StreamMetadata, and StreamChunk models
 * as specified in ADR-020 Phase 3.
 */
@DisplayName("Stream Protocol Models Tests")
class StreamModelsTest {

    @Test
    @DisplayName("Should create data message")
    void shouldCreateDataMessage() {
        // When
        StreamMessage message = StreamMessage.data("test content");

        // Then
        assertThat(message.type()).isEqualTo("data");
        assertThat(message.data()).isEqualTo("test content");
        assertThat(message.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should create data message with metadata")
    void shouldCreateDataMessageWithMetadata() {
        // Given
        StreamMetadata metadata = StreamMetadata.of("stream-123", "application/json");

        // When
        StreamMessage message = StreamMessage.data("content", metadata);

        // Then
        assertThat(message.type()).isEqualTo("data");
        assertThat(message.data()).isEqualTo("content");
        assertThat(message.metadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("Should create metadata message")
    void shouldCreateMetadataMessage() {
        // Given
        StreamMetadata metadata = StreamMetadata.of("stream-123");

        // When
        StreamMessage message = StreamMessage.metadata(metadata);

        // Then
        assertThat(message.type()).isEqualTo("metadata");
        assertThat(message.metadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("Should create progress message")
    void shouldCreateProgressMessage() {
        // When
        StreamMessage message = StreamMessage.progress(5, 10, "Processing...");

        // Then
        assertThat(message.type()).isEqualTo("progress");
        assertThat(message.data()).isInstanceOf(StreamMessage.ProgressData.class);
        
        StreamMessage.ProgressData progressData = (StreamMessage.ProgressData) message.data();
        assertThat(progressData.current()).isEqualTo(5);
        assertThat(progressData.total()).isEqualTo(10);
        assertThat(progressData.message()).isEqualTo("Processing...");
        
        assertThat(message.metadata().progress()).isEqualTo(5);
        assertThat(message.metadata().totalChunks()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should create error message")
    void shouldCreateErrorMessage() {
        // When
        StreamMessage message = StreamMessage.error("Error occurred", "ERR_001");

        // Then
        assertThat(message.type()).isEqualTo("error");
        assertThat(message.data()).isInstanceOf(StreamMessage.ErrorData.class);
        
        StreamMessage.ErrorData errorData = (StreamMessage.ErrorData) message.data();
        assertThat(errorData.message()).isEqualTo("Error occurred");
        assertThat(errorData.code()).isEqualTo("ERR_001");
    }

    @Test
    @DisplayName("Should create completion message")
    void shouldCreateCompletionMessage() {
        // When
        StreamMessage message = StreamMessage.complete();

        // Then
        assertThat(message.type()).isEqualTo("complete");
        assertThat(message.data()).isNull();
    }

    @Test
    @DisplayName("Should create completion message with metadata")
    void shouldCreateCompletionMessageWithMetadata() {
        // Given
        StreamMetadata metadata = StreamMetadata.of("stream-123");

        // When
        StreamMessage message = StreamMessage.complete(metadata);

        // Then
        assertThat(message.type()).isEqualTo("complete");
        assertThat(message.metadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("Should create stream metadata with ID")
    void shouldCreateStreamMetadataWithId() {
        // When
        StreamMetadata metadata = StreamMetadata.of("stream-123");

        // Then
        assertThat(metadata.streamId()).isEqualTo("stream-123");
        assertThat(metadata.contentType()).isNull();
    }

    @Test
    @DisplayName("Should create stream metadata with content type")
    void shouldCreateStreamMetadataWithContentType() {
        // When
        StreamMetadata metadata = StreamMetadata.of("stream-123", "application/json");

        // Then
        assertThat(metadata.streamId()).isEqualTo("stream-123");
        assertThat(metadata.contentType()).isEqualTo("application/json");
        assertThat(metadata.encoding()).isEqualTo("UTF-8");
    }

    @Test
    @DisplayName("Should create stream metadata with progress")
    void shouldCreateStreamMetadataWithProgress() {
        // When
        StreamMetadata metadata = StreamMetadata.withProgress("stream-123", 7, 10);

        // Then
        assertThat(metadata.streamId()).isEqualTo("stream-123");
        assertThat(metadata.progress()).isEqualTo(7);
        assertThat(metadata.totalChunks()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should create full stream metadata")
    void shouldCreateFullStreamMetadata() {
        // When
        StreamMetadata metadata = StreamMetadata.full("stream-123", "text/plain", "UTF-8", 5, 10);

        // Then
        assertThat(metadata.streamId()).isEqualTo("stream-123");
        assertThat(metadata.contentType()).isEqualTo("text/plain");
        assertThat(metadata.encoding()).isEqualTo("UTF-8");
        assertThat(metadata.progress()).isEqualTo(5);
        assertThat(metadata.totalChunks()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should create stream chunk")
    void shouldCreateStreamChunk() {
        // When
        StreamChunk chunk = StreamChunk.of(0, "chunk content");

        // Then
        assertThat(chunk.chunkId()).isEqualTo(0);
        assertThat(chunk.content()).isEqualTo("chunk content");
        assertThat(chunk.isLastChunk()).isFalse();
        assertThat(chunk.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should create last stream chunk")
    void shouldCreateLastStreamChunk() {
        // When
        StreamChunk chunk = StreamChunk.last(9, "final chunk");

        // Then
        assertThat(chunk.chunkId()).isEqualTo(9);
        assertThat(chunk.content()).isEqualTo("final chunk");
        assertThat(chunk.isLastChunk()).isTrue();
    }

    @Test
    @DisplayName("Should create complete marker chunk")
    void shouldCreateCompleteMarkerChunk() {
        // When
        StreamChunk chunk = StreamChunk.complete(10);

        // Then
        assertThat(chunk.chunkId()).isEqualTo(10);
        assertThat(chunk.content()).isNull();
        assertThat(chunk.isLastChunk()).isTrue();
    }
}
