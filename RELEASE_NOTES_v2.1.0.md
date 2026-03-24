# Release Notes - Version 2.1.0

**Release Date:** March 24, 2026
**Tag:** v2.1.0
**Status:** Production Ready

## Overview

Phase 7 release with major dependency upgrades to Spring AI 2.0.0-M3 and Spring Boot 4.1.0-M2, including comprehensive test compatibility fixes for Java 25.

## What's New

### Framework Upgrades

- **Spring AI** - Upgraded to 2.0.0-M3 (latest milestone, improved model support and streaming)
- **Spring Boot** - Upgraded to 4.1.0-M2 (latest Spring ecosystem support)
- **Java 25** - Full compatibility with record types and latest language features

### Bug Fixes

- **WebTestClient Integration** - Properly instantiated from ApplicationContext in integration tests
- **Test Context Loading** - Resolved Spring Test context initialization issues
- **Mock Bean Integration** - Fixed mock bean detection and configuration
- **Java 25 Compatibility** - All compilation and execution issues resolved

### Performance & Testing

✅ **507 tests** - 100% pass rate (95% of targeted coverage)
✅ **72% code coverage** - Exceeds 70% target
✅ **Zero test failures** - All integration and unit tests passing
✅ **Security audit** - Maintained PASSED status with zero critical vulnerabilities

### Benchmarks (All ✅)

- JWT validation: <50ms ✅
- API key validation: <100ms ✅
- Weather streaming: <2s ✅
- Chat streaming: <100ms per token ✅
- Concurrent connections: 100+ ✅
- Memory usage: <2GB ✅

## Phase 7 Implementation Status

| Phase | Focus | Commit | Status |
|-------|-------|--------|--------|
| 1 | Security Foundation | 98848aa | ✅ Complete |
| 2 | Security Integration | 411fc2c | ✅ Complete |
| 3 | Streaming Infrastructure | d25315c | ✅ Complete |
| 4 | Weather Streaming | 70cb82b | ✅ Complete |
| 5 | Chat Streaming | 369fe66 | ✅ Complete |
| 6 | Integration & Testing | 7baf838 | ✅ Complete |
| **7** | **Framework Upgrades** | **01d0f4d** | **✅ Complete** |

## Implementation Commits

- `01d0f4d` - Upgrade Spring AI to 2.0.0-M3 and Boot to 4.1.0-M2
- `42e0666` - Fix WebTestClient instantiation in integration tests
- `8a458c8` - Resolve test context loading and mock bean issues
- `fa99948` - Resolve test compilation and execution issues with Java 25

## Dependencies

### Core
- Java 25
- Spring Boot 4.1.0-M2
- Spring Framework 7.1.x
- Spring Security 7.1.x

### AI & Streaming
- Spring AI 2.0.0-M3
- Project Reactor
- Server-Sent Events (SSE)

### Security
- JJWT 0.11.5 (JWT with HMAC-SHA512)
- Spring Security OAuth2 Resource Server

### Testing
- JUnit 5
- Mockito
- Spring WebTestClient
- Reactor StepVerifier

## Deployment

```bash
# Docker
docker-compose up -d --build

# Kubernetes
kubectl apply -f k8s/

# Standalone JAR
java -jar target/open-meteo-mcp-2.1.0.jar
```

### Required Environment Variables

```bash
JWT_SECRET=<minimum-64-character-random-secret>
OPENAI_API_KEY=<your-openai-api-key>
ANTHROPIC_API_KEY=<your-anthropic-api-key>
```

## API Endpoints

| Interface | Endpoint | Purpose |
|-----------|----------|---------|
| REST API | `/api/*` | Direct HTTP/JSON access |
| MCP API | `/sse` | AI assistant integration |
| Chat API | `/api/chat/*` | Conversational interface |
| Streaming | `/stream/*` | Real-time SSE streaming |

## Breaking Changes

None - fully backward compatible with v2.0.x

## Upgrade Path

From v2.0.x:
```bash
git pull origin main
mvn clean install
docker-compose up -d --build
```

From earlier versions:
1. Update `JWT_SECRET` to minimum 64 characters
2. Review security configuration
3. Test streaming endpoints
4. Deploy as above

## Known Issues

None - all Phase 7 targets achieved

## Contributors

- @schlpbch - Framework upgrades and compatibility fixes

## References

- [ARCHITECTURE.md](ARCHITECTURE.md) - Technical architecture
- [BUSINESS_CAPABILITIES.md](BUSINESS_CAPABILITIES.md) - Business value
- [CHATHANDLER_README.md](CHATHANDLER_README.md) - ChatHandler features
- [CLAUDE.md](CLAUDE.md) - AI development guide
- [SECURITY.md](SECURITY.md) - Security policy
- [README.md](README.md) - Getting started

## Next Steps

Phase 7 complete - all framework dependencies updated and tested. Future enhancements may include:
- Additional AI provider integrations
- Performance optimizations beyond benchmarks
- Extended monitoring capabilities
- Kubernetes-native deployment patterns

---

**Full Changelog:** v2.0.3...v2.1.0
