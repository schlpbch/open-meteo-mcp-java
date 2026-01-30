# Open Meteo MCP - Python to Java Migration Guide

**Version**: 1.0.0
**Date**: January 30, 2026
**Status**: Active Migration
**Target Release**: Q2 2026 (v1.0.0)

---

## Overview

This guide provides a practical roadmap for migrating **Open Meteo MCP** from Python/FastMCP v3.2.0 to Java with Spring Boot 3.5 and Spring AI 2.0.

**Key Documents**:
- **[CONSTITUTION.md](./CONSTITUTION.md)** - Complete migration strategy (Section 15)
- **[ADR_COMPENDIUM.md](./ADR_COMPENDIUM.md)** - 15 architecture decisions
- **Python Reference**: `c:\Users\schlp\code\open-meteo-mcp`

---

## Quick Start

### Migration Philosophy

1. **Feature parity first** - Match Python v3.2.0 exactly
2. **Enhance second** - Add Spring AI capabilities after core works
3. **Test continuously** - Maintain 80%+ coverage
4. **Document everything** - ADR-driven development

### Timeline: 9 Weeks (6 Phases)

| Phase | Duration | Focus | Status |
|-------|----------|-------|--------|
| Phase 1 | Weeks 1-2 | Foundation: Project setup, API client, models | 🔄 In Progress |
| Phase 2 | Weeks 3-4 | Core Tools: 4 MCP tools with @McpTool | ⏳ Pending |
| Phase 3 | Week 5 | Resources & Prompts: @McpResource, @McpPrompt | ⏳ Pending |
| Phase 4 | Week 6 | AI Enhancement: Spring AI ChatClient | ⏳ Pending |
| Phase 5 | Weeks 7-8 | Testing & Documentation | ⏳ Pending |
| Phase 6 | Week 9 | Deployment & Release | ⏳ Pending |

---

## Technology Mapping

| Python (v3.2.0) | Java (v1.0.0) | Migration Notes |
|-----------------|---------------|-----------------|
| **FastMCP** | **Spring AI 2.0 MCP annotations** | Use `@McpTool`, `@McpResource`, `@McpPrompt` - no custom protocol needed |
| **httpx** | Spring WebClient + gzip | Async HTTP with compression (70-80% reduction) |
| **Pydantic** | Java Records | Immutable, type-safe DTOs with validation |
| **structlog** | SLF4J + Logback | Structured JSON logging |
| **pytest** | JUnit 5 + AssertJ | Comprehensive test suite with Mockito |
| **uv** | Maven/Gradle | Dependency management |
| **async/await** | CompletableFuture + Virtual Threads | Java 21 for efficient concurrency |

### MCP Annotation Mapping

```
Python                  →  Java
──────────────────────────────────────
@mcp.tool()            →  @McpTool
@mcp.resource()        →  @McpResource
@mcp.prompt()          →  @McpPrompt
```

---

## Phase-by-Phase Guide

### Phase 1: Foundation (Weeks 1-2)

**Goal**: Set up project structure and core infrastructure

**Checklist**:
- [x] Create Maven project with Spring Boot 3.5
- [x] Add dependencies: Spring Boot, Spring AI 2.0, WebFlux, Jackson
- [x] Implement `OpenMeteoClient.java` with gzip compression
- [x] Create Java Records 
- [x] Set up JUnit 5 + Mockito + AssertJ test infrastructure
- [x] Copy `data/*.json` files to `src/main/resources/data/`

**Key ADRs**: [ADR-001](./ADR_COMPENDIUM.md#adr-001), [ADR-002](./ADR_COMPENDIUM.md#adr-002), [ADR-003](./ADR_COMPENDIUM.md#adr-003)

**Example**: OpenMeteoClient.java
```java
@Component
public class OpenMeteoClient {
    private final WebClient webClient;

    public CompletableFuture<WeatherResponse> getWeather(double lat, double lon) {
        return webClient.get()
            .uri("/v1/forecast?latitude={lat}&longitude={lon}", lat, lon)
            .retrieve()
            .bodyToMono(WeatherResponse.class)
            .toFuture();
    }
}
```

---

### Phase 2: Core Tools (Weeks 3-4)

**Goal**: Implement all 4 MCP tools using Spring AI annotations

**Checklist**:
- [ ] Create `LocationToolService` with `@McpTool` for `search_location`
- [ ] Create `WeatherToolService` with `@McpTool` for `get_weather`
- [ ] Create `SnowToolService` with `@McpTool` for `get_snow_conditions`
- [ ] Create `AirQualityToolService` with `@McpTool` for `get_air_quality`
- [ ] Port weather code interpretation utilities
- [ ] Port AQI interpretation utilities
- [ ] Add `@McpParam` annotations with descriptions
- [ ] Write unit tests for each tool (≥80% coverage)

**Key ADRs**: [ADR-004](./ADR_COMPENDIUM.md#adr-004), [ADR-007](./ADR_COMPENDIUM.md#adr-007), [ADR-011](./ADR_COMPENDIUM.md#adr-011)

**Example**: WeatherToolService.java
```java
@Service
public class WeatherToolService {
    private final WeatherService weatherService;

    @McpTool(
        name = "get_weather",
        description = "Get weather forecast with temperature, precipitation, wind, UV index"
    )
    public CompletableFuture<WeatherResponse> getWeather(
        @McpParam(value = "latitude", description = "Latitude in decimal degrees", required = true)
        double latitude,

        @McpParam(value = "longitude", description = "Longitude in decimal degrees", required = true)
        double longitude,

        @McpParam(value = "forecast_days", description = "Number of forecast days (1-16)", required = false)
        Optional<Integer> forecastDays
    ) {
        return weatherService.getWeather(latitude, longitude, forecastDays.orElse(7));
    }
}
```

---

### Phase 3: Resources & Prompts (Week 5)

**Goal**: Implement MCP resources and workflow prompts

**Checklist**:
- [ ] Create `WeatherResourceService` with `@McpResource` methods
- [ ] Implement 5 resources: codes, ski-resorts, swiss-locations, aqi-reference, parameters
- [ ] Create `WeatherPromptService` with `@McpPrompt` methods
- [ ] Implement 3 prompts: ski-trip-weather, plan-outdoor-activity, weather-aware-travel
- [ ] Test with MCP Inspector
- [ ] Write tests for resources and prompts

**Key ADRs**: [ADR-004](./ADR_COMPENDIUM.md#adr-004), [ADR-012](./ADR_COMPENDIUM.md#adr-012)

**Example**: WeatherResourceService.java
```java
@Service
public class WeatherResourceService {

    @McpResource(
        uri = "weather://codes",
        name = "Weather Codes",
        description = "WMO weather code interpretations"
    )
    public String getWeatherCodes() {
        return loadJsonResource("data/weather-codes.json");
    }
}
```

---

### Phase 4: AI Enhancement (Week 6)

**Goal**: Add Spring AI 2.0 ChatClient for LLM-powered features

**Checklist**:
- [ ] Configure Spring AI 2.0 with Anthropic/OpenAI
- [ ] Create `InterpretationService` with ChatClient
- [ ] Implement weather condition interpretation
- [ ] Add natural language query processing
- [ ] Generate structured recommendations
- [ ] Integrate AI with MCP tools (optional enhancement)
- [ ] Write tests for interpretation service

**Key ADRs**: [ADR-004](./ADR_COMPENDIUM.md#adr-004)

**Configuration**: application.yml
```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      chat:
        model: claude-sonnet-4-5-20250929
    mcp:
      enabled: true
      server:
        name: open-meteo
        version: 1.0.0
```

---

### Phase 5: Testing & Documentation (Weeks 7-8)

**Goal**: Complete testing and comprehensive documentation

**Checklist**:
- [ ] Achieve ≥80% test coverage across all layers
- [ ] Write API documentation with examples
- [ ] Create detailed migration notes
- [ ] Performance benchmarking vs Python version
- [ ] Write CLAUDE.md for Java project
- [ ] Update all spec documents
- [ ] Generate test coverage report

**Key ADRs**: [ADR-010](./ADR_COMPENDIUM.md#adr-010)

---

### Phase 6: Deployment (Week 9)

**Goal**: Production release and deployment

**Checklist**:
- [ ] Set up CI/CD pipeline (GitHub Actions)
- [ ] Configure cloud deployment (cloud-agnostic)
- [ ] Integration test with swiss-mobility-mcp
- [ ] Load testing (target: 100 req/s)
- [ ] Security audit
- [ ] Release v1.0.0
- [ ] Verify deployment

---

## Project Structure

### Spring Boot Package Layout

```
com.openmeteo.mcp/
├── OpenMeteoMcpApplication.java   # Main @SpringBootApplication
├── config/
│   ├── WebClientConfig.java       # HTTP client with gzip
│   ├── SpringAiConfig.java        # Spring AI setup (ADR-004)
│   └── McpServerConfig.java       # MCP server config
├── tool/                          # MCP Tool Layer
│   ├── WeatherToolService.java   # @McpTool methods
│   ├── SnowToolService.java
│   ├── AirQualityToolService.java
│   ├── LocationToolService.java
│   ├── WeatherResourceService.java  # @McpResource methods
│   └── WeatherPromptService.java    # @McpPrompt methods
├── service/                       # Business Logic Layer
│   ├── WeatherService.java
│   ├── LocationService.java
│   ├── SnowConditionsService.java
│   ├── AirQualityService.java
│   └── InterpretationService.java   # Spring AI ChatClient
├── client/                        # External API Layer
│   ├── OpenMeteoClient.java
│   └── OpenMeteoClientConfig.java
├── model/
│   ├── dto/                      # Java Records
│   │   ├── WeatherResponse.java
│   │   ├── SnowConditionsResponse.java
│   │   └── AirQualityResponse.java
│   ├── request/
│   │   ├── WeatherRequest.java
│   │   └── SnowConditionsRequest.java
│   └── mcp/
│       ├── McpToolRequest.java
│       └── McpToolResponse.java
├── exception/
│   ├── OpenMeteoException.java
│   ├── McpException.java
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice
└── util/
    ├── WeatherCodeInterpreter.java
    ├── AqiInterpreter.java
    └── JsonSerializationUtil.java
```

---

## Code Migration Examples

### 1. Python Tool → Java @McpTool

**Python (server.py)**:
```python
@mcp.tool()
async def search_location(name: str, count: int = 10):
    results = await client.search_location(name, count)
    return results
```

**Java (LocationToolService.java)**:
```java
@Service
public class LocationToolService {
    private final OpenMeteoClient openMeteoClient;

    @McpTool(
        name = "search_location",
        description = "Search for locations by name to get coordinates"
    )
    public CompletableFuture<LocationSearchResponse> searchLocation(
        @McpParam(value = "name", description = "Location name to search", required = true)
        String name,

        @McpParam(value = "count", description = "Number of results (1-100)", required = false)
        Optional<Integer> count
    ) {
        return openMeteoClient.searchLocation(name, count.orElse(10));
    }
}
```

### 2. Pydantic Model → Java Record

**Python (models.py)**:
```python
class WeatherResponse(JsonSerializableMixin):
    temperature: float
    precipitation: float
    weather_code: int
    timestamp: datetime
```

**Java (WeatherResponse.java)**:
```java
public record WeatherResponse(
    double temperature,
    double precipitation,
    int weatherCode,
    @JsonProperty("timestamp") Instant timestamp
) implements Serializable {
    // Compact constructor for validation
    public WeatherResponse {
        if (temperature < -273.15) {
            throw new IllegalArgumentException("Invalid temperature");
        }
    }
}
```

### 3. Async HTTP Client

**Python (client.py)**:
```python
async def get_weather(self, lat: float, lon: float):
    async with self.client.get(url, params=params) as response:
        return WeatherResponse(**response.json())
```

**Java (OpenMeteoClient.java)**:
```java
public CompletableFuture<WeatherResponse> getWeather(double lat, double lon) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/v1/forecast")
            .queryParam("latitude", lat)
            .queryParam("longitude", lon)
            .build())
        .retrieve()
        .bodyToMono(WeatherResponse.class)
        .toFuture();  // Convert to CompletableFuture
}
```

---

## Key Architecture Decisions

### Core ADRs (Must Implement)

1. **[ADR-004: Spring AI 2.0 MCP Annotations](./ADR_COMPENDIUM.md#adr-004)** ⚡ **CRITICAL**
   - Use `@McpTool`, `@McpResource`, `@McpPrompt` - no custom MCP protocol
   - Spring AI handles all MCP JSON-RPC communication

2. **[ADR-002: Java Records](./ADR_COMPENDIUM.md#adr-002)**
   - All DTOs, requests, responses as Java Records
   - Immutable, type-safe, concise

3. **[ADR-001: CompletableFuture](./ADR_COMPENDIUM.md#adr-001)**
   - No reactive programming (Mono/Flux)
   - Use CompletableFuture + Virtual Threads

4. **[ADR-003: Spring Boot Layered Architecture](./ADR_COMPENDIUM.md#adr-003)**
   - Standard Spring patterns
   - Constructor injection, separation of concerns

5. **[ADR-007: Snake_case Tool Names](./ADR_COMPENDIUM.md#adr-007)**
   - `search_location`, `get_weather` (not camelCase)
   - Consistency with MCP ecosystem

---

## Success Criteria

### Feature Parity
- ✅ All 4 tools match Python functionality
- ✅ All 5 resources available
- ✅ All 3 prompts working
- ✅ Gzip compression implemented
- ✅ JSON optimization equivalent or better

### Performance
- 📊 Response time ≤ Python version (target: <500ms p95)
- 📊 Memory usage <512MB
- 📊 Throughput ≥ 100 req/s

### Quality
- 🧪 Test coverage ≥80%
- 📝 Complete API documentation
- 🔍 Zero critical bugs
- ✅ All 15 ADRs reviewed and compliant

### AI Enhancement
- 🤖 Spring AI ChatClient functional
- 🤖 Weather interpretation working
- 🤖 Natural language queries supported

---

## Testing Strategy

### Test Layers

1. **Unit Tests** (JUnit 5 + Mockito)
   - Service layer: business logic
   - Client layer: API integration
   - Utility layer: helpers
   - Target: ≥85% line coverage

2. **Integration Tests** (@SpringBootTest)
   - Full application context
   - All 4 MCP tools
   - End-to-end workflows

3. **Contract Tests**
   - MCP protocol compliance
   - MCP Inspector validation
   - Resource and prompt contracts

4. **Performance Tests**
   - Benchmark vs Python version
   - Load testing (100+ req/s)
   - Memory profiling

---

## Risk Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Feature drift from Python | High | Daily comparison tests against Python v3.2.0 |
| Performance regression | Medium | Benchmark every commit, target <500ms p95 |
| MCP protocol incompatibility | High | Contract tests + MCP Inspector validation |
| Spring AI complexity | Medium | Start simple, iterate gradually |
| JVM memory overhead | Low | Profile regularly, optimize if needed |
| Test coverage gaps | Medium | Require tests with every PR |

---

## Dependencies

### Maven Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
        <version>3.5.0</version>
    </dependency>

    <!-- Spring AI 2.0 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-anthropic</artifactId>
        <version>2.0.0</version>
    </dependency>

    <!-- Logging -->
    <dependency>
        <groupId>net.logstash.logback</groupId>
        <artifactId>logstash-logback-encoder</artifactId>
        <version>7.4</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## Tools & Commands

### Build & Test
```bash
# Build project
mvn clean install

# Run tests
mvn test

# Run with coverage
mvn test jacoco:report

# Run application
mvn spring-boot:run

# Package
mvn package
```

### MCP Inspector
```bash
# Test MCP server locally
npx @modelcontextprotocol/inspector java -jar target/open-meteo-mcp-1.0.0.jar
```

---

## Resources

### Documentation
- **[CONSTITUTION.md](./CONSTITUTION.md)** - Full migration strategy (Section 15)
- **[ADR_COMPENDIUM.md](./ADR_COMPENDIUM.md)** - 15 architecture decisions
- **[Python Reference](c:\Users\schlp\code\open-meteo-mcp)** - Python v3.2.0 source code

### External References
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring AI**: https://spring.io/projects/spring-ai
- **MCP Protocol**: https://modelcontextprotocol.io/
- **Open-Meteo API**: https://open-meteo.com/

---

## Quick Reference

### MCP Tools to Implement
1. `search_location` - Geocoding and location search
2. `get_weather` - Weather forecasts
3. `get_snow_conditions` - Snow depth and mountain weather
4. `get_air_quality` - AQI, pollutants, pollen

### MCP Resources to Implement
1. `weather://codes` - WMO weather codes
2. `weather://ski-resorts` - Ski resort coordinates
3. `weather://swiss-locations` - Swiss locations
4. `weather://aqi-reference` - AQI reference
5. `weather://parameters` - Weather parameters

### MCP Prompts to Implement
1. `ski-trip-weather` - Ski trip planning
2. `plan-outdoor-activity` - Outdoor activity planning
3. `weather-aware-travel` - Travel planning

---

**Last Updated**: January 30, 2026
**Migration Status**: Phase 1 - Foundation (In Progress)
**Target Release**: Q2 2026 (v1.0.0)
**Questions?** See [CONSTITUTION.md](./CONSTITUTION.md) or open an issue
