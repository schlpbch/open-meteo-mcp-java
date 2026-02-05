# CLAUDE.md

AI development guide for the Open-Meteo MCP Java project.

## Project Overview

**Open Meteo MCP (Java)** - Model Context Protocol server providing weather, snow conditions, and air quality data via [Open-Meteo API](https://open-meteo.com/) with **enterprise security**, **real-time streaming**, and conversational AI capabilities.

**Status**: Phase 6 Complete - Production Ready with Security & Streaming
**Updated**: February 5, 2026
**Test Coverage**: 426 tests (100% pass, 72% coverage)
**Security Audit**: PASSED (zero critical vulnerabilities)

## Key Technologies

- **Core**: Java 25, Spring Boot 4.0.1, Spring AI 2.0.0-M2
- **Security**: Spring Security 7, JJWT 0.11.5 (JWT), OAuth2 Resource Server
- **Streaming**: Spring WebFlux, Project Reactor, Server-Sent Events (SSE)
- **Infrastructure**: Docker (Eclipse Temurin), Redis, Maven 3.9+
- **AI Providers**: Azure OpenAI/OpenAI/Anthropic Claude

## Quick Commands

```bash
# Build & Test
mvn clean install
mvn test jacoco:report

# Run Application (requires JWT_SECRET)
export JWT_SECRET=your-secure-64-character-minimum-jwt-secret-key-here
mvn spring-boot:run
java -jar target/open-meteo-mcp-2.0.2.jar

# Generate API Key (requires authentication)
curl -X POST http://localhost:8888/api/security/api-keys \
  -H "Content-Type: application/json" \
  -H "X-API-Key: admin-key" \
  -d '{"description": "My API key", "roles": ["MCP_CLIENT"]}'

# Test Streaming
curl -N -X POST http://localhost:8888/stream/chat \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-key" \
  -d '{"message": "What is the weather?", "sessionId": "test"}'

# Docker
docker compose up --build
docker compose down
```

## Architecture

**Four API Endpoints:**

- **REST API** - `/api/*` - Direct HTTP endpoints
- **MCP API** - `/sse` - Model Context Protocol (Claude Desktop)
- **Chat API** - `/api/chat/*` - Conversational interface
- **Streaming API** - `/stream/*` - Real-time SSE (weather + chat)

**Security Layer (Phase 1-2):**

- **Authentication**: JWT tokens (HMAC-SHA512) + API keys
- **Authorization**: Role-based (PUBLIC, MCP_CLIENT, ADMIN)
- **Audit Logging**: 10,000 security events retained
- **Performance**: <50ms JWT, <100ms API key validation

**Streaming Infrastructure (Phase 3-5):**

- **Weather Streaming**: <2s first chunk latency
- **Chat Streaming**: <100ms token-by-token delivery
- **Connection Management**: 100+ concurrent streams
- **Progress Indicators**: 4-step tracking (25%, 50%, 75%, 100%)

**11 MCP Tools**: `*` (weather, snow, air quality, location, alerts, etc.)
**4 Resources**: weather codes, parameters, AQI reference, Swiss locations
**3 Prompts**: ski-trip, outdoor-activity, travel planning

## Package Structure

```
com.openmeteo.mcp/
├── OpenMeteoMcpApplication.java
├── config/          # Spring configuration + Security config
├── security/        # JWT, API keys, RBAC (Phase 1-2)
├── controller/      # REST + Streaming controllers
├── service/         # Business logic + Streaming services
├── tool/            # @McpTool services
├── chat/            # ChatHandler (Spring AI 2.0)
├── client/          # Open-Meteo API client
├── model/           # Java Records (DTOs) + Stream models
└── exception/       # Error handling
```

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - System design
- [docs/MCP_DOCUMENTATION.md](docs/MCP_DOCUMENTATION.md) - MCP protocol reference
- [docs/openapi-open-meteo.yaml](docs/openapi-open-meteo.yaml) - REST API spec
- [docs/openapi-chat.yaml](docs/openapi-chat.yaml) - Chat API spec
- [README.md](README.md) - User guide
- [spec/CONSTITUTION.md](spec/CONSTITUTION.md) - Governance
- [spec/ADR_COMPENDIUM.md](spec/ADR_COMPENDIUM.md) - Architecture decisions

## Development Guidelines

### Core Patterns

- **Java Records** for all DTOs (immutable, type-safe)
- **CompletableFuture** for async (no reactive Mono/Flux)
- **@McpTool/@McpResource/@McpPrompt** annotations
- **snake_case** for MCP tool names (`*`)
- **>=80% test coverage** target

### New MCP Tool Example

```java
@Service
public class MyToolService {
    @McpTool(description = "Tool description with examples")
    public CompletableFuture<MyResponse> myTool(
        @McpParam("param1") String param1,
        @McpParam("param2") Optional<Integer> param2
    ) {
        return myService.performOperation(param1, param2.orElse(10));
    }
}
```

### Testing Pattern

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock MyDependency dependency;
    @InjectMocks MyService service;

    @Test
    void shouldDoSomething() {
        // Arrange, Act, Assert
    }
}
```

## Configuration

**Environment** (.env.example):

```bash
# Security (REQUIRED - Phase 1-2)
JWT_SECRET=your-secure-jwt-secret-minimum-64-characters-required

# AI Providers
AZURE_OPENAI_KEY=your_key
OPENAI_API_KEY=your_key
ANTHROPIC_API_KEY=your_key

# CORS (optional)
SECURITY_CORS_ALLOWED_ORIGINS=http://localhost:3000
```

**Spring Profile** (application.yml):

```yaml
# Security (Phase 1-2)
security:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiration: 86400000  # 24h
  cors:
    allowed-origins: ${SECURITY_CORS_ALLOWED_ORIGINS:http://localhost:*}

# Chat
openmeteo:
  chat:
    enabled: true
    memory:
      type: redis # or inmemory

# Streaming (Phase 5)
streaming:
  chat:
    token-delay-ms: 50
    max-tokens-per-chunk: 10
```

## MCP Components

### Tools (11 total)

| Tool | Description |
|------|-------------|
| `search_location` | Geocoding - search locations by name |
| `get_weather` | Weather forecast with temperature, precipitation |
| `get_snow_conditions` | Snow depth, snowfall, mountain weather |
| `get_air_quality` | AQI, pollutants, UV index, pollen |
| `get_weather_alerts` | Weather alerts based on thresholds |
| `get_comfort_index` | Outdoor activity comfort score (0-100) |
| `get_astronomy` | Sunrise, sunset, golden hour, moon phase |
| `search_location_swiss` | Swiss-specific location search |
| `compare_locations` | Multi-location weather comparison |
| `get_historical_weather` | Historical weather data (1940-present) |
| `get_marine_conditions` | Wave/swell data for lakes and coasts |

### Resources (4 total)

| URI | Description |
|-----|-------------|
| `weather://codes` | WMO weather code reference |
| `weather://aqi-reference` | AQI scales and health recommendations |
| `weather://swiss-locations` | Swiss cities, mountains, passes |
| `weather://parameters` | Available weather parameters |

### Prompts (3 total)

| Prompt | Description |
|--------|-------------|
| `ski-trip-weather` | Ski trip planning with snow conditions |
| `plan-outdoor-activity` | Weather-aware activity planning |
| `weather-aware-travel` | Travel planning with weather integration |

## Endpoints

**Core:**
- **App**: http://localhost:8888
- **MCP**: http://localhost:8888/sse
- **Chat**: http://localhost:8888/api/chat
- **Health**: http://localhost:8888/actuator/health

**Security (Phase 1-2):**
- **Auth**: http://localhost:8888/api/security/auth/login
- **API Keys**: http://localhost:8888/api/security/api-keys
- **Audit**: http://localhost:8888/api/security/audit/events

**Streaming (Phase 3-5):**
- **Weather**: http://localhost:8888/stream/weather
- **Chat Simple**: http://localhost:8888/stream/chat
- **Chat Progress**: http://localhost:8888/stream/chat/progress
- **Chat Context**: http://localhost:8888/stream/chat/context

**Authentication Required**: All protected endpoints require `X-API-Key` header or `Authorization: Bearer <token>` header.

## Troubleshooting

```bash
# Clean rebuild
mvn clean install -U

# Skip tests
mvn clean install -DskipTests

# Run specific test
mvn test -Dtest=WeatherServiceTest -X

# Check test reports
cat target/surefire-reports/*.txt
```

## Important Reminders

1. **JWT_SECRET required** - Set environment variable (minimum 64 characters)
2. **Java Records** for all DTOs (ADR-002)
3. **Flux/Mono for streaming** - Use reactive types for SSE (Phase 3-5)
4. **CompletableFuture** for non-streaming async (ADR-001)
5. **snake_case** for MCP tool names (ADR-007)
6. **@PreAuthorize** for protected endpoints - RBAC enforcement
7. **Document before coding** - Specification-Driven Development (ADR-005)
8. **>=80% test coverage** target (ADR-010)
9. **Structured JSON logging** with SLF4J (ADR-008)
10. **Security audit logging** - All auth events tracked

## Quick Links

- **Spring AI**: https://docs.spring.io/spring-ai/reference/
- **Open-Meteo**: https://open-meteo.com/en/docs
- **MCP Protocol**: https://modelcontextprotocol.io/

---

## Phase Implementation Status

| Phase | Description | Status | Commit |
|-------|-------------|--------|--------|
| Phase 1 | Security Foundation | ✅ Complete | 98848aa |
| Phase 2 | Security Integration | ✅ Complete | 411fc2c |
| Phase 3 | Streaming Infrastructure | ✅ Complete | d25315c |
| Phase 4 | Weather Streaming | ✅ Complete | 70cb82b |
| Phase 5 | Chat Streaming | ✅ Complete | 369fe66 |
| Phase 6 | Integration & Testing | ✅ Complete | 7baf838 |

**Performance Benchmarks (All Targets Met ✅):**
- JWT Authentication: <50ms
- API Key Authentication: <100ms
- Weather First Chunk: <2s
- Chat Token Delay: 50ms
- Concurrent Connections: 100+
- Memory Usage: <2GB

---

**Phase 6 Complete**: Enterprise security, real-time streaming, zero critical vulnerabilities, production deployment ready
