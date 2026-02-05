# CLAUDE.md - Open-Meteo MCP Java

**Status**: Phase 6 Complete | **Tests**: 426 (100% pass, 72% coverage) | **Security**: PASSED ✅ | **Updated**: 2026-02-05

**Tech Stack**: Java 25, Spring Boot 4.0.1, Spring AI 2.0.0-M2, Spring Security 7, WebFlux, Redis, Docker

**Capabilities**: MCP server with enterprise security (JWT+API keys), real-time streaming (SSE), conversational AI

## Quick Reference

**Build/Test**: `mvn clean install` | `mvn test jacoco:report`  
**Run**: `export JWT_SECRET=<64-char-min>; mvn spring-boot:run`  
**Docker**: `docker compose up --build`

**Gen API Key**: `curl -X POST localhost:8888/api/security/api-keys -H "X-API-Key: admin" -d '{"description":"key","roles":["MCP_CLIENT"]}'`  
**Test Stream**: `curl -N -X POST localhost:8888/stream/chat -H "X-API-Key: key" -d '{"message":"weather?","sessionId":"test"}'`

## Architecture

**4 APIs**: REST (`/api/*`) | MCP (`/sse`) | Chat (`/api/chat/*`) | Streaming (`/stream/*`)  
**Security**: JWT (HMAC-SHA512, <50ms) + API keys (<100ms) | RBAC (PUBLIC/MCP_CLIENT/ADMIN) | 10K audit events  
**Streaming**: Weather <2s | Chat <100ms tokens | 100+ concurrent | 4-step progress  
**MCP**: 11 tools | 4 resources | 3 prompts

## Package Structure

```
com.openmeteo.mcp/
├── security/        # JWT, API keys, RBAC
├── controller/      # REST + Streaming
├── service/         # Business + Streaming
├── tool/            # @McpTool (11 tools)
├── chat/            # ChatHandler (Spring AI)
├── client/          # Open-Meteo API
└── model/           # Records + Stream models
```

## Configuration

**Required**: `JWT_SECRET=<64-char-min>` | AI provider keys (AZURE_OPENAI_KEY/OPENAI_API_KEY/ANTHROPIC_API_KEY)  
**Optional**: `SECURITY_CORS_ALLOWED_ORIGINS=http://localhost:3000`

```yaml
security.jwt.secret: ${JWT_SECRET}
security.jwt.access-token-expiration: 86400000  # 24h
openmeteo.chat.enabled: true
openmeteo.chat.memory.type: redis  # or inmemory
streaming.chat.token-delay-ms: 50
```

## Endpoints

**Core**: App (8888) | MCP (/sse) | Chat (/api/chat) | Health (/actuator/health)  
**Security**: Auth (/api/security/auth/login) | Keys (/api/security/api-keys) | Audit (/api/security/audit/events)  
**Streaming**: Weather (/stream/weather) | Chat (/stream/chat) | Progress (/stream/chat/progress) | Context (/stream/chat/context)

**Auth**: All protected endpoints require `X-API-Key` or `Authorization: Bearer <token>` header

## MCP Tools (11)

search_location | get_weather | get_snow_conditions | get_air_quality | get_weather_alerts | get_comfort_index | get_astronomy | search_location_swiss | compare_locations | get_historical_weather | get_marine_conditions

## Development Patterns

**DTOs**: Java Records (immutable, type-safe)  
**Async**: CompletableFuture (non-streaming) | Flux/Mono (streaming SSE)  
**MCP**: @McpTool/@McpResource/@McpPrompt | snake_case names  
**Security**: @PreAuthorize for RBAC  
**Testing**: Mockito + JUnit 5 | >=80% coverage target

## Key Reminders

1. **JWT_SECRET required** (64-char min) 2. **Java Records** for DTOs 3. **Flux/Mono** for streaming, **CompletableFuture** for non-streaming 4. **@PreAuthorize** for RBAC 5. **snake_case** for MCP tools 6. **>=80% coverage** target

## Troubleshooting

**Clean build**: `mvn clean install -U` | **Skip tests**: `mvn clean install -DskipTests` | **Specific test**: `mvn test -Dtest=TestName -X`

## Phase Status

| Phase | Status | Commit | Details |
|-------|--------|--------|---------|
| 1 | ✅ | 98848aa | Security Foundation (JWT, API keys) |
| 2 | ✅ | 411fc2c | Security Integration (RBAC, audit) |
| 3 | ✅ | d25315c | Streaming Infrastructure (SSE, reactive) |
| 4 | ✅ | 70cb82b | Weather Streaming (<2s) |
| 5 | ✅ | 369fe66 | Chat Streaming (<100ms tokens) |
| 6 | ✅ | 7baf838 | Integration & Testing (PASSED) |

**Benchmarks** (All ✅): JWT <50ms | API Key <100ms | Weather <2s | Chat 50ms | 100+ concurrent | <2GB memory

**Docs**: [ARCHITECTURE.md](ARCHITECTURE.md) | [MCP_DOCUMENTATION.md](docs/MCP_DOCUMENTATION.md) | [README.md](README.md) | [ADR_COMPENDIUM.md](spec/ADR_COMPENDIUM.md)  
**Links**: [Spring AI](https://docs.spring.io/spring-ai/reference/) | [Open-Meteo](https://open-meteo.com/en/docs) | [MCP](https://modelcontextprotocol.io/)
