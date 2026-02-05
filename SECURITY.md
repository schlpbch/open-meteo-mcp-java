# Security Policy

## Security Status

**Latest Security Audit:** ✅ PASSED (Zero Critical Vulnerabilities)  
**OWASP Top 10 Compliance:** ✅ Verified  
**Last Audit Date:** Phase 6 Completion (Commit: 7baf838)

## Security Features

Open-Meteo MCP Server implements enterprise-grade security:

- **Authentication**: Dual authentication (JWT + API Keys) with HMAC-SHA512
- **Authorization**: Role-Based Access Control (RBAC) - PUBLIC, MCP_CLIENT, ADMIN
- **Audit Logging**: Comprehensive security event logging (10,000+ events tracked)
- **Performance**: JWT validation <50ms, API key validation <100ms
- **Spring Security 7**: Latest security framework with OAuth2 Resource Server
- **JJWT 0.11.5**: Industry-standard JWT implementation

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed security architecture.

## Supported Versions

| Version | Supported          | Notes                           |
| ------- | ------------------ | ------------------------------- |
| 2.0.x   | :white_check_mark: | Current (Phase 6 Complete)      |
| 1.2.x   | :white_check_mark: | Security backports only         |
| < 1.2   | :x:                | Upgrade required                |

## Reporting a Vulnerability

We take security vulnerabilities seriously. Please report security issues responsibly.

### Reporting Process

1. **DO NOT** open a public GitHub issue for security vulnerabilities
2. Email security reports to the repository owner (check GitHub profile)
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if available)
   - Your contact information

### Response Timeline

- **Initial Response**: Within 48 hours
- **Status Update**: Every 7 days until resolution
- **Fix Timeline**: Critical issues within 7-14 days, others within 30 days

### What to Expect

**If Accepted:**
- We will confirm the vulnerability and its severity
- We will develop and test a fix
- We will release a security patch
- We will credit you in the release notes (if desired)

**If Declined:**
- We will explain why it's not considered a vulnerability
- We may suggest alternative solutions or configurations
- We will remain open to further discussion

## Security Best Practices

When deploying this server:

1. **JWT Secret**: Use minimum 64-character random secret for `JWT_SECRET`
2. **API Keys**: Store API keys securely (environment variables, secrets manager)
3. **HTTPS Only**: Always use TLS/HTTPS in production
4. **Rate Limiting**: Configure appropriate rate limits for your use case
5. **Monitoring**: Enable security audit logging and monitor for anomalies
6. **Updates**: Keep dependencies updated (run `mvn versions:display-dependency-updates`)

## Security Contacts

- **GitHub Issues**: For general security questions (non-vulnerabilities)
- **GitHub Security Advisories**: For coordinated vulnerability disclosure
- **Email**: For private security reports (see repository owner profile)

## Additional Resources

- [ARCHITECTURE.md](ARCHITECTURE.md) - Security Layer documentation
- [CHATHANDLER_README.md](CHATHANDLER_README.md) - Authentication examples
- [BUSINESS_CAPABILITIES.md](BUSINESS_CAPABILITIES.md) - Enterprise Security & Compliance capability
