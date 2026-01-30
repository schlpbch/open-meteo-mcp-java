# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Open Meteo MCP (Java) is a **Model Context Protocol (MCP) server** providing weather, snow conditions, and air quality data via the [Open-Meteo API](https://open-meteo.com/). This is a **strategic migration** of the proven open-meteo-mcp (Python/FastMCP v3.2.0) to Java/Spring Boot for enterprise-grade architecture and Spring AI 2.0 integration.

**Current Status**: 🔄 Phase 1 - Foundation (Weeks 1-2 of 9-week migration)

**Key Technologies:**

- Java 21 with Virtual Threads
- Spring Boot 3.5 with WebFlux (async/non-blocking)
- Spring AI 2.0 (native MCP annotations + ChatClient)
- Maven 3.9+ for build management
- Jackson for JSON serialization with gzip compression
- SLF4J + Logback for structured JSON logging
- Micrometer for observability
- JUnit 5 + Mockito + AssertJ for testing

## Essential Commands

### Building and Testing

```bash
# Build project
./mvnw clean install

# Run application
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report
# Report at: target/site/jacoco/index.html

# Run integration tests
./mvnw verify -P integration-tests

# Run specific test
./mvnw test -Dtest=WeatherServiceTest

# Package JAR
./mvnw package
```

### Running the Application

```bash
# Stdio mode (for MCP clients)
java -jar target/open-meteo-mcp-1.0.0.jar

# With Spring AI integration
export ANTHROPIC_API_KEY=your_key_here
java -jar target/open-meteo-mcp-1.0.0.jar

# Development mode with live reload
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Code Quality

```bash
# Check code style
./mvnw checkstyle:check

# Run static analysis
./mvnw spotbugs:check

# Format code
./mvnw spotless:apply
```

## Architecture Overview

### Package Structure

```text
src/main/java/com/openmeteo/mcp/
├── OpenMeteoMcpApplication.java    # Main Spring Boot application
│
├── config/                         # Configuration layer
│   ├── WebClientConfig.java       # WebClient with gzip compression
│   ├── SpringAiConfig.java        # Spring AI ChatClient setup
│   └── McpServerConfig.java       # MCP server configuration
│
├── tool/                           # MCP Tool layer (Spring AI annotations)
│   ├── WeatherToolService.java    # @McpTool methods for weather
│   ├── SnowToolService.java       # @McpTool methods for snow
│   ├── AirQualityToolService.java # @McpTool methods for air quality
│   ├── LocationToolService.java   # @McpTool methods for geocoding
│   ├── WeatherResourceService.java # @McpResource methods
│   └── WeatherPromptService.java  # @McpPrompt methods
│
├── service/                        # Service layer (business logic)
│   ├── WeatherService.java        # Weather business logic
│   ├── LocationService.java       # Geocoding business logic
│   ├── SnowConditionsService.java # Snow conditions logic
│   ├── AirQualityService.java     # Air quality logic
│   └── InterpretationService.java # Spring AI weather interpretation
│
├── client/                         # Client layer (external APIs)
│   ├── OpenMeteoClient.java       # Open-Meteo API client
│   └── OpenMeteoClientConfig.java # Client configuration
│
├── model/                          # Model layer (Java Records)
│   ├── dto/                        # Data Transfer Objects
│   │   ├── WeatherResponse.java
│   │   ├── SnowConditionsResponse.java
│   │   ├── AirQualityResponse.java
│   │   └── LocationSearchResponse.java
│   ├── request/                    # Request models
│   │   ├── WeatherRequest.java
│   │   ├── SnowConditionsRequest.java
│   │   └── AirQualityRequest.java
│   └── mcp/                        # MCP-specific models
│       ├── McpToolRequest.java
│       ├── McpToolResponse.java
│       └── McpResource.java
│
├── exception/                      # Exception layer
│   ├── OpenMeteoException.java    # Custom exceptions
│   ├── McpException.java
│   └── GlobalExceptionHandler.java # @RestControllerAdvice
│
└── util/                           # Utility layer
    ├── WeatherCodeInterpreter.java # WMO code interpretation
    ├── AqiInterpreter.java         # AQI interpretation
    └── JsonSerializationUtil.java  # JSON utilities
```

### MCP Tools (4 tools to implement)

| Tool                 | Description                                             | Status |
| -------------------- | ------------------------------------------------------- | ------ |
| `search_location`    | Geocoding - search locations by name                    | ⏳ Pending |
| `get_weather`        | Get weather forecast with temperature, precipitation    | ⏳ Pending |
| `get_snow_conditions`| Get snow depth, snowfall, mountain weather              | ⏳ Pending |
| `get_air_quality`    | Get AQI, pollutants, UV index, pollen                   | ⏳ Pending |

### MCP Resources (5 resources to implement)

| Resource URI                | Description                          | Data File                    |
| --------------------------- | ------------------------------------ | ---------------------------- |
| `weather://codes`           | WMO weather code reference           | data/weather-codes.json      |
| `weather://ski-resorts`     | Ski resort coordinates               | data/ski-resorts.json        |
| `weather://swiss-locations` | Swiss cities, mountains, passes      | data/swiss-locations.json    |
| `weather://aqi-reference`   | AQI scales and health recommendations| data/aqi-reference.json      |
| `weather://parameters`      | Available weather parameters         | data/weather-parameters.json |

### MCP Prompts (3 prompts to implement)

| Prompt                   | Description                              | Arguments                     |
| ------------------------ | ---------------------------------------- | ----------------------------- |
| `ski-trip-weather`       | Ski trip planning with snow conditions   | resort, dates                 |
| `plan-outdoor-activity`  | Weather-aware activity planning          | activity, location, timeframe |
| `weather-aware-travel`   | Travel planning with weather integration | destination, travel_dates, trip_type |

## Core Components

### Spring AI 2.0 MCP Annotations

**Key Innovation**: Spring AI 2.0 provides native MCP protocol support via annotations, eliminating the need for custom protocol implementation.

**MCP Tool Example**:
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

**MCP Resource Example**:
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

**MCP Prompt Example**:
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

### Data Models (Java Records)

**All data models use Java Records** for immutability and conciseness:

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
            throw new IllegalArgumentException("Temperature below absolute zero");
        }
    }
}
```

### Async Operations (CompletableFuture + Virtual Threads)

**No reactive Mono/Flux** - use CompletableFuture with Virtual Threads:

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

### Open-Meteo API Client

**Async HTTP client with gzip compression**:

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

## Development Guidelines

### Specification-Driven Development (ADR-005)

**ALWAYS document before coding**:

1. Write specification (tool/resource/prompt contract)
2. Get review/approval
3. Implement following spec
4. Update documentation

### Adding a New MCP Tool

1. **Define Tool Contract** in spec/API_REFERENCE.md (create if needed)

2. **Create Tool Service** with `@McpTool` annotation:

```java
@Service
public class MyToolService {
    private final MyService myService;

    @McpTool(
        name = "my_tool",  // snake_case (ADR-007)
        description = "Description of what this tool does"
    )
    public CompletableFuture<MyResponse> myTool(
        @McpParam(value = "param1", description = "Description", required = true)
        String param1,

        @McpParam(value = "param2", description = "Description", required = false)
        Optional<Integer> param2
    ) {
        return myService.performOperation(param1, param2.orElse(10));
    }
}
```

3. **Create Service Layer** for business logic:

```java
@Service
public class MyService {
    private final OpenMeteoClient client;

    public CompletableFuture<MyResponse> performOperation(String param1, int param2) {
        // Business logic here
        return client.callApi(param1, param2);
    }
}
```

4. **Create Data Models** as Java Records:

```java
public record MyResponse(
    String field1,
    int field2,
    Instant timestamp
) implements Serializable {
    public MyResponse {
        // Validation in compact constructor
        if (field2 < 0) {
            throw new IllegalArgumentException("field2 must be non-negative");
        }
    }
}
```

5. **Write Tests**:

```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock OpenMeteoClient client;
    @InjectMocks MyService service;

    @Test
    void shouldPerformOperation() {
        when(client.callApi(anyString(), anyInt()))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));

        var result = service.performOperation("test", 10).join();

        assertThat(result).isNotNull();
        verify(client).callApi("test", 10);
    }
}
```

### Testing Strategy (ADR-010)

**Target: ≥80% test coverage**

**Test Layers**:
1. **Unit Tests** (JUnit 5 + Mockito) - Fast, isolated
2. **Integration Tests** (@SpringBootTest) - Full application context
3. **Contract Tests** - MCP protocol compliance
4. **Performance Tests** - Benchmark vs Python version

**Example Unit Test**:
```java
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {
    @Mock OpenMeteoClient client;
    @InjectMocks WeatherService service;

    @Test
    void shouldGetWeatherSuccessfully() {
        // Arrange
        var mockResponse = new WeatherResponse(15.5, 0.0, 1, Instant.now());
        when(client.getWeather(anyDouble(), anyDouble()))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        var result = service.getWeather(47.3769, 8.5417, 7).join();

        // Assert
        assertThat(result.temperature()).isEqualTo(15.5);
        verify(client).getWeather(47.3769, 8.5417);
    }
}
```

**Example Integration Test**:
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class WeatherIntegrationTest {
    @Autowired WeatherToolService weatherToolService;

    @Test
    void shouldGetWeatherFromActualApi() {
        var result = weatherToolService.getWeather(47.3769, 8.5417, Optional.of(7)).join();

        assertThat(result).isNotNull();
        assertThat(result.temperature()).isBetween(-50.0, 50.0);
    }
}
```

### Logging (ADR-008)

**Use SLF4J with structured JSON logging**:

```java
@Slf4j
@Service
public class WeatherService {
    public CompletableFuture<WeatherResponse> getWeather(double lat, double lon, int days) {
        log.info("Fetching weather for location lat={}, lon={}, days={}", lat, lon, days);

        return client.getWeather(lat, lon, days)
            .whenComplete((response, error) -> {
                if (error != null) {
                    log.error("Failed to fetch weather for lat={}, lon={}", lat, lon, error);
                } else {
                    log.info("Successfully fetched weather for lat={}, lon={}", lat, lon);
                }
            });
    }
}
```

### Error Handling

**Use custom exceptions and global handler**:

```java
// Custom exception
public class OpenMeteoException extends RuntimeException {
    private final HttpStatus status;

    public OpenMeteoException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}

// Global handler
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(OpenMeteoException.class)
    public ResponseEntity<ProblemDetail> handleOpenMeteoException(OpenMeteoException ex) {
        var problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setTitle("Open-Meteo API Error");
        return ResponseEntity.of(problem).build();
    }
}
```

## Key Files

### Core Files

- `pom.xml` - Maven dependencies and build configuration
- `src/main/java/com/openmeteo/mcp/OpenMeteoMcpApplication.java` - Main application
- `src/main/resources/application.yml` - Spring Boot configuration
- `src/main/resources/logback-spring.xml` - Logging configuration

### Documentation

- `README.md` - User-facing documentation
- `CLAUDE.md` - This file (AI development guide)
- `spec/CONSTITUTION.md` - Project governance (1,053 lines)
- `spec/ADR_COMPENDIUM.md` - 15 Architecture Decision Records (657 lines)
- `spec/MIGRATION_GUIDE.md` - Python to Java migration guide (550+ lines)

### Python Reference Implementation

- Location: `c:\Users\schlp\code\open-meteo-mcp`
- Version: v3.2.0 (production reference)
- Key files to reference during migration:
  - `src/open_meteo_mcp/server.py` - FastMCP tools/resources/prompts
  - `src/open_meteo_mcp/client.py` - httpx async client
  - `src/open_meteo_mcp/models.py` - Pydantic models
  - `src/open_meteo_mcp/helpers.py` - Utility functions

## Configuration

### Application Configuration (application.yml)

```yaml
spring:
  application:
    name: open-meteo-mcp

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

openmeteo:
  api:
    base-url: https://api.open-meteo.com
    geocoding-url: https://geocoding-api.open-meteo.com
    air-quality-url: https://air-quality-api.open-meteo.com
    gzip-enabled: true
    cache-ttl-minutes: 30
    timeout-seconds: 10

management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Maven Dependencies (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <!-- Spring AI 2.0 -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-anthropic</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-mcp</artifactId>
    </dependency>

    <!-- Observability -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>

    <!-- Logging -->
    <dependency>
        <groupId>net.logstash.logback</groupId>
        <artifactId>logstash-logback-encoder</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Troubleshooting

### Common Issues

**Build Failures:**
```bash
# Clean and rebuild
./mvnw clean install -U

# Skip tests
./mvnw clean install -DskipTests
```

**Test Failures:**
```bash
# Run specific test with verbose output
./mvnw test -Dtest=WeatherServiceTest -X

# Check test reports
cat target/surefire-reports/*.txt
```

**Spring AI Configuration:**
```bash
# Verify API key is set
echo $ANTHROPIC_API_KEY

# Check Spring AI autoconfiguration
./mvnw spring-boot:run -Ddebug
```

**WebClient Gzip Issues:**
- Ensure `reactor-netty` is on classpath
- Check `Accept-Encoding: gzip` header in logs
- Verify Open-Meteo API supports gzip (it does)

## Migration Status Tracking

### Phase 1: Foundation (Weeks 1-2) - ✅ In Progress

**Tasks**:
- [x] Create Maven project structure
- [ ] Set up Spring Boot 3.5 with WebFlux
- [ ] Implement OpenMeteoClient with gzip compression
- [ ] Migrate Pydantic models to Java Records
- [ ] Set up test infrastructure (JUnit 5, Mockito, AssertJ)
- [ ] Implement JSON serialization utilities
- [ ] Copy data/*.json resource files

### Phase 2: Core Tools (Weeks 3-4) - ⏳ Pending

**Tasks**:
- [ ] Create LocationToolService with @McpTool for `search_location`
- [ ] Create WeatherToolService with @McpTool for `get_weather`
- [ ] Create SnowToolService with @McpTool for `get_snow_conditions`
- [ ] Create AirQualityToolService with @McpTool for `get_air_quality`
- [ ] Port weather code interpretation logic
- [ ] Port AQI interpretation logic
- [ ] Write unit tests for each tool

### Phase 3-6: See CONSTITUTION.md Section 15

## Project Status

**Current Version**: 1.0.0-alpha (Migration Phase)
**Target Release**: Q2 2026 (v1.0.0)
**Test Coverage**: 0% (TBD - target 80%+)
**Python Reference**: v3.2.0 (production)

## Important Reminders

1. **ALWAYS use Java Records** for all DTOs and response models (ADR-002)
2. **ALWAYS use CompletableFuture** (not Mono/Flux) for async operations (ADR-001)
3. **ALWAYS use snake_case** for MCP tool names (ADR-007)
4. **ALWAYS document before coding** - Specification-Driven Development (ADR-005)
5. **ALWAYS use @McpTool/@McpResource/@McpPrompt** annotations from Spring AI 2.0 (ADR-004)
6. **ALWAYS write tests** - target ≥80% coverage (ADR-010)
7. **ALWAYS use structured JSON logging** with SLF4J (ADR-008)

## Quick Links

- **Python Reference**: `c:\Users\schlp\code\open-meteo-mcp`
- **Java Implementation**: `c:\Users\schlp\code\open-meteo-mcp-java`
- **ADR Compendium**: [spec/ADR_COMPENDIUM.md](spec/ADR_COMPENDIUM.md)
- **Constitution**: [spec/CONSTITUTION.md](spec/CONSTITUTION.md)
- **Migration Guide**: [spec/MIGRATION_GUIDE.md](spec/MIGRATION_GUIDE.md)
- **Spring AI Docs**: https://docs.spring.io/spring-ai/reference/
- **Open-Meteo API**: https://open-meteo.com/en/docs
- **MCP Protocol**: https://modelcontextprotocol.io/

---

**For detailed migration strategy, see [CONSTITUTION.md Section 15](spec/CONSTITUTION.md#15-migration-strategy-python-to-java)**
