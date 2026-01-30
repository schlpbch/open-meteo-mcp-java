# Open Meteo MCP Server (Java)

A Model Context Protocol (MCP) server providing weather, snow conditions, and air quality tools via the [Open-Meteo API](https://open-meteo.com/).

**Version**: 1.0.0-alpha.6 (Phase 5 Complete)
**Status**: ✅ Phase 5 Complete - Testing & Documentation Finished
**License**: Apache 2.0

## 🎉 Project Milestone

This is a **strategic migration** of the proven [open-meteo-mcp](https://github.com/schlpbch/open-meteo-mcp) Python v3.2.0 to Java with Spring Boot 3.5 and Spring AI 2.0.

**Why Java?**
- ✅ Enterprise-grade architecture with Spring Boot ecosystem
- ✅ Spring AI 2.0 integration for enhanced weather intelligence
- ✅ Type safety with Java Records
- ✅ Better integration with Swiss AI MCP infrastructure
- ✅ JVM performance and scalability characteristics

**Migration Complete - Phase 5 Achievement**:

- ✅ Phase 1: Foundation (Weeks 1-2) - Project structure, API client, 18 models
- ✅ Phase 2: Services & Utilities (Weeks 3-4) - 4 services, 4 utilities, 87 tests
- ✅ Phase 3: Resources & Prompts (Week 5) - 4 resources, 3 prompts, 112 tests
- ✅ **Phase 5: Testing & Documentation (Weeks 7-8) - 279 tests, 81% coverage, API docs**
- ⏳ Phase 6: Deployment (Week 9) - Production Release (Q2 2026)

**v1.0.0-alpha.6 Highlights**:
- 279 passing tests (100% pass rate)
- 81% code coverage (exceeding 80% goal)
- Comprehensive API documentation
- 7 new test classes covering request models and DTOs
- Ready for Phase 6 deployment

## Features

### 🌦️ MCP Tools (Planned)

- **`search_location`**: Geocoding and location search
- **`get_weather`**: Weather forecasts with temperature, precipitation, wind, UV index
- **`get_snow_conditions`**: Snow depth, snowfall, mountain weather
- **`get_air_quality`**: AQI, pollutants, UV index, pollen data (Europe)

### 📚 MCP Resources

- **`weather://codes`**: Weather codes (WMO interpretations)
- **`weather://parameters`**: Weather parameters (available API fields)
- **`weather://aqi-reference`**: AQI reference (health recommendations)
- **`weather://swiss-locations`**: Swiss locations (cities, mountains, passes)

### 🎯 MCP Prompts

- **`meteo__ski-trip-weather`**: Ski trip planning workflow with snow conditions
- **`meteo__plan-outdoor-activity`**: Weather-aware outdoor activity planning
- **`meteo__weather-aware-travel`**: Travel planning with weather integration

## Technology Stack

### Core Dependencies

- **Java 21** - LTS with virtual threads
- **Spring Boot 3.5** - Latest stable
- **Spring AI 2.0** - AI integration + native MCP annotations
- **Maven 3.9+** - Build tool
- **Jackson** - JSON serialization with compression
- **Micrometer** - Observability and metrics
- **JUnit 5 + AssertJ + Mockito** - Testing framework

### Key Architectural Decisions

- **CompletableFuture + Virtual Threads** for async operations (no reactive Mono/Flux)
- **Java Records** for all data models
- **Spring AI 2.0 MCP annotations** (`@McpTool`, `@McpResource`, `@McpPrompt`)
- **SLF4J + Logback** for structured JSON logging
- **Specification-Driven Development** - Document before coding

See [ADR_COMPENDIUM.md](spec/ADR_COMPENDIUM.md) for all 15 architectural decisions.

## Prerequisites

- Java 21 or higher
- Maven 3.9+ or Gradle 8+
- (Optional) Docker for containerized deployment

## Installation

### Clone the Repository

```bash
git clone https://github.com/schlpbch/open-meteo-mcp-java.git
cd open-meteo-mcp-java
```

### Build the Project

**Maven:**
```bash
./mvnw clean install
```

**Gradle:**
```bash
./gradlew build
```

## Running Locally

### Development Mode

**Maven:**
```bash
./mvnw spring-boot:run
```

**Gradle:**
```bash
./gradlew bootRun
```

### With Spring AI Integration (Optional)

Set up environment variables for AI features:

```bash
export ANTHROPIC_API_KEY=your_api_key_here
# or
export OPENAI_API_KEY=your_api_key_here

./mvnw spring-boot:run
```

## Configuration

### Application Properties

Configure via `application.yml`:

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
    gzip-enabled: true
    cache-ttl-minutes: 30
```

## Testing

### Run All Tests

```bash
./mvnw test
```

### Run with Coverage

```bash
./mvnw test jacoco:report
# Report at: target/site/jacoco/index.html
```

### Run Integration Tests

```bash
./mvnw verify -P integration-tests
```

## Documentation

### Project Documentation

- **[CONSTITUTION.md](spec/CONSTITUTION.md)** - Project governance and migration strategy
- **[ADR_COMPENDIUM.md](spec/ADR_COMPENDIUM.md)** - 15 Architecture Decision Records
- **[MIGRATION_GUIDE.md](spec/MIGRATION_GUIDE.md)** - Python to Java migration guide
- **[CLAUDE.md](CLAUDE.md)** - AI-friendly development guide

### Python Reference Implementation

- **Repository**: [open-meteo-mcp (Python)](https://github.com/schlpbch/open-meteo-mcp)
- **Version**: v3.2.0 (production reference)
- **Status**: Production-ready with 4 tools, 5 resources, 3 prompts

## Project Structure

```
open-meteo-mcp-java/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/openmeteo/mcp/
│   │   │       ├── OpenMeteoMcpApplication.java
│   │   │       ├── config/          # Configuration classes
│   │   │       ├── tool/            # MCP tools (@McpTool)
│   │   │       ├── service/         # Business logic
│   │   │       ├── client/          # Open-Meteo API client
│   │   │       ├── model/           # Java Records (DTOs)
│   │   │       ├── exception/       # Exception handling
│   │   │       └── util/            # Utilities
│   │   └── resources/
│   │       ├── application.yml      # Configuration
│   │       ├── data/                # JSON resource files
│   │       └── logback-spring.xml   # Logging config
│   └── test/
│       └── java/
│           └── com/openmeteo/mcp/   # Test classes
├── spec/
│   ├── CONSTITUTION.md              # Project governance
│   ├── ADR_COMPENDIUM.md            # Architecture decisions
│   └── MIGRATION_GUIDE.md           # Migration guide
├── pom.xml                          # Maven configuration
├── README.md                        # This file
└── CLAUDE.md                        # AI development guide
```

## Migration Timeline

**9-Week Migration** (Q1-Q2 2026):

1. **Phase 1: Foundation** (Weeks 1-2) - ✅ Complete
   - Project setup, API client, 18 Java Records, comprehensive testing

2. **Phase 2: Services & Utilities** (Weeks 3-4) - ✅ Complete
   - 4 service classes, 4 utility classes, 87 unit tests (78-100% coverage)

3. **Phase 3: Resources & Prompts** (Week 5) - ✅ Complete
   - ResourceService with 4 resources, PromptService with 3 prompts, 112 tests (67% coverage)

4. **Phase 4: AI Enhancement** (Week 6)
   - Spring AI ChatClient integration, @McpTool annotations

5. **Phase 5: Testing & Documentation** (Weeks 7-8)
   - 80%+ test coverage, complete docs

6. **Phase 6: Deployment** (Week 9)
   - CI/CD, cloud deployment, v1.0.0 release

## Contributing

We welcome contributions during the migration phase!

### How to Contribute

1. Check the [CONSTITUTION.md](spec/CONSTITUTION.md) for governance principles
2. Review [ADR_COMPENDIUM.md](spec/ADR_COMPENDIUM.md) for architectural decisions
3. Follow [MIGRATION_GUIDE.md](spec/MIGRATION_GUIDE.md) for implementation patterns
4. Open an issue or pull request

### Code Standards

- **Specification-Driven Development**: Document before coding
- **Test Coverage**: ≥80% for new code
- **Code Style**: Follow Spring Boot best practices
- **Commit Messages**: Use Conventional Commits format

## MCP Integration

### Claude Desktop Configuration (Future)

Once v1.0.0 is released, add to Claude Desktop configuration:

**macOS**: `~/Library/Application Support/Claude/claude_desktop_config.json`
**Windows**: `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "open-meteo-java": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/open-meteo-mcp-java/target/open-meteo-mcp-1.0.0.jar"
      ]
    }
  }
}
```

## API Details

### Open-Meteo API

- **Geocoding**: `https://geocoding-api.open-meteo.com/v1/search`
- **Weather**: `https://api.open-meteo.com/v1/forecast`
- **Air Quality**: `https://air-quality-api.open-meteo.com/v1/air-quality`

**Features**:
- No API key required (free tier)
- Rate limiting: ~10,000 requests/day
- Gzip compression supported
- JSON responses with extensive parameters

## Roadmap

### v1.0.0 (Q2 2026) - Feature Parity

- ✅ All 4 tools match Python functionality
- ✅ All 5 resources available
- ✅ All 3 prompts working
- ✅ Gzip compression
- ✅ 80%+ test coverage
- ✅ Spring AI integration

### v1.1.0 - Enhanced Features

- Historical weather data access
- Weather alerts and notifications
- Extended forecast periods (beyond 16 days)
- Multi-location batch queries

### v2.0.0 - Advanced AI

- Predictive weather analysis
- Travel recommendation engine
- Weather pattern recognition
- Integration with other Swiss AI MCP servers

## Support

- **Issues**: [GitHub Issues](https://github.com/schlpbch/open-meteo-mcp-java/issues)
- **Discussions**: [GitHub Discussions](https://github.com/schlpbch/open-meteo-mcp-java/discussions)
- **Python Version**: [open-meteo-mcp](https://github.com/schlpbch/open-meteo-mcp)

## License

Apache License 2.0 - See [LICENSE](LICENSE) for details.

## Credits

- Weather data provided by [Open-Meteo](https://open-meteo.com/) - Free Open-Source Weather API
- Based on [open-meteo-mcp (Python)](https://github.com/schlpbch/open-meteo-mcp) v3.2.0
- Part of the Swiss AI MCP ecosystem

## Acknowledgments

Special thanks to:
- Open-Meteo team for their excellent free weather API
- Spring AI team for native MCP protocol support
- Anthropic for the Model Context Protocol specification
- Contributors to the Python reference implementation

---

**🔄 Migration in Progress** - Follow along on [GitHub](https://github.com/schlpbch/open-meteo-mcp-java) or check the [Python version](https://github.com/schlpbch/open-meteo-mcp) for a production-ready implementation.
