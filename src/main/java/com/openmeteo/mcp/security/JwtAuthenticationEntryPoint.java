package com.openmeteo.mcp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * JWT Authentication Entry Point for handling authentication failures.
 * 
 * Implements custom authentication error responses as per ADR-019.
 * 
 * Features:
 * - Structured JSON error responses
 * - Security audit logging
 * - Client-friendly error messages
 * - HTTP 401 status for authentication failures
 */
@Component
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    
    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException authException) {
        ServerHttpResponse response = exchange.getResponse();
        
        // Log authentication failure for security audit
        log.warn("Authentication failed for request: {} {} from IP: {} - {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                getClientIpAddress(exchange),
                authException.getMessage());

        // Set response status and content type
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Create structured error response
        Map<String, Object> errorResponse = Map.of(
                "error", "unauthorized",
                "message", "Authentication required",
                "details", "Valid JWT token or API key required to access this resource",
                "timestamp", Instant.now().toString(),
                "path", exchange.getRequest().getPath().value(),
                "status", HttpStatus.UNAUTHORIZED.value()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("Error writing authentication error response", e);
            return response.setComplete();
        }
    }

    /**
     * Get client IP address from request, handling proxies.
     */
    private String getClientIpAddress(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        
        String xForwardedFor = headers.getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = headers.getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }

        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}