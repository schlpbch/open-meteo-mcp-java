package com.openmeteo.mcp.security;

import com.openmeteo.mcp.service.ApiKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

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
@Component
public class ApiKeyAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    
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
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        // Skip public endpoints
        if (shouldNotFilter(path)) {
            return chain.filter(exchange);
        }

        try {
            // Extract API key from request header
            String apiKey = extractApiKey(request);
            
            if (apiKey != null) {
                log.debug("API key found in request: {}***", apiKey.substring(0, Math.min(8, apiKey.length())));
                
                // Validate API key and get authentication if valid
                if (apiKeyService.isValidApiKey(apiKey)) {
                    Authentication authentication = apiKeyService.getAuthentication(apiKey);
                    
                    log.info("Successfully authenticated API key for: {}", 
                            authentication.getName());
                    
                    // Continue with authentication context
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                } else {
                    log.warn("Invalid API key attempted from IP: {}", getClientIpAddress(request));
                }
            }
        } catch (Exception e) {
            log.error("Error occurred during API key authentication: {}", e.getMessage(), e);
        }

        // Continue filter chain without authentication
        return chain.filter(exchange);
    }

    /**
     * Extract API key from request headers.
     * 
     * Supports multiple header formats:
     * - X-API-Key: direct-key-value
     * - Authorization: ApiKey key-value
     * - Authorization: Bearer key-value (if it's not a JWT)
     */
    private String extractApiKey(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        
        // Primary: Check configured API key header
        String apiKey = headers.getFirst(apiKeyHeaderName);
        if (StringUtils.hasText(apiKey)) {
            return apiKey.trim();
        }

        // Secondary: Check Authorization header for API key formats
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader)) {
            authHeader = authHeader.trim();
            String authHeaderLower = authHeader.toLowerCase();
            
            // Format: "ApiKey key-value"
            if (authHeaderLower.startsWith("apikey ")) {
                return authHeader.substring(7).trim();
            }
            
            // Format: "Bearer key-value" (but not JWT - simple heuristic)
            if (authHeaderLower.startsWith("bearer ")) {
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
    private String getClientIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        
        String xForwardedFor = headers.getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = headers.getFirst("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp.trim();
        }

        return request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
    }

    /**
     * Determine if filter should be applied to the request.
     * Skip authentication for public endpoints.
     */
    private boolean shouldNotFilter(String path) {
        // Skip public endpoints
        return path.startsWith("/health") ||
               path.startsWith("/actuator") ||
               path.startsWith("/metrics") ||
               path.equals("/") ||
               path.equals("/favicon.ico");
    }
}