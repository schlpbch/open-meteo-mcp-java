# Open Meteo MCP - Architecture Decision Records (ADR) Compendium

**Document Version**: 3.5.0 **Last Updated**: 2026-02-05 **Total ADRs**: 20 (10
Accepted, 10 Proposed)

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
- [ADR-018: ChatHandler with Spring AI ChatClient](#adr-018-chathandler-with-spring-ai-chatclient)
  🔄

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
- [ADR-020: MCP Streamable HTTP Protocol Support](#adr-020-mcp-streamable-http-protocol-support)
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

## ADR-018: ChatHandler with Spring AI ChatClient

**Status**: 🔄 Proposed **Date**: 2026-02-02 **Deciders**: Architecture Team
**Context**: Core Architecture **Related Issue**: #4

### [ADR-018] Decision

Implement **ChatHandler with Spring AI ChatClient** to transform the MCP server
into an intelligent weather assistant with conversational AI capabilities.

**Primary LLM Provider**: Azure OpenAI GPT-4

**Rationale**:

- **Conversational Interface**: Enable natural language weather queries beyond
  simple tool calls
- **Multi-Provider Support**: Azure OpenAI (primary) with OpenAI and Anthropic
  fallbacks
- **Function Calling Integration**: Leverage all 11 MCP tools as AI functions
- **Conversation Memory**: Maintain context across multi-turn conversations
- **RAG Enhancement**: Enrich responses with historical weather patterns and
  location-specific knowledge
- **Enterprise Ready**: Azure OpenAI provides enterprise SLA, compliance, and
  data residency

### [ADR-018] Architecture

```
ChatHandler
    ├── Spring AI ChatClient (Azure OpenAI Primary)
    │   ├── Azure OpenAI GPT-4 (Primary)
    │   ├── OpenAI GPT-4 (Fallback)
    │   └── Anthropic Claude (Fallback)
    ├── Conversation Memory
    │   ├── InMemoryConversationMemory (dev)
    │   └── RedisConversationMemory (prod)
    ├── RAG (Retrieval Augmented Generation)
    │   ├── Vector Store (Pinecone/Weaviate/SimpleVectorStore)
    │   ├── Weather Document Service
    │   └── Context Enrichment
    ├── Function Calling → All 11 MCP Tools
    │   ├── Core Tools (4): search_location, get_weather, get_snow_conditions, get_air_quality
    │   └── Advanced Tools (7): weather_alerts, comfort_index, astronomy, search_location_swiss,
    │                            compare_locations, historical_weather, marine_conditions
    └── Observability
        ├── Token usage tracking
        ├── Response time metrics
        └── Provider health monitoring
```

### [ADR-018] Key Components

**1. ChatHandler Service**

```java
@Service
public class ChatHandler {
    private final ChatClient chatClient;
    private final ConversationMemoryService memoryService;
    private final RagService ragService;

    public CompletableFuture<AiResponse> chat(String sessionId, String message) {
        // Load conversation context
        var context = memoryService.loadContext(sessionId);
        
        // Enrich with RAG
        var enrichedPrompt = ragService.enrichContext(message, context);
        
        // Process with ChatClient (function calling enabled)
        var response = chatClient.prompt()
            .user(enrichedPrompt)
            .call()
            .content();
        
        // Store conversation state
        memoryService.saveMessage(sessionId, message, response);
        
        return CompletableFuture.completedFuture(response);
    }
}
```

**2. Azure OpenAI Configuration**

```yaml
spring:
  ai:
    azure:
      openai:
        api-key: ${AZURE_OPENAI_KEY}
        endpoint: ${AZURE_OPENAI_ENDPOINT}
        deployment-name: ${AZURE_OPENAI_DEPLOYMENT:gpt-4}
        model: gpt-4
    openai:
      api-key: ${OPENAI_API_KEY:}
      model: gpt-4-turbo
    anthropic:
      api-key: ${ANTHROPIC_API_KEY:}
    chat:
      default-provider: azure-openai
      fallback-providers: [openai, anthropic]
      timeout: 30s
      max-tokens: 2000
```

**3. Function Calling Integration**

```java
@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient chatClient(AzureOpenAiChatModel azureChatModel) {
        return ChatClient.builder(azureChatModel)
            .defaultFunctions(
                // All 11 MCP tools with  prefix
                "search_location",
                "get_weather",
                "get_snow_conditions",
                "get_air_quality",
                "get_weather_alerts",
                "get_comfort_index",
                "get_astronomy",
                "search_location_swiss",
                "compare_locations",
                "get_historical_weather",
                "get_marine_conditions"
            )
            .build();
    }
}
```

### [ADR-018] Benefits

**Conversational Intelligence**:
- Natural language queries: "What's the weather like in Zurich this weekend?"
- Multi-step reasoning: "Plan a ski trip to Zermatt next week"
- Context awareness: "How does that compare to last year?"

**Enterprise Features**:
- **Azure OpenAI**: Enterprise SLA, compliance (SOC 2, ISO 27001), data
  residency
- **Fallback Strategy**: Automatic failover to OpenAI or Anthropic
- **Cost Control**: Token usage monitoring and per-user quotas
- **Observability**: Full metrics, logging, and distributed tracing

**Enhanced Capabilities**:
- **RAG**: Historical weather patterns, location-specific knowledge
- **Memory**: Multi-turn conversations with context
- **Streaming**: Real-time response streaming for better UX
- **Agent Patterns**: Complex multi-step weather analysis

### [ADR-018] Implementation Phases

**Phase 4.1** (Week 1-2): Foundation & Core ChatHandler
- Azure OpenAI configuration
- Basic ChatHandler with session management
- Function calling integration with all 11 tools

**Phase 4.2** (Week 2-3): RAG & Context Enrichment
- Vector store setup (SimpleVectorStore for dev, Pinecone for prod)
- Weather document indexing
- Context enrichment pipeline

**Phase 4.3** (Week 3-4): Advanced Features & Production
- Streaming responses
- Agent patterns for complex queries
- Comprehensive observability
- Production deployment

### [ADR-018] Success Criteria

**Functional**:
- [ ] Multi-provider LLM support with fallback
- [ ] Function calling integration with all 11 MCP tools
- [ ] Conversation memory and context management
- [ ] RAG implementation with 40%+ accuracy improvement

**Performance**:
- [ ] Response time: <2s for 95% of queries
- [ ] Throughput: 100+ concurrent conversations
- [ ] Availability: 99.5% uptime SLA

**Quality**:
- [ ] Test coverage: 85%+
- [ ] Complete API documentation
- [ ] Full observability (metrics, logging, tracing)

### [ADR-018] Related ADRs

- [ADR-001](#adr-001-use-standard-java-with-completablefuture-for-async-operations) -
  Async operations
- [ADR-002](#adr-002-use-java-records-for-all-data-models) - Data models for
  chat sessions
- [ADR-004](#adr-004-use-spring-ai-20-for-weather-interpretation) - Spring AI
  foundation
- [ADR-008](#adr-008-structured-json-logging-with-slf4j) - Logging for chat
  interactions
- [ADR-009](#adr-009-micrometer-for-observability) - Metrics for token usage
  and latency
- [ADR-011](#adr-011-mcp-protocol-implementation) - MCP tools as AI functions

### [ADR-018] Risks & Mitigation

**Risks**:
1. **Azure OpenAI Rate Limits**: Mitigate with fallback providers and request
   queuing
2. **Token Costs**: Implement per-user quotas and response caching
3. **Latency**: Use streaming responses and optimize RAG retrieval (<500ms)
4. **Context Window**: Implement conversation summarization for long sessions

**Monitoring**:
- Token usage per session/user
- Provider health and failover events
- Response latency (p50, p95, p99)
- Function calling success rate

---

## ADR-020: MCP Streamable HTTP Protocol Support

**Status**: 🔄 Proposed **Date**: 2026-02-05 **Deciders**: Architecture Team
**Context**: Integration & APIs

### [ADR-020] Decision

Implement **MCP Streamable HTTP Protocol Support** to enable real-time streaming communication between MCP clients and the Open Meteo server.

**Rationale**:

- **Real-time Weather Updates**: Support streaming weather data for dynamic conditions
- **Improved User Experience**: Reduce latency for weather queries with immediate response streaming
- **MCP Protocol Evolution**: Align with MCP protocol specifications for HTTP streaming
- **Resource Efficiency**: Stream large datasets (historical weather, forecasts) without memory overhead
- **Interactive Chat Experience**: Enable real-time conversational AI responses for weather queries
- **Modern Web Standards**: Leverage Server-Sent Events (SSE) and HTTP streaming best practices

### [ADR-020] Architecture

```
MCP Streamable HTTP Architecture
    ├── HTTP Streaming Layer
    │   ├── Server-Sent Events (SSE) Support
    │   ├── Chunked Transfer Encoding
    │   └── WebSocket Fallback (Future)
    ├── MCP Stream Protocol
    │   ├── Stream Initialization
    │   ├── Data Chunks with Metadata
    │   ├── Progress Indicators
    │   └── Stream Completion Signals
    ├── Weather Data Streaming
    │   ├── Real-time Current Conditions
    │   ├── Forecast Data Streams
    │   ├── Historical Data Pagination
    │   └── Multi-location Weather Streams
    ├── Chat Response Streaming
    │   ├── Token-by-token AI Responses
    │   ├── Function Call Results
    │   ├── Weather Interpretation Streaming
    │   └── Context-aware Response Building
    └── Client Compatibility
        ├── Claude Desktop Integration
        ├── Browser-based MCP Clients
        ├── Mobile App Support
        └── Legacy Non-streaming Fallback
```

### [ADR-020] Implementation Details

**1. Streamable HTTP Configuration**

```java
@Configuration
@EnableWebFlux
public class StreamingConfig {
    
    @Bean
    public RouterFunction<ServerResponse> streamingRoutes() {
        return RouterFunctions.route()
            .path("/api/mcp/stream", builder -> builder
                .GET("/weather/{location}", this::streamWeatherData)
                .GET("/forecast/{location}", this::streamForecastData)
                .GET("/chat/{sessionId}", this::streamChatResponse)
                .POST("/chat/stream", this::streamChatInteraction)
            )
            .build();
    }
    
    @Bean
    public WebFluxConfigurer webFluxConfigurer() {
        return new WebFluxConfigurer() {
            @Override
            public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
                // Enable streaming for large responses
                configurer.defaultCodecs().maxInMemorySize(1024 * 1024); // 1MB
                configurer.defaultCodecs().enableLoggingRequestDetails(true);
            }
        };
    }
}
```

**2. MCP Stream Protocol Handler**

```java
@Component
public class McpStreamHandler {
    
    public Flux<ServerSentEvent<McpStreamChunk>> streamWeatherData(
            String location, 
            ServerRequest request) {
        
        return weatherService.getStreamingWeatherData(location)
            .map(weatherData -> ServerSentEvent.<McpStreamChunk>builder()
                .id(UUID.randomUUID().toString())
                .event("weather-data")
                .data(new McpStreamChunk("weather", weatherData, false))
                .build())
            .concatWith(Mono.just(ServerSentEvent.<McpStreamChunk>builder()
                .event("stream-complete")
                .data(new McpStreamChunk("completion", null, true))
                .build()));
    }
    
    public Flux<ServerSentEvent<McpChatChunk>> streamChatResponse(
            String sessionId,
            Mono<ChatRequest> chatRequest) {
        
        return chatRequest
            .flatMapMany(request -> chatService.streamResponse(sessionId, request))
            .map(token -> ServerSentEvent.<McpChatChunk>builder()
                .id(UUID.randomUUID().toString())
                .event("chat-token")
                .data(new McpChatChunk(token, false))
                .build())
            .concatWith(Mono.just(ServerSentEvent.<McpChatChunk>builder()
                .event("chat-complete")
                .data(new McpChatChunk("", true))
                .build()));
    }
}
```

**3. Stream Data Models**

```java
public record McpStreamChunk(
    String type,
    Object data,
    boolean isComplete,
    String timestamp,
    Map<String, Object> metadata
) {
    public McpStreamChunk(String type, Object data, boolean isComplete) {
        this(type, data, isComplete, Instant.now().toString(), Map.of());
    }
}

public record McpChatChunk(
    String token,
    boolean isComplete,
    String timestamp,
    ChatMetadata metadata
) {
    public McpChatChunk(String token, boolean isComplete) {
        this(token, isComplete, Instant.now().toString(), new ChatMetadata());
    }
}

public record ChatMetadata(
    int tokenCount,
    String functionCall,
    String modelUsed,
    long processingTimeMs
) {
    public ChatMetadata() {
        this(0, null, "gpt-4", 0L);
    }
}
```

**4. Streaming Weather Service**

```java
@Service
public class StreamingWeatherService {
    
    private final WeatherService weatherService;
    private final LocationService locationService;
    
    public Flux<WeatherData> getStreamingWeatherData(String location) {
        return Mono.fromCallable(() -> locationService.resolveLocation(location))
            .flatMapMany(coords -> Flux.merge(
                getCurrentConditions(coords),
                getHourlyForecast(coords),
                getDailyForecast(coords)
            ))
            .delayElements(Duration.ofMillis(100)); // Simulate streaming delay
    }
    
    private Flux<WeatherData> getCurrentConditions(Coordinates coords) {
        return weatherService.getCurrentWeatherReactive(coords.lat(), coords.lon())
            .flux()
            .map(weather -> new WeatherData("current", weather));
    }
    
    private Flux<WeatherData> getHourlyForecast(Coordinates coords) {
        return weatherService.getHourlyForecastReactive(coords.lat(), coords.lon())
            .flatMapMany(forecast -> Flux.fromIterable(forecast.hourly()))
            .map(hour -> new WeatherData("hourly", hour));
    }
}
```

**5. Client-Side Stream Consumption**

```javascript
// JavaScript client example for MCP Streamable HTTP
class McpStreamClient {
    constructor(baseUrl, apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }
    
    streamWeatherData(location, onData, onComplete, onError) {
        const eventSource = new EventSource(
            `${this.baseUrl}/api/mcp/stream/weather/${location}`,
            {
                headers: {
                    'X-API-Key': this.apiKey
                }
            }
        );
        
        eventSource.addEventListener('weather-data', (event) => {
            const chunk = JSON.parse(event.data);
            onData(chunk);
        });
        
        eventSource.addEventListener('stream-complete', (event) => {
            eventSource.close();
            onComplete();
        });
        
        eventSource.onerror = onError;
        
        return eventSource;
    }
    
    async streamChatResponse(sessionId, message, onToken, onComplete) {
        const response = await fetch(`${this.baseUrl}/api/mcp/stream/chat/stream`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-API-Key': this.apiKey,
                'Accept': 'text/event-stream'
            },
            body: JSON.stringify({
                sessionId: sessionId,
                message: message
            })
        });
        
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        
        while (true) {
            const { done, value } = await reader.read();
            if (done) break;
            
            const chunk = decoder.decode(value);
            const lines = chunk.split('\n');
            
            for (const line of lines) {
                if (line.startsWith('data: ')) {
                    const data = JSON.parse(line.substring(6));
                    if (data.isComplete) {
                        onComplete();
                    } else {
                        onToken(data.token);
                    }
                }
            }
        }
    }
}
```

### [ADR-020] Protocol Specification

**Stream Message Format:**
```json
{
  "event": "weather-data|chat-token|stream-complete|error",
  "id": "uuid-v4",
  "data": {
    "type": "current|hourly|daily|chat|completion",
    "content": {...},
    "isComplete": false,
    "timestamp": "2026-02-05T10:30:00Z",
    "metadata": {
      "progress": 0.75,
      "totalChunks": 10,
      "chunkIndex": 7
    }
  }
}
```

**Stream Endpoints:**
- **GET `/api/mcp/stream/weather/{location}`** - Stream real-time weather data
- **GET `/api/mcp/stream/forecast/{location}`** - Stream forecast data chunks
- **POST `/api/mcp/stream/chat`** - Stream chat conversation
- **GET `/api/mcp/stream/chat/{sessionId}`** - Resume streaming chat session

**Headers:**
- **`Accept: text/event-stream`** - Enable SSE streaming
- **`Cache-Control: no-cache`** - Disable caching for real-time data
- **`X-API-Key`** - Authentication (as per ADR-019)

### [ADR-020] Configuration Properties

```yaml
mcp:
  streaming:
    enabled: true
    max-connections: 100
    heartbeat-interval: 30s
    chunk-size: 1024
    timeout: 300s
    
  sse:
    enabled: true
    retry-interval: 3000
    keep-alive: true
    
  websocket:
    enabled: false  # Future implementation
    max-frame-size: 65536
    
  weather-streaming:
    batch-size: 10
    delay-between-chunks: 100ms
    include-metadata: true
    
  chat-streaming:
    token-delay: 50ms
    include-function-calls: true
    buffer-size: 256
```

### [ADR-020] Benefits

**Performance Benefits:**
- **Reduced Memory Usage**: Stream large datasets without loading entirely in memory
- **Lower Latency**: Start processing data as soon as first chunk arrives
- **Better Responsiveness**: Real-time updates for changing weather conditions
- **Scalable Connections**: Handle multiple concurrent streaming clients

**User Experience Benefits:**
- **Immediate Feedback**: Users see data arriving in real-time
- **Interactive Chat**: Token-by-token AI responses for natural conversation
- **Progress Indicators**: Visual feedback for long-running operations
- **Cancellable Operations**: Users can stop streaming if needed

**Technical Benefits:**
- **Protocol Compliance**: Full MCP streamable HTTP specification support
- **Backward Compatibility**: Graceful fallback to standard HTTP for legacy clients
- **Error Handling**: Stream-aware error recovery and retry mechanisms
- **Monitoring**: Stream-specific metrics and observability

### [ADR-020] Implementation Roadmap

**Phase 1: Core Streaming Infrastructure (Week 1-2)**
- [ ] Spring WebFlux configuration for streaming
- [ ] Basic SSE endpoint implementation
- [ ] Stream data models and protocol definitions
- [ ] Authentication integration with existing Spring Security

**Phase 2: Weather Data Streaming (Week 3)**
- [ ] Current conditions streaming endpoint
- [ ] Forecast data streaming with chunking
- [ ] Historical weather data pagination streaming
- [ ] Multi-location concurrent streaming

**Phase 3: Chat Response Streaming (Week 4)**
- [ ] Integration with Spring AI ChatClient
- [ ] Token-by-token response streaming
- [ ] Function call result streaming
- [ ] Conversation context streaming

**Phase 4: Advanced Features (Week 5-6)**
- [ ] WebSocket fallback implementation
- [ ] Stream compression and optimization
- [ ] Advanced error handling and recovery
- [ ] Performance monitoring and metrics

**Testing & Documentation:**
- [ ] Stream protocol compliance tests
- [ ] Load testing for concurrent streams
- [ ] Client SDK examples (JavaScript, Python)
- [ ] Integration with Claude Desktop MCP client

### [ADR-020] Related ADRs

- [ADR-001](#adr-001-use-standard-java-with-completablefuture-for-async-operations) - Async operations foundation
- [ADR-008](#adr-008-structured-json-logging-with-slf4j) - Streaming operation logging
- [ADR-009](#adr-009-micrometer-for-observability) - Stream performance metrics
- [ADR-011](#adr-011-mcp-protocol-implementation) - Core MCP protocol compliance
- [ADR-018](#adr-018-chathandler-with-spring-ai-chatclient) - Chat streaming integration
- [ADR-019](#adr-019-use-spring-security-for-authentication-and-authorization) - Stream authentication

### [ADR-020] Dependencies

```xml
<!-- Spring WebFlux for Reactive Streaming -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Reactor Core for Stream Processing -->
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
</dependency>

<!-- Jackson for JSON Streaming -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-streaming</artifactId>
</dependency>
```

### [ADR-020] Risks & Mitigation

**Risks:**
1. **Connection Management**: Large number of concurrent streams may exhaust server resources
   - **Mitigation**: Implement connection limits and proper cleanup
2. **Network Reliability**: Stream interruptions due to network issues
   - **Mitigation**: Implement retry mechanisms and stream resumption
3. **Client Compatibility**: Not all MCP clients support streaming
   - **Mitigation**: Maintain backward compatibility with standard HTTP endpoints
4. **Resource Consumption**: Streaming may increase server resource usage
   - **Mitigation**: Implement proper backpressure and resource monitoring

**Monitoring:**
- Track active stream connections and resource usage
- Monitor stream completion rates and error frequencies
- Measure streaming latency and throughput
- Alert on connection limit violations

---

## Summary

This streamlined ADR compendium focuses on **20 core architectural decisions**
relevant to the Open Meteo MCP Java project:

**Core Architecture** (7 ADRs):

- Standard Java with CompletableFuture
- Java Records for data models
- Spring Boot 5 framework (supersedes previous Spring Boot practices)
- Spring AI 2.0 for interpretation
- Java 25 LTS adoption
- ChatHandler with Spring AI ChatClient

**Development Standards** (3 ADRs):

- Specification-Driven Development
- Semantic Versioning
- MCP tool naming conventions

**Quality & Observability** (3 ADRs):

- Structured JSON logging
- Micrometer metrics
- 80%+ test coverage

**Integration & APIs** (4 ADRs):

- MCP protocol implementation
- MCP resources and prompts
- Open-Meteo API client with gzip
- MCP Streamable HTTP protocol support

**Security & Privacy** (3 ADRs):

- Privacy-first data handling
- API versioning strategy
- Spring Security for authentication and authorization

---

**Migration Note**: This compendium replaces the previous 52-ADR version by
removing Journey Service (SBB railway) specific decisions and focusing on
weather service essentials.
