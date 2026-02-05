package com.openmeteo.mcp.model.dto;

import java.time.Instant;

/**
 * Security audit event for logging security-related activities.
 * 
 * Captures authentication attempts, API key operations,
 * and other security events for audit trail.
 */
public record SecurityAuditEvent(
        String eventType,
        String principal,
        String action,
        String resource,
        boolean success,
        String ipAddress,
        String userAgent,
        String details,
        Instant timestamp
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventType;
        private String principal;
        private String action;
        private String resource;
        private boolean success;
        private String ipAddress;
        private String userAgent;
        private String details;

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder principal(String principal) {
            this.principal = principal;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }

        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public SecurityAuditEvent build() {
            return new SecurityAuditEvent(
                    eventType,
                    principal,
                    action,
                    resource,
                    success,
                    ipAddress,
                    userAgent,
                    details,
                    Instant.now()
            );
        }
    }
}
