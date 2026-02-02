package com.openmeteo.mcp.chat.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Metrics tracking for ChatHandler operations.
 * Provides observability through Micrometer metrics.
 * 
 * @since 1.2.0
 */
@Component
@ConditionalOnProperty(name = "openmeteo.chat.enabled", havingValue = "true")
public class ChatMetrics {
    
    private final Counter chatRequestsTotal;
    private final Counter chatRequestsSuccess;
    private final Counter chatRequestsFailure;
    private final Timer chatResponseTime;
    private final AtomicLong activeSessions;
    
    public ChatMetrics(MeterRegistry meterRegistry) {
        // Request counters
        this.chatRequestsTotal = Counter.builder("chat.requests.total")
            .description("Total number of chat requests")
            .tag("service", "chathandler")
            .register(meterRegistry);
        
        this.chatRequestsSuccess = Counter.builder("chat.requests.success")
            .description("Number of successful chat requests")
            .tag("service", "chathandler")
            .register(meterRegistry);
        
        this.chatRequestsFailure = Counter.builder("chat.requests.failure")
            .description("Number of failed chat requests")
            .tag("service", "chathandler")
            .register(meterRegistry);
        
        // Response time timer
        this.chatResponseTime = Timer.builder("chat.response.time")
            .description("Chat response time")
            .tag("service", "chathandler")
            .register(meterRegistry);
        
        // Active sessions gauge
        this.activeSessions = meterRegistry.gauge("chat.sessions.active",
            new AtomicLong(0));
    }
    
    /**
     * Record a chat request
     */
    public void recordRequest() {
        chatRequestsTotal.increment();
    }
    
    /**
     * Record a successful chat response
     */
    public void recordSuccess() {
        chatRequestsSuccess.increment();
    }
    
    /**
     * Record a failed chat response
     */
    public void recordFailure() {
        chatRequestsFailure.increment();
    }
    
    /**
     * Record response time
     */
    public void recordResponseTime(Duration duration) {
        chatResponseTime.record(duration);
    }
    
    /**
     * Record response time in milliseconds
     */
    public void recordResponseTime(long milliseconds) {
        chatResponseTime.record(Duration.ofMillis(milliseconds));
    }
    
    /**
     * Increment active sessions
     */
    public void incrementActiveSessions() {
        activeSessions.incrementAndGet();
    }
    
    /**
     * Decrement active sessions
     */
    public void decrementActiveSessions() {
        activeSessions.decrementAndGet();
    }
    
    /**
     * Get current active sessions count
     */
    public long getActiveSessions() {
        return activeSessions.get();
    }
}
