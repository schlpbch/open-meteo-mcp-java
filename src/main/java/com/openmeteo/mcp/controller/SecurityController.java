package com.openmeteo.mcp.controller;

import com.openmeteo.mcp.model.dto.ApiKeyRequest;
import com.openmeteo.mcp.model.dto.ApiKeyResponse;
import com.openmeteo.mcp.model.dto.SecurityAuditEvent;
import com.openmeteo.mcp.service.ApiKeyService;
import com.openmeteo.mcp.service.SecurityAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Security Management REST Controller.
 * 
 * Provides endpoints for API key management and security administration
 * as specified in ADR-019 Phase 2.
 * 
 * Security Features:
 * - API key generation, revocation, and management (Admin only)
 * - Security audit logging and monitoring
 * - Authentication information endpoints
 * - Role-based access control
 */
@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SecurityController {

    private static final Logger log = LoggerFactory.getLogger(SecurityController.class);
    
    private final ApiKeyService apiKeyService;
    private final SecurityAuditService auditService;

    public SecurityController(ApiKeyService apiKeyService, SecurityAuditService auditService) {
        this.apiKeyService = apiKeyService;
        this.auditService = auditService;
    }

    /**
     * Get authentication information for current user.
     */
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MCP_CLIENT', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        log.info("Authentication info requested by: {}", authentication.getName());
        
        Map<String, Object> userInfo = Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .toList(),
                "authenticated", authentication.isAuthenticated()
        );
        
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Generate a new API key (Admin only).
     */
    @PostMapping("/api-keys")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiKeyResponse> generateApiKey(
            @RequestBody ApiKeyRequest request,
            Authentication authentication,
            ServerHttpRequest httpRequest) {
        
        try {
            log.info("Admin {} generating API key for client: {} with roles: {}", 
                    authentication.getName(), request.clientName(), request.roles());
            
            String apiKey = apiKeyService.generateApiKey(
                    request.clientName(), 
                    request.roles(),
                    request.description()
            );
            
            // Audit log
            auditService.logApiKeyGeneration(
                    authentication.getName(),
                    request.clientName(),
                    request.roles(),
                    getClientIpAddress(httpRequest)
            );
            
            ApiKeyResponse response = ApiKeyResponse.forGeneration(
                    apiKey,
                    request.clientName(),
                    request.roles(),
                    request.description()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid API key generation request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * List all active API keys (Admin only).
     */
    @GetMapping("/api-keys")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys(
            @RequestParam(required = false, defaultValue = "false") boolean includeInactive,
            Authentication authentication) {
        
        log.info("Admin {} requested API key list", authentication.getName());
        
        List<ApiKeyService.ApiKeyInfo> apiKeys = includeInactive 
                ? apiKeyService.listAllApiKeys()
                : apiKeyService.listActiveApiKeys();
        
        List<ApiKeyResponse> response = apiKeys.stream()
                .map(info -> ApiKeyResponse.forInfo(
                        info.getName(),
                        info.getRoles(),
                        info.getDescription(),
                        info.isActive(),
                        info.getCreatedAt()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get API key statistics (Admin only).
     */
    @GetMapping("/api-keys/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getApiKeyStatistics(Authentication authentication) {
        log.info("Admin {} requested API key statistics", authentication.getName());
        return ResponseEntity.ok(apiKeyService.getApiKeyStatistics());
    }

    /**
     * Revoke an API key (Admin only).
     */
    @DeleteMapping("/api-keys/{apiKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> revokeApiKey(
            @PathVariable String apiKey,
            Authentication authentication,
            ServerHttpRequest httpRequest) {
        
        String apiKeyPrefix = apiKey.substring(0, Math.min(8, apiKey.length()));
        log.info("Admin {} revoking API key: {}***", authentication.getName(), apiKeyPrefix);
        
        boolean revoked = apiKeyService.revokeApiKey(apiKey);
        
        // Audit log
        auditService.logApiKeyRevocation(
                authentication.getName(),
                apiKeyPrefix,
                revoked,
                getClientIpAddress(httpRequest)
            );
        
        if (revoked) {
            return ResponseEntity.ok(Map.of(
                    "message", "API key revoked successfully",
                    "status", "revoked"
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update API key roles (Admin only).
     */
    @PutMapping("/api-keys/{apiKey}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateApiKeyRoles(
            @PathVariable String apiKey,
            @RequestBody Map<String, List<String>> request,
            Authentication authentication) {
        
        List<String> newRoles = request.get("roles");
        if (newRoles == null || newRoles.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "roles are required"));
        }
        
        try {
            boolean updated = apiKeyService.updateApiKeyRoles(apiKey, newRoles);
            
            if (updated) {
                log.info("Admin {} updated API key roles", authentication.getName());
                return ResponseEntity.ok(Map.of(
                        "message", "API key roles updated successfully",
                        "roles", newRoles
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get audit events (Admin only).
     */
    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAuditEvent>> getAuditEvents(
            @RequestParam(required = false, defaultValue = "100") int limit,
            @RequestParam(required = false) String principal,
            Authentication authentication) {
        
        log.info("Admin {} requested audit events", authentication.getName());
        
        List<SecurityAuditEvent> events = principal != null
                ? auditService.getEventsByPrincipal(principal, limit)
                : auditService.getRecentEvents(limit);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Get failed authentication attempts (Admin only).
     */
    @GetMapping("/audit/failed-auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityAuditEvent>> getFailedAuthAttempts(
            @RequestParam(required = false, defaultValue = "24") int hours,
            Authentication authentication) {
        
        log.info("Admin {} requested failed auth attempts", authentication.getName());
        
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<SecurityAuditEvent> events = auditService.getFailedAuthAttempts(since);
        
        return ResponseEntity.ok(events);
    }

    /**
     * Security health check (Public endpoint).
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> securityHealth() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "security", "enabled",
                "authenticationMethods", List.of("JWT", "API_KEY"),
                "auditEventCount", auditService.getEventCount(),
                "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(health);
    }

    /**
     * Helper method to extract client IP address from request.
     */
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }

        return request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}