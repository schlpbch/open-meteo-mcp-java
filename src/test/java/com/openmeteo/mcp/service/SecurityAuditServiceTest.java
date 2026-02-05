package com.openmeteo.mcp.service;

import com.openmeteo.mcp.model.dto.SecurityAuditEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SecurityAuditService.
 * 
 * Tests audit logging functionality as specified in ADR-019 Phase 2.
 */
@DisplayName("Security Audit Service Tests")
class SecurityAuditServiceTest {

    private SecurityAuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new SecurityAuditService();
        auditService.clearEvents();
    }

    @Test
    @DisplayName("Should log authentication attempt")
    void shouldLogAuthenticationAttempt() {
        // When
        auditService.logAuthenticationAttempt("testuser", true, "192.168.1.1", "Login successful");

        // Then
        List<SecurityAuditEvent> events = auditService.getRecentEvents(10);
        assertThat(events).hasSize(1);
        
        SecurityAuditEvent event = events.get(0);
        assertThat(event.eventType()).isEqualTo("AUTHENTICATION");
        assertThat(event.principal()).isEqualTo("testuser");
        assertThat(event.success()).isTrue();
        assertThat(event.ipAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    @DisplayName("Should log failed authentication")
    void shouldLogFailedAuthentication() {
        // When
        auditService.logAuthenticationAttempt("baduser", false, "192.168.1.100", "Invalid credentials");

        // Then
        List<SecurityAuditEvent> events = auditService.getRecentEvents(10);
        assertThat(events).hasSize(1);
        
        SecurityAuditEvent event = events.get(0);
        assertThat(event.success()).isFalse();
        assertThat(event.action()).isEqualTo("LOGIN_FAILURE");
    }

    @Test
    @DisplayName("Should log API key generation")
    void shouldLogApiKeyGeneration() {
        // When
        auditService.logApiKeyGeneration(
                "admin",
                "test-client",
                List.of("MCP_CLIENT"),
                "192.168.1.1"
        );

        // Then
        List<SecurityAuditEvent> events = auditService.getRecentEvents(10);
        assertThat(events).hasSize(1);
        
        SecurityAuditEvent event = events.get(0);
        assertThat(event.eventType()).isEqualTo("API_KEY_MANAGEMENT");
        assertThat(event.action()).isEqualTo("API_KEY_GENERATED");
        assertThat(event.resource()).contains("test-client");
    }

    @Test
    @DisplayName("Should log API key revocation")
    void shouldLogApiKeyRevocation() {
        // When
        auditService.logApiKeyRevocation("admin", "ak_12345", true, "192.168.1.1");

        // Then
        List<SecurityAuditEvent> events = auditService.getRecentEvents(10);
        assertThat(events).hasSize(1);
        
        SecurityAuditEvent event = events.get(0);
        assertThat(event.eventType()).isEqualTo("API_KEY_MANAGEMENT");
        assertThat(event.action()).isEqualTo("API_KEY_REVOKED");
        assertThat(event.success()).isTrue();
    }

    @Test
    @DisplayName("Should log access denied")
    void shouldLogAccessDenied() {
        // When
        auditService.logAccessDenied("user", "/api/admin", "GET", "192.168.1.1");

        // Then
        List<SecurityAuditEvent> events = auditService.getRecentEvents(10);
        assertThat(events).hasSize(1);
        
        SecurityAuditEvent event = events.get(0);
        assertThat(event.eventType()).isEqualTo("AUTHORIZATION");
        assertThat(event.action()).isEqualTo("ACCESS_DENIED");
        assertThat(event.success()).isFalse();
    }

    @Test
    @DisplayName("Should retrieve events by principal")
    void shouldRetrieveEventsByPrincipal() {
        // Given
        auditService.logAuthenticationAttempt("user1", true, "192.168.1.1", "Login");
        auditService.logAuthenticationAttempt("user2", true, "192.168.1.2", "Login");
        auditService.logAuthenticationAttempt("user1", true, "192.168.1.1", "Login again");

        // When
        List<SecurityAuditEvent> user1Events = auditService.getEventsByPrincipal("user1", 10);

        // Then
        assertThat(user1Events).hasSize(2);
        assertThat(user1Events).allMatch(e -> e.principal().equals("user1"));
    }

    @Test
    @DisplayName("Should retrieve failed auth attempts")
    void shouldRetrieveFailedAuthAttempts() {
        // Given
        auditService.logAuthenticationAttempt("user1", true, "192.168.1.1", "Success");
        auditService.logAuthenticationAttempt("user2", false, "192.168.1.2", "Failed");
        auditService.logAuthenticationAttempt("user3", false, "192.168.1.3", "Failed");

        // When
        Instant since = Instant.now().minus(1, ChronoUnit.HOURS);
        List<SecurityAuditEvent> failedAttempts = auditService.getFailedAuthAttempts(since);

        // Then
        assertThat(failedAttempts).hasSize(2);
        assertThat(failedAttempts).allMatch(e -> !e.success());
    }

    @Test
    @DisplayName("Should limit event count")
    void shouldLimitEventCount() {
        // When - add events
        for (int i = 0; i < 15; i++) {
            auditService.logAuthenticationAttempt("user" + i, true, "192.168.1." + i, "Login");
        }

        // Then
        assertThat(auditService.getEventCount()).isEqualTo(15);
        
        List<SecurityAuditEvent> recentEvents = auditService.getRecentEvents(10);
        assertThat(recentEvents).hasSize(10);
    }

    @Test
    @DisplayName("Should clear events")
    void shouldClearEvents() {
        // Given
        auditService.logAuthenticationAttempt("user1", true, "192.168.1.1", "Login");
        auditService.logAuthenticationAttempt("user2", true, "192.168.1.2", "Login");
        assertThat(auditService.getEventCount()).isEqualTo(2);

        // When
        auditService.clearEvents();

        // Then
        assertThat(auditService.getEventCount()).isEqualTo(0);
    }
}
