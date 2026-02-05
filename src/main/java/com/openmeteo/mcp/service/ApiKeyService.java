package com.openmeteo.mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * API Key Service for managing API key authentication.
 * 
 * Implements API key management as per ADR-019 Phase 2.
 * 
 * Features:
 * - API key validation and authentication
 * - In-memory API key store (development)
 * - Role-based API key assignment and management
 * - Secure API key generation with prefix
 * - API key metadata and description
 * - Audit logging integration
 */
@Service
public class ApiKeyService {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyService.class);
    private static final Set<String> VALID_ROLES = Set.of("MCP_CLIENT", "ADMIN", "PUBLIC");
    
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
     * Generate a new API key for a client with description.
     */
    public String generateApiKey(String clientName, List<String> roles, String description) {
        validateRoles(roles);
        
        String apiKey = generateSecureApiKey();
        
        ApiKeyInfo keyInfo = new ApiKeyInfo(
                clientName,
                roles,
                description != null ? description : "Generated API key for " + clientName,
                true,
                System.currentTimeMillis()
        );
        
        apiKeys.put(apiKey, keyInfo);
        log.info("Generated new API key for client: {} with roles: {}", clientName, roles);
        
        return apiKey;
    }

    /**
     * Generate a new API key for a client (backward compatibility).
     */
    public String generateApiKey(String clientName, List<String> roles) {
        return generateApiKey(clientName, roles, null);
    }

    /**
     * Update API key roles (admin operation).
     */
    @CacheEvict(value = "api-keys", key = "#apiKey")
    public boolean updateApiKeyRoles(String apiKey, List<String> newRoles) {
        validateRoles(newRoles);
        
        ApiKeyInfo keyInfo = apiKeys.get(apiKey);
        if (keyInfo != null && keyInfo.isActive()) {
            // Create updated key info with new roles
            ApiKeyInfo updated = new ApiKeyInfo(
                    keyInfo.getName(),
                    newRoles,
                    keyInfo.getDescription(),
                    keyInfo.isActive(),
                    keyInfo.getCreatedAt()
            );
            apiKeys.put(apiKey, updated);
            log.info("Updated roles for API key {}: {} -> {}", 
                    keyInfo.getName(), keyInfo.getRoles(), newRoles);
            return true;
        }
        return false;
    }

    /**
     * Get API key information by key (without exposing the key).
     */
    public Optional<ApiKeyInfo> getApiKeyInfo(String apiKey) {
        return Optional.ofNullable(apiKeys.get(apiKey));
    }

    /**
     * Get API key by client name.
     */
    public Optional<String> findApiKeyByClientName(String clientName) {
        return apiKeys.entrySet().stream()
                .filter(e -> e.getValue().getName().equals(clientName))
                .filter(e -> e.getValue().isActive())
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Revoke an API key.
     */
    @CacheEvict(value = "api-keys", key = "#apiKey")
    public boolean revokeApiKey(String apiKey) {
        ApiKeyInfo keyInfo = apiKeys.get(apiKey);
        if (keyInfo != null) {
            keyInfo.setActive(false);
            log.info("Revoked API key for client: {}", keyInfo.getName());
            return true;
        }
        log.warn("Attempted to revoke non-existent API key");
        return false;
    }

    /**
     * Delete an API key permanently.
     */
    @CacheEvict(value = "api-keys", key = "#apiKey")
    public boolean deleteApiKey(String apiKey) {
        ApiKeyInfo removed = apiKeys.remove(apiKey);
        if (removed != null) {
            log.info("Deleted API key for client: {}", removed.getName());
            return true;
        }
        return false;
    }

    /**
     * Reactivate a revoked API key.
     */
    @CacheEvict(value = "api-keys", key = "#apiKey")
    public boolean reactivateApiKey(String apiKey) {
        ApiKeyInfo keyInfo = apiKeys.get(apiKey);
        if (keyInfo != null && !keyInfo.isActive()) {
            keyInfo.setActive(true);
            log.info("Reactivated API key for client: {}", keyInfo.getName());
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
                .collect(Collectors.toList());
    }

    /**
     * List all API keys including inactive ones (admin operation).
     */
    public List<ApiKeyInfo> listAllApiKeys() {
        return new ArrayList<>(apiKeys.values());
    }

    /**
     * Get count of API keys by status.
     */
    public Map<String, Long> getApiKeyStatistics() {
        long active = apiKeys.values().stream().filter(ApiKeyInfo::isActive).count();
        long inactive = apiKeys.size() - active;
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) apiKeys.size());
        stats.put("active", active);
        stats.put("inactive", inactive);
        
        return stats;
    }

    /**
     * Validate roles against allowed set.
     */
    private void validateRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
        
        List<String> invalidRoles = roles.stream()
                .filter(role -> !VALID_ROLES.contains(role))
                .collect(Collectors.toList());
        
        if (!invalidRoles.isEmpty()) {
            throw new IllegalArgumentException("Invalid roles: " + invalidRoles + 
                    ". Valid roles are: " + VALID_ROLES);
        }
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
                "Default MCP client for development",
                true,
                System.currentTimeMillis()
        ));

        // Default admin API key
        String adminKey = "admin-dev-key-67890";
        apiKeys.put(adminKey, new ApiKeyInfo(
                "admin-dev",
                List.of("ADMIN", "MCP_CLIENT"),
                "Default admin for development",
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
        private final String description;
        private boolean active;
        private final long createdAt;

        public ApiKeyInfo(String name, List<String> roles, String description, 
                         boolean active, long createdAt) {
            this.name = name;
            this.roles = List.copyOf(roles); // Immutable copy
            this.description = description;
            this.active = active;
            this.createdAt = createdAt;
        }

        // Getters
        public String getName() { return name; }
        public List<String> getRoles() { return roles; }
        public String getDescription() { return description; }
        public boolean isActive() { return active; }
        public long getCreatedAt() { return createdAt; }

        // Setters
        public void setActive(boolean active) { this.active = active; }
    }
}