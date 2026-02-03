# Open Meteo MCP Server (Java)

A Model Context Protocol (MCP) server providing weather, snow conditions, and
air quality tools via the [Open-Meteo API](https://open-meteo.com/) with
conversational AI capabilities.

**Version**: 2.0.0 - Enterprise Ready ✅  
**Status**: Complete API documentation suite, Docker infrastructure, production
deployment ready  
**Release Date**: February 2, 2026  
**License**: Apache 2.0

## 🎉 Project Overview

### Key Features

- ✅ **11 MCP Tools**: 4 core + 7 advanced (weather, snow, air quality,
  location, alerts, astronomy, marine)
- ✅ **ChatHandler**: Conversational AI with Spring AI 2.0, function calling,
  Redis memory
- ✅ **4 MCP Resources**: weather codes, parameters, AQI reference, Swiss
  locations
- ✅ **3 MCP Prompts**: ski-trip, outdoor-activity, travel planning
- ✅ **360 Tests**: 100% pass rate, 81% coverage
- ✅ **Docker Infrastructure**: Multi-stage builds, Redis orchestration
- ✅ **Complete API Documentation**: MCP protocol, OpenAPI 3.0.3 specs

### Three API Endpoints

1. **🌐 REST API** - Traditional HTTP endpoints for direct integration
2. **🔗 MCP API** - Model Context Protocol for AI tools (Claude Desktop, IDEs)
3. **💬 Chat API** - Conversational interface with memory and streaming

### Current Implementation Status

- ✅ **11 MCP Tools**: 4 core + 7 advanced (weather, snow, air quality, location, alerts, astronomy, marine)
- ✅ **ChatHandler**: Conversational AI with Spring AI 2.0, function calling, Redis memory  
- ✅ **4 MCP Resources**: weather codes, parameters, AQI reference, Swiss locations
- ✅ **3 MCP Prompts**: ski-trip, outdoor-activity, travel planning
- ✅ **Enhanced Descriptions**: All components include comprehensive examples, features, use cases, and health guidelines
- ✅ **MCP Server Configuration**: Spring Boot with @McpTool/@McpPrompt/@McpResource annotations
- ✅ **SSE Transport**: Full MCP protocol support via HTTP/SSE at `/sse` endpoint
- ✅ **MCP Inspector Integration**: Tested with MCP Inspector web UI
- ✅ **Claude Desktop Integration**: Ready-to-use configuration files and setup scripts
- ✅ **Docker Infrastructure**: Multi-stage builds, Redis orchestration
- ✅ **Server Running**: Spring Boot 4.0.0 on port 8888

## Technology Stack

- **Java 25** with Virtual Threads, **Spring Boot 4.0**, **Spring AI 2.0**
- **Docker** (Eclipse Temurin), **Redis**, **Maven 3.9+**
- **AI Providers**: Azure OpenAI (primary), OpenAI, Anthropic Claude
- **Testing**: JUnit 5 + Mockito + AssertJ

## Quick Start

### Installation

```bash
git clone https://github.com/schlpbch/open-meteo-mcp-java.git
cd open-meteo-mcp-java
./mvnw clean install
```

### Run Application

```bash
# Java
./mvnw spring-boot:run

# Docker
docker compose up --build
```

### Test Endpoints

```bash
# Health check
curl http://localhost:8888/actuator/health

# MCP Protocol
curl http://localhost:8888/sse

# Chat API
curl -X POST http://localhost:8888/api/chat/sessions/test/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "What's the weather in Zurich?"}'

# REST API Examples
# Search location
curl -X POST http://localhost:8080/api/tools/search-location \
  -H "Content-Type: application/json" \
  -d '{"name":"London","count":5,"language":"en"}'

# Get weather
curl -X POST http://localhost:8080/api/tools/weather \
  -H "Content-Type: application/json" \
  -d '{"latitude":51.5074,"longitude":-0.1278,"forecastDays":7,"timezone":"UTC"}'

# Get snow conditions
curl -X POST http://localhost:8080/api/tools/snow-conditions \
  -H "Content-Type: application/json" \
  -d '{"latitude":46.5197,"longitude":10.2093,"forecastDays":7,"timezone":"Europe/Rome"}'

# Get air quality
curl -X POST http://localhost:8080/api/tools/air-quality \
  -H "Content-Type: application/json" \
  -d '{"latitude":52.52,"longitude":13.405,"forecastDays":7,"timezone":"Europe/Berlin"}'
```

## 🤖 Claude Desktop Integration

The project includes ready-to-use Claude Desktop configuration files in the [`claude-desktop/`](claude-desktop/) directory:

### Quick Setup

**Windows:**
```powershell
cd claude-desktop
.\setup.bat
```

**macOS/Linux:**
```bash
cd claude-desktop
chmod +x setup.sh
./setup.sh
```

### Manual Setup

1. **Build the project:**
   ```bash
   ./mvnw clean install
   ```

2. **Copy configuration:**
   - **Windows:** Copy `claude-desktop/claude-desktop.json` to `%USERPROFILE%\AppData\Roaming\Claude\mcp_servers.json`
   - **macOS:** Copy to `~/Library/Application Support/Claude/mcp_servers.json`
   - **Linux:** Copy to `~/.config/Claude/mcp_servers.json`

3. **Restart Claude Desktop** and test with: *"What's the weather like in London?"*

### Configuration Options

- **`open-meteo-mcp-java`** - Production mode using built JAR
- **`open-meteo-mcp-java-dev`** - Development mode using Maven

For remote development, Docker, and advanced configurations, see [`claude-desktop/README.md`](claude-desktop/README.md).

### With Spring AI Integration (Optional)

Set environment variables for Claude AI integration:

```bash
export ANTHROPIC_API_KEY=your_api_key_here

./mvnw spring-boot:run
```

## Configuration

### Environment Variables

```bash
# AI Provider Keys
AZURE_OPENAI_KEY=your_key
OPENAI_API_KEY=your_key
ANTHROPIC_API_KEY=your_key

# Redis (production)
REDIS_URL=redis://localhost:6379
```

### Application Properties

```yaml
openmeteo:
  api:
    weather-url: https://api.open-meteo.com/v1
    timeout-seconds: 30
    gzip-enabled: true
  chat:
    enabled: true
    memory:
      type: redis # or inmemory
```

## MCP Tools & Resources

### Core Tools

- `meteo__search_location` - Geocoding and location search
- `meteo__get_weather` - Weather forecasts with interpretations
- `meteo__get_snow_conditions` - Snow depth and ski conditions
- `meteo__get_air_quality` - AQI, pollutants, UV, pollen data

### Advanced Tools

- `meteo__get_weather_alerts` - Threshold-based weather alerts
- `meteo__get_comfort_index` - Outdoor activity comfort score (0-100)
- `meteo__get_astronomy` - Sunrise, sunset, moon phases
- `meteo__search_location_swiss` - Swiss-specific location search
- `meteo__compare_locations` - Multi-location comparison
- `meteo__get_historical_weather` - Historical data (1940-present)
- `meteo__get_marine_conditions` - Wave/swell data

### Resources & Prompts

- **Resources**: Weather codes, parameters, AQI scales, Swiss locations
- **Prompts**: Ski trip planning, outdoor activities, travel planning

## Documentation

### API Specifications

- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System design with three API
  endpoints
- **[docs/MCP_DOCUMENTATION.md](docs/MCP_DOCUMENTATION.md)** - Complete MCP
  protocol reference
- **[docs/openapi-open-meteo.yaml](docs/openapi-open-meteo.yaml)** - REST API
  specification
- **[docs/openapi-chat.yaml](docs/openapi-chat.yaml)** - Chat API specification

### Project Documentation

- **[CHATHANDLER_README.md](CHATHANDLER_README.md)** - ChatHandler
  implementation guide
- **[spec/CONSTITUTION.md](spec/CONSTITUTION.md)** - Project governance
- **[spec/ADR_COMPENDIUM.md](spec/ADR_COMPENDIUM.md)** - Architecture decisions
- **[CLAUDE.md](CLAUDE.md)** - AI development guide

## MCP Integration

### Claude Desktop Configuration

Add to `~/Library/Application Support/Claude/claude_desktop_config.json` (macOS)
or `%APPDATA%\Claude\claude_desktop_config.json` (Windows):

```json
{
  "mcpServers": {
    "open-meteo-java": {
      "command": "java",
      "args": ["-jar", "/path/to/open-meteo-mcp-2.0.0.jar"]
    }
  }
}
```

### MCP Inspector

```bash
npx @modelcontextprotocol/inspector http://localhost:8888/sse
```

## Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
# Report at: target/site/jacoco/index.html

# Integration tests
./mvnw verify -P integration-tests
```

## Docker Deployment

### Single Container

```bash
docker build -t open-meteo-mcp:2.0.0 .
docker run -p 8888:8888 open-meteo-mcp:2.0.0
```

### Full Stack with Redis

```bash
docker compose up --build
```

## Development

### Core Patterns

- **Java Records** for all DTOs (immutable, type-safe)
- **CompletableFuture** for async operations (no reactive Mono/Flux)
- **@McpTool/@McpResource/@McpPrompt** Spring AI annotations
- **≥80% test coverage** target

### Adding New Tools

```java
@Service
public class MyToolService {
    @McpTool(description = "Tool description")
    public CompletableFuture<MyResponse> myTool(
        @McpParam("param") String param
    ) {
        return myService.process(param);
    }
}
```

## API Endpoints

### REST API

- `GET /api/weather` - Weather forecasts
- `GET /api/air-quality` - Air quality data
- `GET /api/geocoding/search` - Location search
- `GET /api/snow` - Snow conditions

### Chat API

- `POST /api/chat/sessions/{id}/messages` - Send message
- `GET /api/chat/sessions/{id}` - Session details
- `DELETE /api/chat/sessions/{id}` - Delete session

### MCP Protocol

- `GET /sse` - Server-Sent Events endpoint for MCP clients

## v2.0.0 Release Highlights

- 📚 **Complete API Documentation Suite** - MCP protocol, OpenAPI specs, client
  examples
- 🐳 **Docker Infrastructure** - Multi-stage builds, Redis orchestration,
  production ready
- 🏗️ **Enhanced Architecture** - Three distinct APIs with comprehensive
  documentation
- 📖 **Professional Documentation** - Enterprise-grade specifications and guides
- 🔧 **Developer Experience** - Optimized development workflow and tools

## Support & Contributing

- **Issues**:
  [GitHub Issues](https://github.com/schlpbch/open-meteo-mcp-java/issues)
- **Python Reference**:
  [open-meteo-mcp](https://github.com/schlpbch/open-meteo-mcp) v3.2.0
- **Contributing**: See [CONSTITUTION.md](spec/CONSTITUTION.md) for guidelines

## License & Credits

- **License**: Apache 2.0
- **Weather Data**: [Open-Meteo](https://open-meteo.com/) - Free Open-Source
  Weather API
- **Framework**: Spring AI 2.0 with native MCP support
- **Protocol**: [Model Context Protocol](https://modelcontextprotocol.io/) by
  Anthropic

---

**v2.0.0**: ✅ **ENTERPRISE READY** - Complete documentation, Docker
infrastructure, production deployment ready
