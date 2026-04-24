# Release Notes - v2.1.2

**Release Date**: April 24, 2026  
**Status**: Stable ✅  
**Java Version**: 25  
**Spring Boot**: 4.1.0-M4

---

## Highlights

### Quality & Testing
- **577 passing tests** (up from 504 in v2.1.1)
- **81% instruction coverage** (up from 65%, now exceeds 80% target)
- 73 new unit tests covering previously untested controllers and security components
- Full test coverage for `SecurityController`, `StreamingController`, `StreamingChatController`, `McpToolsController`, `JwtAuthenticationEntryPoint`

### Dependency Upgrades
- **Spring Boot 4.1.0-M4** (from 4.0.5)
- **JJWT 0.12.7** (from 0.12.6)

---

## What's New in v2.1.2

### Test Coverage Improvements
New unit tests added for all previously undertested components:

| Class | Coverage before | Coverage after |
|-------|----------------|----------------|
| `SecurityController` | 0% | 100% |
| `McpToolsController` | 5% | 100% |
| `StreamingChatController` | 6% | 98% |
| `StreamingController` | 12% | 88% |
| `JwtAuthenticationEntryPoint` | 0% | 88% |
| `ApiKeyRequest` / `ApiKeyResponse` | 0% | 100% |
| `ChatStreamRequest` | 0% | 100% |

Tests use Mockito strict stubs, JUnit 5 nested test classes, and Reactor `StepVerifier` for SSE stream assertions.

### Dependency Upgrades
- **Spring Boot 4.1.0-M4**: Latest milestone with improved WebFlux performance
- **JJWT 0.12.7**: Security patch release for JWT library

### Architecture Documentation
- **ADR-020** added: Reactive Streaming with SSE (Flux/Mono pattern for streaming endpoints)
- **ADR Compendium v3.6.0**: Updated statuses — ADR-013, ADR-014, ADR-018, ADR-019 accepted
- Corrected all version references across documentation

---

## Technology Stack

- **Java 25** with Virtual Threads
- **Spring Boot 4.1.0-M4** / **Spring AI 2.0.0-M4** / **Spring Security 7.1.0-M3**
- **Spring WebFlux** — Reactive streaming (Flux/Mono + SSE)
- **Redis 8** — Session & conversation memory
- **JJWT 0.12.7** — JWT authentication (HMAC-SHA512)
- **Jackson 3** — JSON serialization
- **Docker** (Eclipse Temurin) — Production deployment

---

## Performance Benchmarks (All ✅)

| Component | Target | Status |
|-----------|--------|--------|
| JWT Validation | <50ms | ✅ |
| API Key Validation | <100ms | ✅ |
| Weather Streaming | <2s | ✅ |
| Chat Token Delivery | 50ms/token | ✅ |
| Concurrent Connections | 100+ | ✅ |
| Memory Footprint | <2GB | ✅ |

---

## Installation

### Requirements
- Java 25+
- Maven 3.8+
- Docker & Docker Compose (optional)

### Quick Start

```bash
git clone https://github.com/schlpbch/open-meteo-mcp-java.git
cd open-meteo-mcp-java
mvn clean install
export JWT_SECRET=your-64-char-minimum-secret-key
mvn spring-boot:run
```

### Docker

```bash
docker compose up --build
# or
docker build -t open-meteo-mcp:2.1.2 .
docker run -e JWT_SECRET=<secret> -p 8888:8888 open-meteo-mcp:2.1.2
```

---

## Migration Guide

### From v2.1.1 → v2.1.2

**Breaking Changes**: None

**Dependencies Updated**:
```xml
<!-- Spring Boot: 4.0.5 → 4.1.0-M4 -->
<version>4.1.0-M4</version>

<!-- JJWT: 0.12.6 → 0.12.7 -->
<jjwt.version>0.12.7</jjwt.version>
```

**Configuration Changes**: None required — fully backward compatible.

---

## Validation Checklist

- [x] 577 tests passing (0 failures)
- [x] Code coverage 81% (exceeds 80% target)
- [x] Security audit passed
- [x] Performance benchmarks met
- [x] Docker image builds successfully
- [x] Documentation updated

---

## Support

- **Issues**: [GitHub Issues](https://github.com/schlpbch/open-meteo-mcp-java/issues)
- **MCP Protocol**: [modelcontextprotocol.io](https://modelcontextprotocol.io/)
- **Open-Meteo API**: [open-meteo.com/docs](https://open-meteo.com/en/docs)

---

**Version**: 2.1.2  
**License**: Apache 2.0  
**Repository**: [schlpbch/open-meteo-mcp-java](https://github.com/schlpbch/open-meteo-mcp-java)
