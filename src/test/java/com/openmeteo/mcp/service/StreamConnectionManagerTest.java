package com.openmeteo.mcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for StreamConnectionManager.
 * 
 * Tests connection management functionality as specified in ADR-020 Phase 3.
 */
@DisplayName("Stream Connection Manager Tests")
class StreamConnectionManagerTest {

    private StreamConnectionManager connectionManager;

    @BeforeEach
    void setUp() {
        connectionManager = new StreamConnectionManager(10, 60000); // 10 connections, 60s max
    }

    @Test
    @DisplayName("Should register connection")
    void shouldRegisterConnection() {
        // When
        boolean registered = connectionManager.registerConnection("stream-1", "client-1", "test");

        // Then
        assertThat(registered).isTrue();
        assertThat(connectionManager.getActiveConnectionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should unregister connection")
    void shouldUnregisterConnection() {
        // Given
        connectionManager.registerConnection("stream-1", "client-1", "test");
        assertThat(connectionManager.getActiveConnectionCount()).isEqualTo(1);

        // When
        connectionManager.unregisterConnection("stream-1");

        // Then
        assertThat(connectionManager.getActiveConnectionCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should enforce connection limit")
    void shouldEnforceConnectionLimit() {
        // Given - register 10 connections (max)
        for (int i = 0; i < 10; i++) {
            boolean registered = connectionManager.registerConnection("stream-" + i, "client-" + i, "test");
            assertThat(registered).isTrue();
        }

        // When - try to register 11th connection
        boolean registered = connectionManager.registerConnection("stream-11", "client-11", "test");

        // Then
        assertThat(registered).isFalse();
        assertThat(connectionManager.isAtCapacity()).isTrue();
    }

    @Test
    @DisplayName("Should get connection info")
    void shouldGetConnectionInfo() {
        // Given
        connectionManager.registerConnection("stream-1", "client-1", "weather");

        // When
        StreamConnectionManager.ConnectionInfo info = connectionManager.getConnectionInfo("stream-1");

        // Then
        assertThat(info).isNotNull();
        assertThat(info.streamId()).isEqualTo("stream-1");
        assertThat(info.clientId()).isEqualTo("client-1");
        assertThat(info.streamType()).isEqualTo("weather");
        assertThat(info.startTime()).isNotNull();
    }

    @Test
    @DisplayName("Should return null for non-existent connection")
    void shouldReturnNullForNonExistentConnection() {
        // When
        StreamConnectionManager.ConnectionInfo info = connectionManager.getConnectionInfo("non-existent");

        // Then
        assertThat(info).isNull();
    }

    @Test
    @DisplayName("Should get statistics")
    void shouldGetStatistics() {
        // Given
        connectionManager.registerConnection("stream-1", "client-1", "test");
        connectionManager.registerConnection("stream-2", "client-2", "data");

        // When
        StreamConnectionManager.ConnectionStats stats = connectionManager.getStatistics();

        // Then
        assertThat(stats.activeConnections()).isEqualTo(2);
        assertThat(stats.maxConnections()).isEqualTo(10);
        assertThat(stats.totalServed()).isEqualTo(2);
        assertThat(stats.maxDurationMs()).isEqualTo(60000);
    }

    @Test
    @DisplayName("Should track total connections served")
    void shouldTrackTotalConnectionsServed() {
        // When
        connectionManager.registerConnection("stream-1", "client-1", "test");
        connectionManager.registerConnection("stream-2", "client-2", "test");
        connectionManager.unregisterConnection("stream-1");
        connectionManager.registerConnection("stream-3", "client-3", "test");

        // Then
        StreamConnectionManager.ConnectionStats stats = connectionManager.getStatistics();
        assertThat(stats.totalServed()).isEqualTo(3);
        assertThat(stats.activeConnections()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should cleanup expired connections")
    void shouldCleanupExpiredConnections() throws InterruptedException {
        // Given - connection manager with 1ms max duration
        StreamConnectionManager shortDurationManager = new StreamConnectionManager(10, 1);
        shortDurationManager.registerConnection("stream-1", "client-1", "test");
        shortDurationManager.registerConnection("stream-2", "client-2", "test");
        
        // When - wait for expiration
        Thread.sleep(10);
        int cleaned = shortDurationManager.cleanupExpiredConnections();

        // Then
        assertThat(cleaned).isEqualTo(2);
        assertThat(shortDurationManager.getActiveConnectionCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should not cleanup non-expired connections")
    void shouldNotCleanupNonExpiredConnections() {
        // Given
        connectionManager.registerConnection("stream-1", "client-1", "test");

        // When
        int cleaned = connectionManager.cleanupExpiredConnections();

        // Then
        assertThat(cleaned).isEqualTo(0);
        assertThat(connectionManager.getActiveConnectionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should check capacity status")
    void shouldCheckCapacityStatus() {
        // Given - empty
        assertThat(connectionManager.isAtCapacity()).isFalse();

        // When - fill to capacity
        for (int i = 0; i < 10; i++) {
            connectionManager.registerConnection("stream-" + i, "client-" + i, "test");
        }

        // Then
        assertThat(connectionManager.isAtCapacity()).isTrue();
    }

    @Test
    @DisplayName("Should handle concurrent registrations")
    void shouldHandleConcurrentRegistrations() throws InterruptedException {
        // Given
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];

        // When - register connections concurrently
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                connectionManager.registerConnection("stream-" + index, "client-" + index, "test");
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        assertThat(connectionManager.getActiveConnectionCount()).isEqualTo(5);
        StreamConnectionManager.ConnectionStats stats = connectionManager.getStatistics();
        assertThat(stats.totalServed()).isEqualTo(5);
    }
}
