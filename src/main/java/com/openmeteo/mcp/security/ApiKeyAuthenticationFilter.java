package com.openmeteo.mcp.security;

import com.openmeteo.mcp.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API Key Authentication Filter for MCP clients.
 * 
 * Implements API key-based authentication as per ADR-019.
 * 
 * Features:
 * - Extract API key from X-API-Key header
 * - Validate API key against configured store
 * - Set authentication context for valid keys
 * - Support for multiple API key formats
 * - Comprehensive logging for security audit
 */
@Slf4j
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;
    private final String apiKeyHeaderName;

    public ApiKeyAuthenticationFilter(
            ApiKeyService apiKeyService,
            @Value("${security.api-key.header-name:X-API-Key}") String apiKeyHeaderName) {
        this.apiKeyService = apiKeyService;
        this.apiKeyHeaderName = apiKeyHeaderName;
        log.info("API Key Authentication Filter initialized with header: {}", apiKeyHeaderName);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        try {
            // Extract API key from request header
            String apiKey = extractApiKey(request);
            
            if (apiKey != null) {
                log.debug("API key found in request: {}***", apiKey.substring(0, Math.min(8, apiKey.length())));
                
                // Validate API key and get authentication if valid
                if (apiKeyService.isValidApiKey(apiKey)) {
                    Authentication authentication = apiKeyService.getAuthentication(apiKey);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.info("Successfully authenticated API key for: {}", 
                            authentication.getName());
                } else {
                    log.warn("Invalid API key attempted from IP: {}", getClientIpAddress(request));
                }
            }
        } catch (Exception e) {
            log.error("Error occurred during API key authentication: {}", e.getMessage(), e);
            // Clear security context on error
            SecurityContextHolder.clearContext();
        }

        // Continue filter chain regardless of authentication result
        filterChain.doFilter(request, response);
    }

    /**
     * Extract API key from request headers.
     * 
     * Supports multiple header formats:
     * - X-API-Key: direct-key-value
     * - Authorization: ApiKey key-value
     * - Authorization: Bearer key-value (if it's not a JWT)
     */
    private String extractApiKey(HttpServletRequest request) {
        // Primary: Check configured API key header
        String apiKey = request.getHeader(apiKeyHeaderName);
        if (StringUtils.hasText(apiKey)) {
            return apiKey.trim();
        }

        // Secondary: Check Authorization header for API key formats
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader)) {
            authHeader = authHeader.trim();
            
            // Format: "ApiKey key-value"
            if (authHeader.toLowerCase().startsWith("apikey ")) {
                return authHeader.substring(7).trim();
            }
            
            // Format: "Bearer key-value" (but not JWT - simple heuristic)
            if (authHeader.toLowerCase().startsWith("bearer ")) {
                String token = authHeader.substring(7).trim();
                // Simple heuristic: JWTs have dots, API keys typically don't
                if (!token.contains(".")) {
                    return token;
                }
            }
        }

        return null;
    }

    /**
     * Get client IP address from request, handling proxies.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }

    /**
     * Determine if filter should be applied to the request.
     * Skip authentication for public endpoints.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip public endpoints
        return path.startsWith("/health") ||
               path.startsWith("/actuator") ||
               path.startsWith("/metrics") ||
               path.equals("/") ||
               path.equals("/favicon.ico");
    }
}