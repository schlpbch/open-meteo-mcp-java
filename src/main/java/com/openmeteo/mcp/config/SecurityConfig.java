package com.openmeteo.mcp.config;

import com.openmeteo.mcp.security.ApiKeyAuthenticationFilter;
import com.openmeteo.mcp.security.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

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
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
                         JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Disable CSRF as we're using stateless authentication
            .csrf(csrf -> csrf.disable())
            
            // Configure CORS for MCP clients
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Stateless session management (no server-side sessions)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/health/**", "/actuator/**", "/metrics").permitAll()
                
                // MCP endpoints - require MCP_CLIENT role
                .requestMatchers("/api/mcp/**", "/mcp/**").hasRole("MCP_CLIENT")
                
                // Admin endpoints - require ADMIN role
                .requestMatchers("/api/admin/**", "/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Configure JWT authentication via OAuth2 Resource Server
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(withDefaults())
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // Add API key authentication filter before standard authentication
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Configure security headers
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubdomains(true)
                )
            )
            
            // Configure exception handling
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
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