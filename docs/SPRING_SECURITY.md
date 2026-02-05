# Spring Security Implementation

This document describes the Spring Security implementation for the Open Meteo MCP Java application, as specified in [ADR-019: Use Spring Security for Authentication and Authorization](../spec/ADR_COMPENDIUM.md#adr-019-use-spring-security-for-authentication-and-authorization).

## Overview

The security implementation provides:
- **JWT Authentication** for web clients and admin interfaces
- **API Key Authentication** for MCP clients (Claude Desktop, other AI tools)
- **Role-based Authorization** with three levels: PUBLIC, MCP_CLIENT, ADMIN
- **CORS Configuration** for cross-origin MCP requests
- **Security Headers** for XSS and attack prevention

## Authentication Methods

### 1. API Key Authentication (Primary for MCP Clients)

API keys are used for MCP client authentication and are included in request headers:

```bash
# Primary header format
X-API-Key: mcp-client-dev-key-12345

# Alternative Authorization header formats
Authorization: ApiKey mcp-client-dev-key-12345
Authorization: Bearer simple-api-key-without-dots
```

**Default Development API Keys:**
- **MCP Client**: `mcp-client-dev-key-12345` (Role: MCP_CLIENT)
- **Admin**: `admin-dev-key-67890` (Roles: ADMIN, MCP_CLIENT)

### 2. JWT Token Authentication

JWT tokens provide stateless authentication for web clients:

```bash
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Token Features:**
- **Expiration**: 24 hours (configurable)
- **Refresh**: 7 days (configurable)
- **Signing**: HMAC-SHA512
- **Claims**: username, roles, token type (access/refresh)

## Authorization Levels

### PUBLIC
- **Endpoints**: `/health/**`, `/actuator/**`, `/metrics`
- **Access**: No authentication required
- **Purpose**: Health checks and monitoring

### MCP_CLIENT
- **Endpoints**: `/api/mcp/**`, `/mcp/**`
- **Access**: Requires MCP_CLIENT role
- **Purpose**: Weather tools and MCP protocol access

### ADMIN
- **Endpoints**: `/api/admin/**`, `/admin/**`, `/api/security/**`
- **Access**: Requires ADMIN role
- **Purpose**: System administration and user management

## Configuration

### Application Properties

```yaml
# Security Settings
security:
  jwt:
    secret: ${JWT_SECRET:openmeteo-mcp-jwt-secret-change-in-production}
    expiration: 86400000 # 24 hours
    refresh-expiration: 604800000 # 7 days
  api-key:
    header-name: X-API-Key
    cache-ttl: 300 # 5 minutes
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:*}
    max-age: 3600 # 1 hour

# Spring Security OAuth2 (for JWT)
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:http://localhost:8888}
```

### Environment Variables

For production deployment, set these environment variables:

```bash
# JWT Configuration
JWT_SECRET=your-secure-256-bit-secret-key-change-this
JWT_ISSUER_URI=https://your-domain.com

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://claude.ai,https://your-allowed-origin.com
```

## API Endpoints

### Security Management

#### Get Current User Info
```bash
GET /api/security/me
Headers:
  X-API-Key: mcp-client-dev-key-12345

Response:
{
  "username": "mcp-client-dev",
  "roles": ["ROLE_MCP_CLIENT"],
  "authenticated": true
}
```

#### Generate API Key (Admin Only)
```bash
POST /api/security/api-keys
Headers:
  X-API-Key: admin-dev-key-67890
Content-Type: application/json

Body:
{
  "clientName": "new-mcp-client",
  "roles": ["MCP_CLIENT"]
}

Response:
{
  "apiKey": "ak_a1b2c3d4...",
  "clientName": "new-mcp-client",
  "roles": ["MCP_CLIENT"],
  "message": "API key generated successfully..."
}
```

#### List API Keys (Admin Only)
```bash
GET /api/security/api-keys
Headers:
  X-API-Key: admin-dev-key-67890

Response:
[
  {
    "name": "mcp-client-dev",
    "roles": ["MCP_CLIENT"],
    "active": true,
    "createdAt": 1704067200000
  }
]
```

#### Security Health Check (Public)
```bash
GET /api/security/health

Response:
{
  "status": "UP",
  "security": "enabled",
  "authenticationMethods": ["JWT", "API_KEY"],
  "timestamp": 1704067200000
}
```

## Testing

### Unit Tests

Run JWT token provider tests:
```bash
mvn test -Dtest=JwtTokenProviderTest
```

### Integration Tests

Run security configuration tests:
```bash
mvn test -Dtest=SecurityConfigIntegrationTest
```

### Manual Testing

Test MCP endpoint with API key:
```bash
# Should succeed with valid API key
curl -H "X-API-Key: mcp-client-dev-key-12345" http://localhost:8888/api/mcp/tools

# Should fail with invalid API key
curl -H "X-API-Key: invalid-key" http://localhost:8888/api/mcp/tools

# Should succeed - public endpoint
curl http://localhost:8888/health
```

## Security Considerations

### Development vs Production

**Development:**
- Default API keys are provided for easy testing
- CORS allows all localhost origins
- JWT secret has a default value

**Production:**
- Generate unique API keys for each client
- Configure specific CORS allowed origins
- Use a strong, unique JWT secret (256+ bits)
- Enable HTTPS and security headers
- Monitor authentication failures and API key usage

### Best Practices

1. **API Key Management:**
   - Rotate API keys regularly
   - Use strong, randomly generated keys
   - Store keys securely (environment variables, secrets management)
   - Monitor API key usage and revoke unused keys

2. **JWT Security:**
   - Use strong JWT secrets (256+ bits)
   - Keep token expiration times short (24 hours)
   - Implement token refresh mechanism
   - Validate tokens on every request

3. **Monitoring:**
   - Log authentication failures
   - Monitor for brute force attacks
   - Track API key usage patterns
   - Set up alerts for suspicious activity

## Architecture Components

### Core Classes

- **`SecurityConfig`**: Main Spring Security configuration
- **`JwtTokenProvider`**: JWT token generation and validation
- **`ApiKeyAuthenticationFilter`**: API key authentication filter
- **`ApiKeyService`**: API key management service
- **`JwtAuthenticationEntryPoint`**: Custom authentication error handling
- **`SecurityController`**: Security management REST endpoints

### Security Flow

1. **Request arrives** at Spring Security filter chain
2. **ApiKeyAuthenticationFilter** checks for API key in headers
3. **JWT authentication** processes Bearer tokens (if no API key)
4. **Authorization** checks roles against endpoint requirements
5. **SecurityConfig** enforces access rules and CORS policies
6. **Request proceeds** to controller if authenticated and authorized

## Troubleshooting

### Common Issues

1. **401 Unauthorized**: Check API key format and validity
2. **403 Forbidden**: User authenticated but lacks required role
3. **CORS errors**: Configure allowed origins in application.yml
4. **JWT validation errors**: Check token format and expiration

### Debug Logging

Enable security debug logging:
```yaml
logging:
  level:
    org.springframework.security: DEBUG
    com.openmeteo.mcp.security: DEBUG
```

## Migration Guide

When upgrading from version without security:

1. **Add dependencies** to pom.xml (already included)
2. **Configure properties** in application.yml
3. **Update MCP clients** to include API keys in requests
4. **Test endpoints** with authentication
5. **Generate production API keys** for real clients

## Related Documentation

- [ADR-019: Use Spring Security](../spec/ADR_COMPENDIUM.md#adr-019-use-spring-security-for-authentication-and-authorization)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JWT RFC 7519](https://tools.ietf.org/html/rfc7519)