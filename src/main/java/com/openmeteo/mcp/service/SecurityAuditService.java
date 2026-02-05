package com.openmeteo.mcp.service;

import com.openmeteo.mcp.model.dto.SecurityAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Security Audit Service for logging and tracking security events.
 * 
 * Implements audit logging as per ADR-019 Phase 2.
 * 
 * Features:
 * - Authentication event logging
 * - API key operation tracking
 * - Security event audit trail
 * - In-memory event storage (for development)
 * - Structured logging for SIEM integration
 */
@Service
public class SecurityAuditService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditService.class);
    private static final Logger auditLog = LoggerFactory.getLogger("SECURITY_AUDIT");
    
    private final ConcurrentLinkedQueue<SecurityAuditEvent> auditEvents = new ConcurrentLinkedQueue<>();
    private static final int MAX_EVENTS = 10000; // Limit in-memory storage

    /**
     * Log an authentication attempt.
     */
    public void logAuthenticationAttempt(String principal, boolean success, 
                                        String ipAddress, String details) {
        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("AUTHENTICATION")
                .principal(principal != null ? principal : "anonymous")
                .action(success ? "LOGIN_SUCCESS" : "LOGIN_FAILURE")
                .resource("authentication")
                .success(success)
                .ipAddress(ipAddress)
                .details(details)
                .build();
        
        recordEvent(event);
        
        if (success) {
            auditLog.info("Authentication successful - Principal: {}, IP: {}", principal, ipAddress);
        } else {
            auditLog.warn("Authentication failed - Principal: {}, IP: {}, Reason: {}", 
                    principal, ipAddress, details);
        }
    }

    /**
     * Log API key generation.
     */
    public void logApiKeyGeneration(String adminPrincipal, String clientName, 
                                   List<String> roles, String ipAddress) {
        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("API_KEY_MANAGEMENT")
                .principal(adminPrincipal)
                .action("API_KEY_GENERATED")
                .resource("api-key:" + clientName)
                .success(true)
                .ipAddress(ipAddress)
                .details("Generated API key for " + clientName + " with roles: " + roles)
                .build();
        
        recordEvent(event);
        auditLog.info("API key generated - Admin: {}, Client: {}, Roles: {}, IP: {}", 
                adminPrincipal, clientName, roles, ipAddress);
    }

    /**
     * Log API key revocation.
     */
    public void logApiKeyRevocation(String adminPrincipal, String apiKeyPrefix, 
                                   boolean success, String ipAddress) {
        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("API_KEY_MANAGEMENT")
                .principal(adminPrincipal)
                .action("API_KEY_REVOKED")
                .resource("api-key:" + apiKeyPrefix + "***")
                .success(success)
                .ipAddress(ipAddress)
                .details(success ? "API key revoked successfully" : "API key not found")
                .build();
        
        recordEvent(event);
        
        if (success) {
            auditLog.info("API key revoked - Admin: {}, Key: {}***, IP: {}", 
                    adminPrincipal, apiKeyPrefix, ipAddress);
        } else {
            auditLog.warn("API key revocation failed - Admin: {}, Key: {}***, IP: {}", 
                    adminPrincipal, apiKeyPrefix, ipAddress);
        }
    }

    /**
     * Log access denied event.
     */
    public void logAccessDenied(String principal, String resource, String action, String ipAddress) {
        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType("AUTHORIZATION")
                .principal(principal != null ? principal : "anonymous")
                .action("ACCESS_DENIED")
                .resource(resource)
                .success(false)
                .ipAddress(ipAddress)
                .details("Insufficient permissions for action: " + action)
                .build();
        
        recordEvent(event);
        auditLog.warn("Access denied - Principal: {}, Resource: {}, Action: {}, IP: {}", 
                principal, resource, action, ipAddress);
    }

    /**
     * Log generic security event.
     */
    public void logSecurityEvent(String eventType, String principal, String action, 
                                 String resource, boolean success, String ipAddress, String details) {
        SecurityAuditEvent event = SecurityAuditEvent.builder()
                .eventType(eventType)
                .principal(principal)
                .action(action)
                .resource(resource)
                .success(success)
                .ipAddress(ipAddress)
                .details(details)
                .build();
        
        recordEvent(event);
        
        String logLevel = success ? "INFO" : "WARN";
        auditLog.atLevel(org.slf4j.event.Level.valueOf(logLevel))
                .log("Security event - Type: {}, Principal: {}, Action: {}, Resource: {}, Success: {}, IP: {}, Details: {}", 
                     eventType, principal, action, resource, success, ipAddress, details);
    }

    /**
     * Get recent audit events.
     */
    public List<SecurityAuditEvent> getRecentEvents(int limit) {
        return auditEvents.stream()
                .sorted((e1, e2) -> e2.timestamp().compareTo(e1.timestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get events for a specific principal.
     */
    public List<SecurityAuditEvent> getEventsByPrincipal(String principal, int limit) {
        return auditEvents.stream()
                .filter(e -> e.principal().equals(principal))
                .sorted((e1, e2) -> e2.timestamp().compareTo(e1.timestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get failed authentication attempts.
     */
    public List<SecurityAuditEvent> getFailedAuthAttempts(Instant since) {
        return auditEvents.stream()
                .filter(e -> "AUTHENTICATION".equals(e.eventType()))
                .filter(e -> !e.success())
                .filter(e -> e.timestamp().isAfter(since))
                .sorted((e1, e2) -> e2.timestamp().compareTo(e1.timestamp()))
                .collect(Collectors.toList());
    }

    /**
     * Record audit event and maintain size limit.
     */
    private void recordEvent(SecurityAuditEvent event) {
        auditEvents.add(event);
        
        // Remove oldest events if limit exceeded
        while (auditEvents.size() > MAX_EVENTS) {
            auditEvents.poll();
        }
        
        log.debug("Recorded security audit event: {} - {} by {}", 
                event.eventType(), event.action(), event.principal());
    }

    /**
     * Get total event count.
     */
    public int getEventCount() {
        return auditEvents.size();
    }

    /**
     * Clear all audit events (for testing).
     */
    public void clearEvents() {
        auditEvents.clear();
        log.info("Cleared all security audit events");
    }
}
