package com.openmeteo.mcp.chat.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChatMetrics.
 */
class ChatMetricsTest {
    
    private MeterRegistry meterRegistry;
    private ChatMetrics chatMetrics;
    
    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        chatMetrics = new ChatMetrics(meterRegistry);
    }
    
    @Test
    void testRecordRequest() {
        // When
        chatMetrics.recordRequest();
        
        // Then
        var counter = meterRegistry.counter("chat.requests.total");
        assertEquals(1.0, counter.count());
    }
    
    @Test
    void testRecordMultipleRequests() {
        // When
        chatMetrics.recordRequest();
        chatMetrics.recordRequest();
        chatMetrics.recordRequest();
        
        // Then
        var counter = meterRegistry.counter("chat.requests.total");
        assertEquals(3.0, counter.count());
    }
    
    @Test
    void testRecordSuccess() {
        // When
        chatMetrics.recordSuccess();
        
        // Then
        var counter = meterRegistry.counter("chat.requests.success");
        assertEquals(1.0, counter.count());
    }
    
    @Test
    void testRecordFailure() {
        // When
        chatMetrics.recordFailure();
        
        // Then
        var counter = meterRegistry.counter("chat.requests.failure");
        assertEquals(1.0, counter.count());
    }
    
    @Test
    void testRecordResponseTime() {
        // When
        chatMetrics.recordResponseTime(150L);
        chatMetrics.recordResponseTime(250L);
        
        // Then
        var timer = meterRegistry.timer("chat.response.time");
        assertEquals(2, timer.count());
        assertTrue(timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS) > 0);
    }
    
    @Test
    void testIncrementActiveSessions() {
        // When
        chatMetrics.incrementActiveSessions();
        
        // Then
        assertEquals(1, chatMetrics.getActiveSessions());
    }
    
    @Test
    void testDecrementActiveSessions() {
        // Given
        chatMetrics.incrementActiveSessions();
        chatMetrics.incrementActiveSessions();
        
        // When
        chatMetrics.decrementActiveSessions();
        
        // Then
        assertEquals(1, chatMetrics.getActiveSessions());
    }
    
    @Test
    void testDecrementActiveSessionsDoesNotGoBelowZero() {
        // When
        chatMetrics.decrementActiveSessions();
        chatMetrics.decrementActiveSessions();
        
        // Then
        assertEquals(0, chatMetrics.getActiveSessions());
    }
    
    @Test
    void testGetActiveSessions() {
        // Given
        chatMetrics.incrementActiveSessions();
        chatMetrics.incrementActiveSessions();
        chatMetrics.incrementActiveSessions();
        
        // When
        var activeSessions = chatMetrics.getActiveSessions();
        
        // Then
        assertEquals(3, activeSessions);
    }
    
    @Test
    void testCompleteWorkflow() {
        // Simulate a complete request workflow
        chatMetrics.recordRequest();
        chatMetrics.incrementActiveSessions();
        chatMetrics.recordResponseTime(200L);
        chatMetrics.recordSuccess();
        chatMetrics.decrementActiveSessions();
        
        // Verify all metrics
        assertEquals(1.0, meterRegistry.counter("chat.requests.total").count());
        assertEquals(1.0, meterRegistry.counter("chat.requests.success").count());
        assertEquals(0, chatMetrics.getActiveSessions());
        assertEquals(1, meterRegistry.timer("chat.response.time").count());
    }
}
