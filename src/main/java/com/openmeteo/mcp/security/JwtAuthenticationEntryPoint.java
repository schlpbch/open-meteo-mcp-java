package com.openmeteo.mcp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {

        // Log authentication failure for security audit
        log.warn("Authentication failed for request: {} {} from IP: {} - {}",
                request.getMethod(),
                request.getRequestURI(),
                getClientIpAddress(request),
                authException.getMessage());

        // Set response status and content type
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Create structured error response
        Map<String, Object> errorResponse = Map.of(
                "error", "unauthorized",
                "message", "Authentication required",
                "details", "Valid JWT token or API key required to access this resource",
                "timestamp", Instant.now().toString(),
                "path", request.getRequestURI(),
                "status", HttpStatus.UNAUTHORIZED.value()
        );

        // Write JSON response
        response.getOutputStream()
                .println(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Get client IP address from request, handling proxies.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp.trim();
        }

        return request.getRemoteAddr();
    }
}