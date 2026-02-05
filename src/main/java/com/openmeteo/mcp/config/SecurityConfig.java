package com.openmeteo.mcp.config;

import com.openmeteo.mcp.security.ApiKeyAuthenticationFilter;
import com.openmeteo.mcp.security.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for the Open Meteo MCP server.
 * 
 * Implements ADR-019: Use Spring Security for Authentication and Authorization
 * 
 * Security Features:
 * - JWT authentication for web clients
 * - API key authentication for MCP clients  
 * - Role-based authorization (PUBLIC, MCP_CLIENT, ADMIN)
 * - CORS configuration for cross-origin requests
 * - Security headers for XSS and other attack prevention
 * - Stateless session management
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
                         JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // Disable CSRF as we're using stateless authentication
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            
            // Configure CORS for MCP clients
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure authorization rules
            .authorizeExchange(authz -> authz
                // Public endpoints - no authentication required
                .pathMatchers("/health/**", "/actuator/**", "/metrics").permitAll()
                
                // MCP endpoints - require MCP_CLIENT role
                .pathMatchers("/api/mcp/**", "/mcp/**").hasRole("MCP_CLIENT")
                
                // Admin endpoints - require ADMIN role
                .pathMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyExchange().authenticated()
            )
            
            // Configure OAuth2 Resource Server for JWT authentication
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> {})
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Add API key authentication filter
            .addFilterAt(apiKeyAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            
            // Configure exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            .build();
    }

    /**
     * CORS configuration to allow MCP clients to connect from different origins.
     * 
     * Configured to:
     * - Allow all origins during development (restrict in production)
     * - Support standard HTTP methods for REST APIs
     * - Allow all headers for flexibility
     * - Enable credentials for authentication
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow all origins (configure specific origins in production)
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // Allow standard HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials (required for authentication)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/mcp/**", configuration);
        
        return source;
    }
}