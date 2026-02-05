package com.openmeteo.mcp.controller;

import com.openmeteo.mcp.service.ApiKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Security Management REST Controller.
 * 
 * Provides endpoints for API key management and security administration
 * as specified in ADR-019.
 * 
 * Security Features:
 * - API key generation and management (Admin only)
 * - Security status and health checks
 * - Authentication information endpoints
 */
@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SecurityController {

    private static final Logger log = LoggerFactory.getLogger(SecurityController.class);
    
    private final ApiKeyService apiKeyService;

    public SecurityController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
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
    public ResponseEntity<Map<String, Object>> generateApiKey(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        String clientName = (String) request.get("clientName");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.get("roles");
        
        if (clientName == null || roles == null || roles.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "clientName and roles are required"));
        }
        
        log.info("Admin {} generating API key for client: {} with roles: {}", 
                authentication.getName(), clientName, roles);
        
        String apiKey = apiKeyService.generateApiKey(clientName, roles);
        
        Map<String, Object> response = Map.of(
                "apiKey", apiKey,
                "clientName", clientName,
                "roles", roles,
                "message", "API key generated successfully. Store securely - it cannot be retrieved again."
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * List all active API keys (Admin only).
     */
    @GetMapping("/api-keys")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ApiKeyService.ApiKeyInfo>> listApiKeys(Authentication authentication) {
        log.info("Admin {} requested API key list", authentication.getName());
        
        List<ApiKeyService.ApiKeyInfo> apiKeys = apiKeyService.listActiveApiKeys();
        return ResponseEntity.ok(apiKeys);
    }

    /**
     * Revoke an API key (Admin only).
     */
    @DeleteMapping("/api-keys/{apiKey}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> revokeApiKey(
            @PathVariable String apiKey,
            Authentication authentication) {
        
        log.info("Admin {} revoking API key: {}***", 
                authentication.getName(), 
                apiKey.substring(0, Math.min(8, apiKey.length())));
        
        boolean revoked = apiKeyService.revokeApiKey(apiKey);
        
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
     * Security health check (Public endpoint).
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> securityHealth() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "security", "enabled",
                "authenticationMethods", List.of("JWT", "API_KEY"),
                "timestamp", System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(health);
    }
}