package com.openmeteo.mcp.model.dto;

import java.util.List;

/**
 * Request DTO for API key generation.
 * 
 * Used in security management endpoints for creating new API keys.
 */
public record ApiKeyRequest(
        String clientName,
        List<String> roles,
        String description
) {
    public ApiKeyRequest {
        if (clientName == null || clientName.isBlank()) {
            throw new IllegalArgumentException("Client name is required");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
    }
}
