package com.openmeteo.mcp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for Spring Security authentication.
 * 
 * Implements JWT token generation and validation as per ADR-019.
 * 
 * Features:
 * - Generate JWT tokens with user info and roles
 * - Validate JWT tokens and extract claims
 * - Configurable token expiration
 * - Secure HMAC-SHA512 signing
 * - Comprehensive error handling and logging
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final String jwtSecret;
    private final int jwtExpiration;
    private final int jwtRefreshExpiration;
    private final SecretKey signingKey;

    public JwtTokenProvider(
            @Value("${security.jwt.secret:defaultSecretForDevelopment}") String jwtSecret,
            @Value("${security.jwt.expiration:86400000}") int jwtExpiration,
            @Value("${security.jwt.refresh-expiration:604800000}") int jwtRefreshExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
        this.jwtRefreshExpiration = jwtRefreshExpiration;
        
        // Create a secure signing key from the secret
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        log.info("JWT Token Provider initialized with {}h expiration", jwtExpiration / 3600000);
    }

    /**
     * Generate a JWT access token for authenticated user.
     * 
     * @param authentication Spring Security authentication object
     * @return JWT token string
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        String token = Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("type", "access")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();

        log.debug("Generated JWT token for user: {} with roles: {}", username, roles);
        return token;
    }

    /**
     * Generate a JWT refresh token for token renewal.
     * 
     * @param authentication Spring Security authentication object
     * @return JWT refresh token string
     */
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtRefreshExpiration);

        String refreshToken = Jwts.builder()
                .setSubject(username)
                .claim("type", "refresh")
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();

        log.debug("Generated JWT refresh token for user: {}", username);
        return refreshToken;
    }

    /**
     * Extract username from JWT token.
     * 
     * @param token JWT token string
     * @return username from token subject
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * Extract roles from JWT token.
     * 
     * @param token JWT token string
     * @return comma-separated roles string
     */
    public String getRolesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("roles", String.class);
    }

    /**
     * Extract token type (access/refresh) from JWT token.
     * 
     * @param token JWT token string
     * @return token type
     */
    public String getTokenType(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("type", String.class);
    }

    /**
     * Validate JWT token signature and expiration.
     * 
     * @param token JWT token string
     * @return true if token is valid
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
            return false;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.error("JWT token compact format is invalid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if JWT token is expired.
     * 
     * @param token JWT token string
     * @return true if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Get expiration time remaining for JWT token.
     * 
     * @param token JWT token string
     * @return milliseconds until expiration, or 0 if expired/invalid
     */
    public long getExpirationTimeRemaining(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            long expiration = claims.getExpiration().getTime();
            long now = System.currentTimeMillis();
            return Math.max(0, expiration - now);
        } catch (JwtException e) {
            return 0;
        }
    }

    /**
     * Extract claims from JWT token with proper validation.
     * 
     * @param token JWT token string
     * @return JWT claims
     * @throws JwtException if token is invalid
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}