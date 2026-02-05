package com.openmeteo.mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stream Connection Manager.
 * 
 * Manages active streaming connections, enforces connection limits,
 * and provides connection statistics for monitoring.
 * 
 * Features:
 * - Connection tracking with metadata
 * - Configurable connection limits
 * - Automatic cleanup of expired connections
 * - Statistics for monitoring
 * 
 * Thread-safe implementation using ConcurrentHashMap.
 */
@Service
public class StreamConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(StreamConnectionManager.class);

    private final Map<String, ConnectionInfo> activeConnections = new ConcurrentHashMap<>();
    private final AtomicInteger totalConnectionsServed = new AtomicInteger(0);
    private final int maxConcurrentConnections;
    private final long maxConnectionDurationMs;

    public StreamConnectionManager(
            @Value("${streaming.max-concurrent-connections:100}") int maxConcurrentConnections,
            @Value("${streaming.max-connection-duration-ms:600000}") long maxConnectionDurationMs) {
        this.maxConcurrentConnections = maxConcurrentConnections;
        this.maxConnectionDurationMs = maxConnectionDurationMs;
        log.info("StreamConnectionManager initialized (max={}, duration={}ms)", 
                maxConcurrentConnections, maxConnectionDurationMs);
    }

    /**
     * Register a new streaming connection.
     * 
     * @param streamId Unique stream identifier
     * @param clientId Client identifier (username or API key)
     * @param streamType Type of stream (test, data, weather, chat)
     * @return true if connection was registered, false if limit exceeded
     */
    public boolean registerConnection(String streamId, String clientId, String streamType) {
        // Check connection limit
        if (activeConnections.size() >= maxConcurrentConnections) {
            log.warn("Connection limit exceeded: {} active connections", activeConnections.size());
            return false;
        }

        ConnectionInfo info = new ConnectionInfo(streamId, clientId, streamType, Instant.now());
        activeConnections.put(streamId, info);
        totalConnectionsServed.incrementAndGet();
        
        log.debug("Registered connection: {} (client={}, type={}, active={})", 
                streamId, clientId, streamType, activeConnections.size());
        return true;
    }

    /**
     * Unregister a streaming connection.
     * 
     * @param streamId Stream identifier
     */
    public void unregisterConnection(String streamId) {
        ConnectionInfo info = activeConnections.remove(streamId);
        if (info != null) {
            long durationMs = Instant.now().toEpochMilli() - info.startTime().toEpochMilli();
            log.debug("Unregistered connection: {} (duration={}ms, active={})", 
                    streamId, durationMs, activeConnections.size());
        }
    }

    /**
     * Check if connection limit is reached.
     * 
     * @return true if at capacity
     */
    public boolean isAtCapacity() {
        return activeConnections.size() >= maxConcurrentConnections;
    }

    /**
     * Get current active connection count.
     * 
     * @return Number of active connections
     */
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }

    /**
     * Get connection statistics.
     * 
     * @return Connection statistics
     */
    public ConnectionStats getStatistics() {
        return new ConnectionStats(
                activeConnections.size(),
                maxConcurrentConnections,
                totalConnectionsServed.get(),
                maxConnectionDurationMs
        );
    }

    /**
     * Cleanup expired connections.
     * 
     * Removes connections that have exceeded maximum duration.
     * Should be called periodically by scheduled task.
     * 
     * @return Number of connections cleaned up
     */
    public int cleanupExpiredConnections() {
        Instant now = Instant.now();
        int cleaned = 0;

        for (Map.Entry<String, ConnectionInfo> entry : activeConnections.entrySet()) {
            ConnectionInfo info = entry.getValue();
            long durationMs = now.toEpochMilli() - info.startTime().toEpochMilli();
            
            if (durationMs > maxConnectionDurationMs) {
                activeConnections.remove(entry.getKey());
                cleaned++;
                log.warn("Cleaned up expired connection: {} (duration={}ms)", 
                        entry.getKey(), durationMs);
            }
        }

        if (cleaned > 0) {
            log.info("Cleaned up {} expired connections", cleaned);
        }

        return cleaned;
    }

    /**
     * Get connection information.
     * 
     * @param streamId Stream identifier
     * @return Connection info or null if not found
     */
    public ConnectionInfo getConnectionInfo(String streamId) {
        return activeConnections.get(streamId);
    }

    /**
     * Connection information record.
     */
    public record ConnectionInfo(
            String streamId,
            String clientId,
            String streamType,
            Instant startTime
    ) {}

    /**
     * Connection statistics record.
     */
    public record ConnectionStats(
            int activeConnections,
            int maxConnections,
            int totalServed,
            long maxDurationMs
    ) {}
}
