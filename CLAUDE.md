# CLAUDE.md

AI development guide for the Open-Meteo MCP Java project.

## Project Overview

**Open Meteo MCP (Java)** - Model Context Protocol server providing weather, snow conditions, and air quality data via [Open-Meteo API](https://open-meteo.com/) with conversational AI capabilities.

**Status**: v2.0.1 - Enterprise Ready
**Updated**: February 3, 2026
**Test Coverage**: 426 tests (100% pass, 72% coverage)

## Key Technologies

- Java 25, Spring Boot 4.0, Spring AI 2.0
- Docker (Eclipse Temurin), Redis, Maven 3.9+
- Azure OpenAI/OpenAI/Anthropic Claude

## Quick Commands

```bash
# Build & Test
mvn clean install
mvn test jacoco:report

# Run Application
mvn spring-boot:run
java -jar target/open-meteo-mcp-2.0.1.jar

# Docker
docker compose up --build
docker compose down
```

## Architecture

**Three API Endpoints:**

- **REST API** - `/api/*` - Direct HTTP endpoints
- **MCP API** - `/sse` - Model Context Protocol (Claude Desktop)
- **Chat API** - `/api/chat/*` - Conversational interface

**11 MCP Tools**: `meteo__*` (weather, snow, air quality, location, alerts, etc.)
**4 Resources**: weather codes, parameters, AQI reference, Swiss locations
**3 Prompts**: ski-trip, outdoor-activity, travel planning

## Package Structure

```
com.openmeteo.mcp/
├── OpenMeteoMcpApplication.java
├── config/          # Spring configuration
├── tool/            # @McpTool services
├── service/         # Business logic
├── chat/            # ChatHandler (Spring AI 2.0)
├── client/          # Open-Meteo API client
├── model/           # Java Records (DTOs)
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
- **snake_case** for MCP tool names (`meteo__*`)
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
AZURE_OPENAI_KEY=your_key
OPENAI_API_KEY=your_key
ANTHROPIC_API_KEY=your_key
```

**Spring Profile** (application.yml):

```yaml
openmeteo:
  chat:
    enabled: true
    memory:
      type: redis # or inmemory
```

## MCP Components

### Tools (11 total)

| Tool | Description |
|------|-------------|
| `meteo__search_location` | Geocoding - search locations by name |
| `meteo__get_weather` | Weather forecast with temperature, precipitation |
| `meteo__get_snow_conditions` | Snow depth, snowfall, mountain weather |
| `meteo__get_air_quality` | AQI, pollutants, UV index, pollen |
| `meteo__get_weather_alerts` | Weather alerts based on thresholds |
| `meteo__get_comfort_index` | Outdoor activity comfort score (0-100) |
| `meteo__get_astronomy` | Sunrise, sunset, golden hour, moon phase |
| `meteo__search_location_swiss` | Swiss-specific location search |
| `meteo__compare_locations` | Multi-location weather comparison |
| `meteo__get_historical_weather` | Historical weather data (1940-present) |
| `meteo__get_marine_conditions` | Wave/swell data for lakes and coasts |

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
| `meteo__ski-trip-weather` | Ski trip planning with snow conditions |
| `meteo__plan-outdoor-activity` | Weather-aware activity planning |
| `meteo__weather-aware-travel` | Travel planning with weather integration |

## Endpoints

- **App**: http://localhost:8888
- **MCP**: http://localhost:8888/sse
- **Chat**: http://localhost:8888/api/chat
- **Health**: http://localhost:8888/actuator/health

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

1. **Java Records** for all DTOs (ADR-002)
2. **CompletableFuture** not Mono/Flux for async (ADR-001)
3. **snake_case** for MCP tool names (ADR-007)
4. **Document before coding** - Specification-Driven Development (ADR-005)
5. **>=80% test coverage** target (ADR-010)
6. **Structured JSON logging** with SLF4J (ADR-008)

## Quick Links

- **Spring AI**: https://docs.spring.io/spring-ai/reference/
- **Open-Meteo**: https://open-meteo.com/en/docs
- **MCP Protocol**: https://modelcontextprotocol.io/

---

**v2.0.1**: Enterprise Ready - Complete documentation, Docker infrastructure, production deployment ready
