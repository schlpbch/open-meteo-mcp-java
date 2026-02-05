package com.openmeteo.mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API Key Service for managing API key authentication.
 * 
 * Implements API key management as per ADR-019.
 * 
 * Features:
 * - API key validation and authentication
 * - In-memory API key store (development)
 * - Configurable API key cache TTL
 * - Role-based API key assignment
 * - Secure API key generation
 */
@Slf4j
@Service
public class ApiKeyService {

    private final Map<String, ApiKeyInfo> apiKeys = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final int cacheTimeToLive;

    public ApiKeyService(@Value("${security.api-key.cache-ttl:300}") int cacheTimeToLive) {
        this.cacheTimeToLive = cacheTimeToLive;
        initializeDefaultApiKeys();
        log.info("API Key Service initialized with {} default keys", apiKeys.size());
    }

    /**
     * Validate if API key exists and is active.
     */
    @Cacheable(value = "api-keys", key = "#apiKey")
    public boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        ApiKeyInfo keyInfo = apiKeys.get(apiKey);
        if (keyInfo == null) {
            log.debug("API key not found: {}***", apiKey.substring(0, Math.min(8, apiKey.length())));
            return false;
        }

        if (!keyInfo.isActive()) {
            log.debug("API key is inactive: {}", keyInfo.getName());
            return false;
        }

        return true;
    }

    /**
     * Get authentication object for valid API key.
     */
    public Authentication getAuthentication(String apiKey) {
        ApiKeyInfo keyInfo = apiKeys.get(apiKey);
        if (keyInfo == null) {
            return null;
        }

        List<SimpleGrantedAuthority> authorities = keyInfo.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        return new UsernamePasswordAuthenticationToken(
                keyInfo.getName(),
                null,
                authorities
        );
    }

    /**
     * Generate a new API key for a client.
     */
    public String generateApiKey(String clientName, List<String> roles) {
        String apiKey = generateSecureApiKey();
        
        ApiKeyInfo keyInfo = new ApiKeyInfo(
                clientName,
                roles,
                true,
                System.currentTimeMillis()
        );
        
        apiKeys.put(apiKey, keyInfo);
        log.info("Generated new API key for client: {} with roles: {}", clientName, roles);
        
        return apiKey;
    }

    /**
     * Revoke an API key.
     */
    public boolean revokeApiKey(String apiKey) {
        ApiKeyInfo keyInfo = apiKeys.get(apiKey);
        if (keyInfo != null) {
            keyInfo.setActive(false);
            log.info("Revoked API key for client: {}", keyInfo.getName());
            return true;
        }
        return false;
    }

    /**
     * List all active API keys (without revealing the actual keys).
     */
    public List<ApiKeyInfo> listActiveApiKeys() {
        return apiKeys.values().stream()
                .filter(ApiKeyInfo::isActive)
                .toList();
    }

    /**
     * Initialize default API keys for development and testing.
     */
    private void initializeDefaultApiKeys() {
        // Default MCP client API key
        String mcpClientKey = "mcp-client-dev-key-12345";
        apiKeys.put(mcpClientKey, new ApiKeyInfo(
                "mcp-client-dev",
                List.of("MCP_CLIENT"),
                true,
                System.currentTimeMillis()
        ));

        // Default admin API key
        String adminKey = "admin-dev-key-67890";
        apiKeys.put(adminKey, new ApiKeyInfo(
                "admin-dev",
                List.of("ADMIN", "MCP_CLIENT"),
                true,
                System.currentTimeMillis()
        ));

        log.info("Initialized default API keys - MCP Client: {}***, Admin: {}***",
                mcpClientKey.substring(0, 8),
                adminKey.substring(0, 8));
    }

    /**
     * Generate cryptographically secure API key.
     */
    private String generateSecureApiKey() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : randomBytes) {
            sb.append(String.format("%02x", b));
        }
        
        return "ak_" + sb.toString();
    }

    /**
     * API Key Information holder.
     */
    public static class ApiKeyInfo {
        private final String name;
        private final List<String> roles;
        private boolean active;
        private final long createdAt;

        public ApiKeyInfo(String name, List<String> roles, boolean active, long createdAt) {
            this.name = name;
            this.roles = roles;
            this.active = active;
            this.createdAt = createdAt;
        }

        // Getters
        public String getName() { return name; }
        public List<String> getRoles() { return roles; }
        public boolean isActive() { return active; }
        public long getCreatedAt() { return createdAt; }

        // Setters
        public void setActive(boolean active) { this.active = active; }
    }
}