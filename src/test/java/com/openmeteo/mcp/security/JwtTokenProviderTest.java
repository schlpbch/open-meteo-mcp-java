package com.openmeteo.mcp.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtTokenProvider.
 * 
 * Tests JWT token generation, validation, and claim extraction
 * as specified in ADR-019.
 */
@DisplayName("JWT Token Provider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private UsernamePasswordAuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        // Initialize with test configuration
        jwtTokenProvider = new JwtTokenProvider(
                "test-secret-key-for-jwt-token-signing",
                3600000, // 1 hour
                86400000 // 24 hours
        );

        // Create test authentication
        authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MCP_CLIENT"))
        );
    }

    @Test
    @DisplayName("Should generate valid JWT access token")
    void shouldGenerateValidAccessToken() {
        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should generate valid JWT refresh token")
    void shouldGenerateValidRefreshToken() {
        // When
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should extract roles from token")
    void shouldExtractRolesFromToken() {
        // Given
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        String roles = jwtTokenProvider.getRolesFromToken(token);

        // Then
        assertThat(roles).isEqualTo("ROLE_MCP_CLIENT");
    }

    @Test
    @DisplayName("Should identify access token type")
    void shouldIdentifyAccessTokenType() {
        // Given
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        String tokenType = jwtTokenProvider.getTokenType(token);

        // Then
        assertThat(tokenType).isEqualTo("access");
    }

    @Test
    @DisplayName("Should identify refresh token type")
    void shouldIdentifyRefreshTokenType() {
        // Given
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // When
        String tokenType = jwtTokenProvider.getTokenType(refreshToken);

        // Then
        assertThat(tokenType).isEqualTo("refresh");
    }

    @Test
    @DisplayName("Should validate token is not expired")
    void shouldValidateTokenNotExpired() {
        // Given
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        // Then
        assertThat(isExpired).isFalse();
    }

    @Test
    @DisplayName("Should get expiration time remaining")
    void shouldGetExpirationTimeRemaining() {
        // Given
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        long timeRemaining = jwtTokenProvider.getExpirationTimeRemaining(token);

        // Then
        assertThat(timeRemaining).isGreaterThan(0);
        assertThat(timeRemaining).isLessThanOrEqualTo(3600000); // 1 hour max
    }

    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject malformed token")
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "not-a-jwt-token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should handle null token gracefully")
    void shouldHandleNullTokenGracefully() {
        // When
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should handle empty token gracefully")
    void shouldHandleEmptyTokenGracefully() {
        // When
        boolean isValid = jwtTokenProvider.validateToken("");

        // Then
        assertThat(isValid).isFalse();
    }
}