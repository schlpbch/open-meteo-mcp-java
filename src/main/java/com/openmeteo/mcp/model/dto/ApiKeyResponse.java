package com.openmeteo.mcp.model.dto;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO for API key operations.
 * 
 * Contains API key information without exposing the actual key value
 * (except during initial generation).
 */
public record ApiKeyResponse(
        String apiKey,
        String clientName,
        List<String> roles,
        String description,
        boolean active,
        Instant createdAt,
        String message
) {
    /**
     * Create response for new API key generation (includes actual key).
     */
    public static ApiKeyResponse forGeneration(String apiKey, String clientName, 
                                               List<String> roles, String description) {
        return new ApiKeyResponse(
                apiKey,
                clientName,
                roles,
                description,
                true,
                Instant.now(),
                "API key generated successfully. Store securely - it cannot be retrieved again."
        );
    }

    /**
     * Create response for API key info (excludes actual key).
     */
    public static ApiKeyResponse forInfo(String clientName, List<String> roles, 
                                        String description, boolean active, long createdAtMillis) {
        return new ApiKeyResponse(
                null,
                clientName,
                roles,
                description,
                active,
                Instant.ofEpochMilli(createdAtMillis),
                null
        );
    }
}
