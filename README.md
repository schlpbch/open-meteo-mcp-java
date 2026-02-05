# Open Meteo MCP Server (Java)

[![Version](https://img.shields.io/badge/version-2.0.2-blue.svg)](https://github.com/schlpbch/open-meteo-mcp-java/releases/tag/v2.0.2)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green.svg)](https://spring.io/projects/spring-boot)
[![Tests](https://img.shields.io/badge/tests-426%20passing-brightgreen.svg)](#testing)
[![Coverage](https://img.shields.io/badge/coverage-72%25-yellow.svg)](#testing)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![MCP](https://img.shields.io/badge/MCP-compatible-purple.svg)](https://modelcontextprotocol.io/)

**One unified server** providing weather, snow conditions, and air quality data
through **three API interfaces** (REST, MCP, Chat) - all powered by the same
core business logic - via the [Open-Meteo API](https://open-meteo.com/).

## Overview

**Unified Architecture:** Single server application with shared business logic
exposing three different API interfaces:

| Interface    | Endpoint      | Purpose                                           |
| ------------ | ------------- | ------------------------------------------------- |
| **REST API** | `/api/*`      | Direct HTTP/JSON access to weather services       |
| **MCP API**  | `/sse`        | Model Context Protocol for AI assistants (Claude) |
| **Chat API** | `/api/chat/*` | Conversational AI interface with memory           |

**Shared Capabilities:**

- **11 MCP Tools** - Weather, snow, air quality, location, alerts, astronomy,
  marine
- **4 MCP Resources** - Weather codes, parameters, AQI reference, Swiss
  locations
- **3 MCP Prompts** - Ski-trip, outdoor-activity, travel planning

## Quick Start

```bash
# Clone and build
git clone https://github.com/schlpbch/open-meteo-mcp-java.git
cd open-meteo-mcp-java
mvn clean install

# Run
mvn spring-boot:run

# Or with Docker
docker compose up --build
```

**Endpoints:**

- Health: http://localhost:8888/actuator/health
- REST: http://localhost:8888/api/weather (and other `/api/*` endpoints)
- MCP: http://localhost:8888/sse
- Chat: http://localhost:8888/api/chat

> **Architecture:** All three APIs share the same weather services, business
> logic, and data models. Choose the interface that best fits your use case -
> REST for direct integration, MCP for AI assistants, or Chat for conversational
> interactions.

## MCP Tools

### Core Tools

| Tool                  | Description                                      |
| --------------------- | ------------------------------------------------ |
| `search_location`     | Geocoding - search locations by name             |
| `get_weather`         | Weather forecast with temperature, precipitation |
| `get_snow_conditions` | Snow depth, snowfall, ski conditions             |
| `get_air_quality`     | AQI, pollutants, UV index, pollen                |

### Advanced Tools

| Tool                     | Description                              |
| ------------------------ | ---------------------------------------- |
| `get_weather_alerts`     | Weather alerts based on thresholds       |
| `get_comfort_index`      | Outdoor activity comfort score (0-100)   |
| `get_astronomy`          | Sunrise, sunset, golden hour, moon phase |
| `search_location_swiss`  | Swiss-specific location search           |
| `compare_locations`      | Multi-location weather comparison        |
| `get_historical_weather` | Historical data (1940-present)           |
| `get_marine_conditions`  | Wave/swell data for lakes and coasts     |

## Claude Desktop Integration

Add to your Claude Desktop config:

**macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
**Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

```json
{
  "mcpServers": {
    "open-meteo": {
      "command": "java",
      "args": ["-jar", "/path/to/open-meteo-mcp-2.0.2.jar"]
    }
  }
}
```

Or use the setup scripts in [`claude-desktop/`](claude-desktop/).

## Configuration

```bash
# Environment variables
AZURE_OPENAI_KEY=your_key
OPENAI_API_KEY=your_key
ANTHROPIC_API_KEY=your_key
REDIS_URL=redis://localhost:6379
```

```yaml
# application.yml
openmeteo:
  chat:
    enabled: true
    memory:
      type: redis # or inmemory
```

## Testing

```bash
mvn test                    # Run all tests
mvn test jacoco:report      # With coverage report
```

**Current:** 426 tests passing, 72% coverage

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md) - System design
- [docs/MCP_DOCUMENTATION.md](docs/MCP_DOCUMENTATION.md) - MCP protocol
  reference
- [docs/openapi-open-meteo.yaml](docs/openapi-open-meteo.yaml) - REST API spec
- [docs/openapi-chat.yaml](docs/openapi-chat.yaml) - Chat API spec
- [CLAUDE.md](CLAUDE.md) - AI development guide

## Technology Stack

- **Java 25** with Virtual Threads
- **Spring Boot 4.0** / **Spring AI 2.0**
- **Docker** (Eclipse Temurin) / **Redis**
- **JUnit 5** + Mockito + AssertJ

## License

Apache 2.0 - See [LICENSE](LICENSE)

**Credits:**

- Weather data: [Open-Meteo](https://open-meteo.com/)
- Protocol: [Model Context Protocol](https://modelcontextprotocol.io/)

---

**v2.0.2** - Enterprise Ready
