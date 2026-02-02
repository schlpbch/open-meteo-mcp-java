# Open Meteo MCP - Architecture Decision Records (ADR) Compendium

**Document Version**: 3.2.0 **Last Updated**: 2026-02-02 **Total ADRs**: 17 (10
Accepted, 7 Proposed)

**Related Documents**:

- [CONSTITUTION.md](./CONSTITUTION.md) - Project governance and principles

---

## Status Legend

- ✅ **Accepted** - Currently in use and actively maintained
- 🔄 **Proposed** - Under consideration, not yet implemented
- ⛔ **Superseded** - Replaced by another ADR (see cross-reference)
- 🗑️ **Deprecated** - No longer applicable, kept for historical context

---

## Quick Reference by Category

### Core Architecture

- [ADR-001: Use Standard Java with CompletableFuture for Async Operations](#adr-001-use-standard-java-with-completablefuture-for-async-operations)
  ✅
- [ADR-002: Use Java Records for All Data Models](#adr-002-use-java-records-for-all-data-models)
  ✅
- [ADR-003: Follow Spring Boot Best Practices and Layered Architecture](#adr-003-follow-spring-boot-best-practices-and-layered-architecture)
  ⛔
- [ADR-004: Use Spring AI 2.0 for Weather Interpretation](#adr-004-use-spring-ai-20-for-weather-interpretation)
  🔄
- [ADR-016: Adopt Java 25 LTS](#adr-016-adopt-java-25-lts) 🔄
- [ADR-017: Adopt Spring Boot 5](#adr-017-adopt-spring-boot-5) 🔄

### Development Standards

- [ADR-005: Specification-Driven Development](#adr-005-specification-driven-development)
  ✅
- [ADR-006: Use Semantic Versioning (SemVer)](#adr-006-use-semantic-versioning-semver)
  ✅
- [ADR-007: MCP Tool Naming Convention](#adr-007-mcp-tool-naming-convention) ✅

### Quality & Observability

- [ADR-008: Structured JSON Logging with SLF4J](#adr-008-structured-json-logging-with-slf4j)
  ✅
- [ADR-009: Micrometer for Observability](#adr-009-micrometer-for-observability)
  ✅
- [ADR-010: Test Strategy with 80%+ Coverage](#adr-010-test-strategy-with-80-coverage)
  🔄

### Integration & APIs

- [ADR-011: MCP Protocol Implementation](#adr-011-mcp-protocol-implementation)
  ✅
- [ADR-012: MCP Resources and Prompts Strategy](#adr-012-mcp-resources-and-prompts-strategy)
  ✅
- [ADR-013: Open-Meteo API Client with Gzip Compression](#adr-013-open-meteo-api-client-with-gzip-compression)
  🔄

### Security & Privacy

- [ADR-014: Privacy-First Weather Data Handling](#adr-014-privacy-first-weather-data-handling)
  🔄
- [ADR-015: API Versioning and Backward Compatibility](#adr-015-api-versioning-and-backward-compatibility)
  🔄

---

## ADR-001: Use Standard Java with CompletableFuture for Async Operations

**Status**: ✅ Accepted **Date**: 2026-01-30 **Deciders**: Architecture Team
**Context**: Core Architecture

### [ADR-001] Decision

Use **standard Java with CompletableFuture and Virtual Threads** for async
operations instead of Project Reactor (Mono/Flux).

**Rationale**:

- Simpler programming model for async operations
- Java 25 Virtual Threads provide excellent concurrency with enhanced
  performance
- Easier testing without reactive test libraries
- Lower learning curve for Java developers
- No need for reactive complexity in stateless weather API

**Example**:

```java
public CompletableFuture<WeatherResponse> getWeather(double lat, double lon) {
    return webClient.get()
        .uri("/v1/forecast?latitude={lat}&longitude={lon}", lat, lon)
        .retrieve()
        .bodyToMono(WeatherResponse.class)
        .toFuture();  // Convert to CompletableFuture
}
```

### [ADR-001] Related ADRs

- [ADR-003](#adr-003-follow-spring-boot-best-practices-and-layered-architecture) -
  Spring Boot patterns

---

## ADR-002: Use Java Records for All Data Models

**Status**: ✅ Accepted **Date**: 2026-01-30 **Context**: Core Architecture

### [ADR-002] Decision

Use **Java Records** for all DTOs, requests, and response models.

**Benefits**:

- Immutable by default
- Concise syntax
- Built-in equals/hashCode/toString
- Pattern matching support
- JSON serialization friendly

**Example**:

```java
public record WeatherResponse(
    double temperature,
    double precipitation,
    int weatherCode,
    Instant timestamp
) {
    // Validation in compact constructor
    public WeatherResponse {
        if (temperature < -273.15) {
            throw new IllegalArgumentException("Invalid temperature");
        }
    }
}
```

---

## ADR-003: Follow Spring Boot Best Practices and Layered Architecture

**Status**: ⛔ Superseded by [ADR-017](#adr-017-adopt-spring-boot-5) **Date**:
2026-01-30 **Context**: Core Architecture

### [ADR-003] Decision

Follow **Spring Boot layered architecture** with standard package structure.

**Package Structure**: `com.openmeteo.mcp.<layer>`

- `controller/` - MCP endpoints (@RestController)
- `service/` - Business logic (@Service)
- `client/` - External API clients (@Component)
- `model/` - Java Records (dto/, request/, mcp/)
- `config/` - Configuration (@Configuration)
- `exception/` - Exception handling (@RestControllerAdvice)
- `util/` - Stateless utilities

**Key Principles**:

- Constructor injection (not field injection)
- Single Responsibility Principle per layer
- No business logic in controllers
- Type-safe configuration with @ConfigurationProperties

### [ADR-003] Related ADRs

- [ADR-002](#adr-002-use-java-records-for-all-data-models) - Java Records for
  models

---

## ADR-004: Use Spring AI 2.0 for Weather Interpretation

**Status**: 🔄 Proposed **Date**: 2026-01-30 **Context**: Core Architecture

### [ADR-004] Decision

Use **Spring AI 2.0** for:

1. **Native MCP annotations** for tool/resource/prompt definition
2. **AI-powered weather interpretation** and natural language features

**Key Features**:

- `@McpTool` annotation for MCP tool definitions
- `@McpResource` annotation for MCP resources
- `@McpPrompt` annotation for workflow prompts
- ChatClient for weather interpretation
- Built-in MCP protocol support (no custom implementation needed)

**MCP Tool Example**:

```java
@Service
public class WeatherToolService {
    private final OpenMeteoClient openMeteoClient;

    @McpTool(name = "get_weather", description = "Get weather forecast for a location")
    public CompletableFuture<WeatherResponse> getWeather(
        @McpParam("latitude") double latitude,
        @McpParam("longitude") double longitude,
        @McpParam("forecast_days") Optional<Integer> forecastDays
    ) {
        return openMeteoClient.getWeather(latitude, longitude, forecastDays.orElse(7));
    }
}
```

**AI Interpretation Example**:

```java
@Service
public class InterpretationService {
    private final ChatClient chatClient;

    public CompletableFuture<String> interpretWeather(WeatherResponse weather) {
        return CompletableFuture.supplyAsync(() ->
            chatClient.prompt()
                .user("Interpret this weather: " + weather.toString())
                .call()
                .content()
        );
    }
}
```

### [ADR-004] Configuration

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

### [ADR-004] Benefits

- **No custom MCP implementation**: Spring AI handles protocol
- **Declarative tools**: Simple annotations replace boilerplate
- **Type safety**: Annotation processor validates at compile time
- **AI integration**: Seamless ChatClient integration for interpretation

### [ADR-004] Related ADRs

- [ADR-003](#adr-003-follow-spring-boot-best-practices-and-layered-architecture) -
  Service layer patterns
- [ADR-011](#adr-011-mcp-protocol-implementation) - Uses Spring AI MCP
  annotations

---

## ADR-005: Specification-Driven Development

**Status**: ✅ Accepted **Date**: 2026-01-30 **Context**: Development Standards

### [ADR-005] Decision

**Document before coding** - all features require specification before
implementation.

**Process**:

1. Write specification (tools, resources, prompts)
2. Get team review/approval
3. Implement following spec
4. Update documentation

**Documents**:

- CONSTITUTION.md - Project governance
- ADR_COMPENDIUM.md - Architecture decisions
- API_REFERENCE.md - Tool contracts
- MIGRATION_GUIDE.md - Python → Java mapping

---

## ADR-006: Use Semantic Versioning (SemVer)

**Status**: ✅ Accepted **Date**: 2026-01-30 **Context**: Development Standards

### [ADR-006] Decision

Follow **Semantic Versioning**: MAJOR.MINOR.PATCH

- **MAJOR**: Breaking API changes (tool signatures, MCP protocol)
- **MINOR**: New features (new tools, resources, prompts)
- **PATCH**: Bug fixes, non-breaking improvements

**Current**: v1.0.0-alpha (migration phase) **Target**: v1.0.0 (Q2 2026)

---

## ADR-007: MCP Tool Naming Convention

**Status**: ✅ Accepted **Date**: 2026-01-30 **Context**: Development Standards

### [ADR-007] Decision

Use **snake_case** for MCP tool names following Python conventions.

**Pattern**: `{action}_{subject}`

**Examples**:

- `search_location` (not `searchLocation`)
- `get_weather` (not `getWeather`)
- `get_snow_conditions`
- `get_air_quality`

**Rationale**: Consistency with MCP ecosystem and Python reference
implementation

---

## ADR-008: Structured JSON Logging with SLF4J

**Status**: ✅ Accepted **Date**: 2026-01-30 **Context**: Quality &
Observability

### [ADR-008] Decision

Use **SLF4J + Logback** with structured JSON logging.

**Configuration**:

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    <root level="INFO">
        <appender-ref ref="JSON"/>
    </root>
</configuration>
```

**Usage**:

```java
@Slf4j
public class WeatherService {
    public WeatherResponse getWeather(double lat, double lon) {
        log.info("Fetching weather for lat={}, lon={}", lat, lon);
        // ...
    }
}
```

---

## ADR-009: Micrometer for Observability

**Status**: ✅ Accepted **Date**: 2026-01-30 **Context**: Quality &
Observability

### [ADR-009] Decision

Use **Micrometer** for metrics and observability.

**Key Metrics**:

```
# Weather API calls
weather.api.requests.total{tool, status}
weather.api.requests.duration{tool}

# Spring AI calls (if enabled)
ai.interpretation.requests.total
ai.interpretation.duration

# JVM metrics
jvm.memory.used
jvm.threads.live
```

**Endpoints**:

- `/actuator/metrics` - All metrics
- `/actuator/health` - Health check
- `/actuator/prometheus` - Prometheus format

---

## ADR-010: Test Strategy with 80%+ Coverage

**Status**: 🔄 Proposed **Date**: 2026-01-30 **Context**: Quality &
Observability

### [ADR-010] Decision

Maintain **≥80% test coverage** with comprehensive test strategy.

**Test Layers**:

1. **Unit Tests** (JUnit 5 + Mockito) - Service/Client layer
2. **Integration Tests** (@SpringBootTest) - Full application
3. **Contract Tests** - MCP protocol compliance
4. **Performance Tests** - Benchmark vs Python version

**Example**:

```java
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {
    @Mock OpenMeteoClient client;
    @InjectMocks WeatherService service;

    @Test
    void shouldGetWeather() {
        when(client.getWeather(anyDouble(), anyDouble()))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));

        var result = service.getWeather(47.3769, 8.5417).join();

        assertThat(result).isNotNull();
        verify(client).getWeather(47.3769, 8.5417);
    }
}
```

---

## ADR-011: MCP Protocol Implementation

**Status**: ✅ Accepted **Date**: 2026-01-30 **Context**: Integration & APIs

### [ADR-011] Decision

Use **Spring AI 2.0 MCP annotations** for tool/resource/prompt exposure to AI
agents.

**Components**:

- **Tools**: 4 weather query tools (search_location, get_weather,
  get_snow_conditions, get_air_quality)
- **Resources**: 5 reference data resources (weather codes, ski resorts,
  locations, AQI, parameters)
- **Prompts**: 3 workflow prompts (ski trip, outdoor activity, travel planning)

**Tool Example with Spring AI MCP Annotations**:

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
        Optional<Integer> forecastDays,

        @McpParam(value = "include_hourly", description = "Include hourly forecasts", required = false)
        Optional<Boolean> includeHourly
    ) {
        return weatherService.getWeather(
            latitude,
            longitude,
            forecastDays.orElse(7),
            includeHourly.orElse(true)
        );
    }
}
```

**Resource Example**:

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

**Prompt Example**:

```java
@Service
public class WeatherPromptService {

    @McpPrompt(
        name = "ski-trip-weather",
        description = "Guide for checking snow conditions and weather for ski trips"
    )
    public String skiTripWeatherPrompt(
        @McpParam("resort") String resort,
        @McpParam("dates") String dates
    ) {
        return """
            1. Use search_location to find coordinates for %s
            2. Use get_snow_conditions for snow depth and snowfall
            3. Use get_weather for temperature and wind conditions
            4. Assess suitability for skiing on %s
            """.formatted(resort, dates);
    }
}
```

**Benefits**:

- **Declarative**: Annotations replace manual protocol implementation
- **Type-safe**: Compile-time validation of MCP contracts
- **Auto-discovery**: Spring AI auto-registers annotated tools/resources/prompts
- **Protocol handling**: Spring AI manages MCP JSON-RPC communication

---

## ADR-012: MCP Resources and Prompts Strategy

**Status**: ✅ Accepted **Date**: 2026-01-30 **Context**: Integration & APIs

### [ADR-012] Decision

Provide **static JSON resources** and **workflow prompts** via Spring AI MCP
annotations.

**Resources** (URI pattern: `weather://<name>`):

- `weather://codes` - WMO weather code interpretations
- `weather://ski-resorts` - Ski resort coordinates
- `weather://swiss-locations` - Swiss cities/mountains/passes
- `weather://aqi-reference` - AQI health recommendations
- `weather://parameters` - Available API parameters

**Prompts**:

- `ski-trip-weather` - Ski trip planning with snow conditions
- `plan-outdoor-activity` - Weather-aware activity planning
- `weather-aware-travel` - Travel planning with weather

**Implementation with Spring AI**:

```java
@Service
public class WeatherResourceService {

    @McpResource(uri = "weather://codes", name = "Weather Codes")
    public String weatherCodes() {
        return loadResource("data/weather-codes.json");
    }

    @McpResource(uri = "weather://ski-resorts", name = "Ski Resorts")
    public String skiResorts() {
        return loadResource("data/ski-resorts.json");
    }
}

@Service
public class WeatherPromptService {

    @McpPrompt(name = "ski-trip-weather")
    public String skiTripWeather(
        @McpParam("resort") String resort,
        @McpParam("dates") String dates
    ) {
        return createSkiTripPrompt(resort, dates);
    }
}
```

**Storage**: JSON files in `src/main/resources/data/`

**Related ADRs**:

- [ADR-004](#adr-004-use-spring-ai-20-for-weather-interpretation) - Spring AI
  MCP annotations

---

## ADR-013: Open-Meteo API Client with Gzip Compression

**Status**: 🔄 Proposed **Date**: 2026-01-30 **Context**: Integration & APIs

### [ADR-013] Decision

Use **Spring WebClient** with **gzip compression** for Open-Meteo API calls.

**Configuration**:

```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient openMeteoWebClient(WebClient.Builder builder) {
        return builder
            .baseUrl("https://api.open-meteo.com")
            .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
            .clientConnector(new ReactorClientHttpConnector(
                HttpClient.create().compress(true)
            ))
            .build();
    }
}
```

**Benefits**: 70-80% bandwidth reduction with gzip compression

---

## ADR-014: Privacy-First Weather Data Handling

**Status**: 🔄 Proposed **Date**: 2026-01-30 **Context**: Security & Privacy

### [ADR-014] Decision

**Stateless weather queries** with no personal data storage.

**Principles**:

- No user tracking or identification
- Location data: Coordinates only (no addresses or names)
- No persistent user data
- Weather data cached temporarily (5-60 minutes)
- No API keys required (Open-Meteo is free)
- GDPR compliant by design

**Caching**:

- Cache weather responses briefly to reduce API load
- Cache key: `weather:{lat}:{lon}:{params}`
- TTL: 5-60 minutes (based on data type)

---

## ADR-015: API Versioning and Backward Compatibility

**Status**: 🔄 Proposed **Date**: 2026-01-30 **Context**: Security & Privacy

### [ADR-015] Decision

**Maintain backward compatibility** for MCP tools during minor versions.

**Breaking Changes**:

- Require new MAJOR version
- 6 months deprecation notice
- Migration guide provided

**Non-Breaking Changes** (allowed in MINOR/PATCH):

- Adding new tools/resources/prompts
- Adding optional parameters to existing tools
- Enhancing response data (additive only)

**Deprecation Process**:

1. Mark tool as deprecated in documentation
2. Log warning when deprecated tool is used
3. Wait 6 months
4. Remove in next MAJOR version

---

## ADR-016: Adopt Java 25 LTS

**Status**: 🔄 Proposed **Date**: 2026-02-02 **Deciders**: Architecture Team
**Context**: Core Architecture

### [ADR-016] Decision

Adopt **Java 25 LTS** as the target runtime for the Open Meteo MCP Java project.

**Rationale**:

- **Long-term Support**: Java 25 is the next LTS release (September 2026)
  providing 8+ years of support
- **Virtual Threads Maturity**: Further optimizations in Project Loom for
  high-concurrency weather API calls
- **Performance Improvements**: Enhanced JIT compilation and garbage collection
  for better latency
- **Language Features**:
  - Improved pattern matching and switch expressions
  - Enhanced record patterns for weather data processing
  - Better foreign function interface for potential native libraries
- **Security Enhancements**: Latest security improvements and cryptographic
  algorithms
- **Spring Boot Compatibility**: Spring Boot 4.x fully supports Java 25
- **Future-Proofing**: Ensures project longevity and access to latest ecosystem
  improvements

**Migration Timeline**:

- **Q3 2026**: Upgrade development environment to Java 25 preview
- **Q4 2026**: Production deployment after Java 25 GA release
- **Q1 2027**: Remove Java 21 compatibility

**Build Configuration**:

```xml
<properties>
    <java.version>25</java.version>
    <maven.compiler.source>25</maven.compiler.source>
    <maven.compiler.target>25</maven.compiler.target>
</properties>
```

**Benefits for Weather Service**:

- **Improved Throughput**: Virtual Threads handle more concurrent weather API
  requests
- **Lower Latency**: Better JIT optimization for hot weather query paths
- **Memory Efficiency**: Enhanced GC reduces memory pressure during peak loads
- **Developer Experience**: Latest language features improve code readability

**Compatibility**:

- **Spring Boot**: 3.4+ fully supports Java 25
- **Spring AI**: Compatible with Java 25
- **Maven**: 3.9+ required for Java 25 support
- **Open-Meteo API**: No impact (HTTP client compatibility maintained)

### [ADR-016] Related ADRs

- [ADR-001](#adr-001-use-standard-java-with-completablefuture-for-async-operations) -
  Virtual Threads enhancement
- [ADR-002](#adr-002-use-java-records-for-all-data-models) - Enhanced record
  patterns
- [ADR-017](#adr-017-adopt-spring-boot-5) - Spring Boot 5 compatibility

---

## ADR-017: Adopt Spring Boot 5

**Status**: 🔄 Proposed **Date**: 2026-02-02 **Deciders**: Architecture Team
**Context**: Core Architecture

### [ADR-017] Decision

Adopt **Spring Boot 5** as the application framework, superseding the current
Spring Boot 4.x approach.

**Rationale**:

- **Jakarta EE 11 Support**: Full compatibility with the latest enterprise Java
  standards
- **Virtual Threads Integration**: Native optimization for Java 25 Virtual
  Threads and Project Loom
- **Enhanced Observability**: Built-in OpenTelemetry and Micrometer 2.0
  integration
- **GraalVM Native Image**: Improved native compilation support for faster
  startup times
- **Spring AI Integration**: Deep integration with Spring AI 3.0 for seamless
  MCP protocol handling
- **Performance Improvements**:
  - Reduced memory footprint (~30% improvement)
  - Faster startup times (~50% improvement with native image)
  - Better request throughput with Virtual Threads

**Migration Timeline**:

- **Q3 2026**: Upgrade to Spring Boot 5.0 GA after Java 25 adoption
- **Q4 2026**: Production deployment with performance validation
- **Q1 2027**: Remove Spring Boot 4.x compatibility

**Key Features for Weather Service**:

- **Auto-Configuration 3.0**: Simplified MCP server configuration
- **Reactive Stack 2.0**: Enhanced WebClient with Virtual Threads support
- **Native Compilation**: Faster cold starts for serverless deployments
- **Structured Logging 2.0**: Enhanced JSON logging with OpenTelemetry tracing

### [ADR-017] Package Structure (Updated)

`com.openmeteo.mcp.<layer>` with Spring Boot 5 conventions:

- `controller/` - MCP endpoints (@RestController)
- `service/` - Business logic (@Service)
- `client/` - External API clients (@Component)
- `model/` - Java Records (dto/, request/, mcp/)
- `config/` - Configuration (@Configuration)
- `exception/` - Exception handling (@RestControllerAdvice)
- `util/` - Stateless utilities

### [ADR-017] Configuration Example

```yaml
spring:
  application:
    name: open-meteo-mcp

  # Spring Boot 5 native features
  native:
    enabled: true
    build-args:
      - --enable-preview

  # Enhanced observability
  management:
    otlp:
      tracing:
        endpoint: http://localhost:4317
    metrics:
      export:
        prometheus:
          enabled: true

  # Spring AI 3.0 integration
  ai:
    mcp:
      server:
        name: open-meteo
        version: 2.0.0
        virtual-threads: true
```

### [ADR-017] Build Configuration

```xml
<properties>
    <spring-boot.version>5.0.0</spring-boot.version>
    <spring-ai.version>3.0.0</spring-ai.version>
    <java.version>25</java.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-mcp-spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

**Benefits**:

- **Future-Ready**: Aligned with latest Spring ecosystem roadmap
- **Performance**: Native image compilation for production deployments
- **Developer Experience**: Simplified configuration and enhanced tooling
- **Observability**: Built-in distributed tracing and metrics
- **Cloud-Native**: Optimized for Kubernetes and serverless environments

### [ADR-017] Related ADRs

- [ADR-001](#adr-001-use-standard-java-with-completablefuture-for-async-operations) -
  Virtual Threads optimization
- [ADR-002](#adr-002-use-java-records-for-all-data-models) - Java Records
  compatibility
- [ADR-004](#adr-004-use-spring-ai-20-for-weather-interpretation) - Spring AI
  3.0 integration
- [ADR-016](#adr-016-adopt-java-25-lts) - Java 25 LTS compatibility
- **Supersedes**:
  [ADR-003](#adr-003-follow-spring-boot-best-practices-and-layered-architecture)

---

## Summary

This streamlined ADR compendium focuses on **17 core architectural decisions**
relevant to the Open Meteo MCP Java project:

**Core Architecture** (6 ADRs):

- Standard Java with CompletableFuture
- Java Records for data models
- Spring Boot 5 framework (supersedes previous Spring Boot practices)
- Spring AI 2.0 for interpretation
- Java 25 LTS adoption

**Development Standards** (3 ADRs):

- Specification-Driven Development
- Semantic Versioning
- MCP tool naming conventions

**Quality & Observability** (3 ADRs):

- Structured JSON logging
- Micrometer metrics
- 80%+ test coverage

**Integration & APIs** (3 ADRs):

- MCP protocol implementation
- MCP resources and prompts
- Open-Meteo API client with gzip

**Security & Privacy** (2 ADRs):

- Privacy-first data handling
- API versioning strategy

---

**Migration Note**: This compendium replaces the previous 52-ADR version by
removing Journey Service (SBB railway) specific decisions and focusing on
weather service essentials.
