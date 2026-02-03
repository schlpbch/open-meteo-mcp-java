# ChatHandler Feature (v1.2.0)

Conversational AI interface for the Open-Meteo MCP server, enabling natural
language weather queries with context awareness and function calling.

## Quick Start

### 1. Configuration

Set environment variables:

```bash
export AZURE_OPENAI_API_KEY=your-api-key
export AZURE_OPENAI_ENDPOINT=https://your-endpoint.openai.azure.com
export OPENMETEO_CHAT_ENABLED=true
```

### 2. Run Locally

**With in-memory storage:**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=chat
```

**With Redis:**

```bash
# Start Redis
docker run -d -p 6379:6379 redis:alpine

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=chat \
  -Dopenmeteo.chat.memory.type=redis
```

**With Docker Compose:**

```bash
docker-compose -f docker-compose-chat.yml up
```

### 3. Test the API

```bash
# Send a message
curl -X POST http://localhost:8080/api/chat/sessions/user-123/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "What'\''s the weather in Zurich?"}'

# Get session info
curl http://localhost:8080/api/chat/sessions/user-123

# Stream response (SSE)
curl -N http://localhost:8080/api/chat/stream/sessions/user-123/messages \
  -H "Content-Type: application/json" \
  -d '{"message": "How about tomorrow?"}'
```

## Features

### ✅ Core Capabilities

- **Async Conversation Memory**: InMemory or Redis-backed
- **Context Awareness**: Location tracking, user preferences
- **RAG Foundation**: Knowledge document integration
- **Function Calling**: 11 MCP weather tools
- **Streaming Responses**: Server-Sent Events (SSE)
- **Production Observability**: Metrics via Micrometer

### 🔧 Supported LLM Providers

- Azure OpenAI (default)
- OpenAI
- Anthropic Claude

### 📊 Available Tools

1. `search_location` - Geocoding
2. `get_weather` - Weather forecast
3. `get_snow_conditions` - Snow conditions
4. `get_air_quality` - Air quality & pollen
5. `get_weather_alerts` - Weather alerts
6. `get_comfort_index` - Comfort index
7. `get_astronomy` - Sunrise/sunset
8. `search_location_swiss` - Swiss locations
9. `compare_locations` - Location comparison
10. `get_historical_weather` - Historical data
11. `get_marine_conditions` - Wave/swell data

## Configuration

See
[`application-chat.properties`](src/main/resources/application-chat.properties)
for all options.

### Key Settings

```properties
# Enable/disable
openmeteo.chat.enabled=true

# Memory type
openmeteo.chat.memory.type=inmemory  # or 'redis'

# Session TTL (minutes)
openmeteo.chat.memory.session-ttl=1440

# LLM provider
spring.ai.azure.openai.chat.options.deployment-name=gpt-4
spring.ai.azure.openai.chat.options.temperature=0.7
```

## API Endpoints

### Chat Operations

**POST** `/api/chat/sessions/{sessionId}/messages`

- Send a message and get AI response
- Request: `{"message": "string"}`
- Response: `{"content": "string", "metadata": {...}}`

**GET** `/api/chat/sessions/{sessionId}`

- Get session information
- Response: Session with context and timestamps

**DELETE** `/api/chat/sessions/{sessionId}`

- Delete a session and its messages

### Streaming (SSE)

**POST** `/api/chat/stream/sessions/{sessionId}/messages`

- Stream AI response in real-time
- Events: `start`, `chunk`, `complete`, `error`

**GET** `/api/chat/stream/health`

- Health check for streaming endpoint

## Metrics

Available via `/actuator/metrics`:

- `chat.requests.total` - Total requests
- `chat.requests.success` - Successful requests
- `chat.requests.failure` - Failed requests
- `chat.response.time` - Response time histogram
- `chat.sessions.active` - Active sessions gauge

## Examples

See
[`ChatHandlerExample.java`](src/main/java/com/openmeteo/mcp/chat/example/ChatHandlerExample.java)
for integration examples:

```bash
mvn spring-boot:run -Dopenmeteo.chat.example.enabled=true
```

## Architecture

```
ChatController → ChatHandler → ChatModel (Spring AI)
                      ↓              ↓
                 Memory Service  Function Calling
                      ↓              ↓
                 InMemory/Redis  11 MCP Tools
```

## Testing

```bash
# Run all ChatHandler tests
mvn test -Dtest="**/chat/**/*Test"

# Specific tests
mvn test -Dtest="InMemoryConversationMemoryServiceTest"
mvn test -Dtest="ContextEnrichmentServiceTest"
```

**Test Coverage**: 17 unit tests (100% pass rate)

## Deployment

### Docker

```bash
# Build image
docker build -t openmeteo-mcp:1.2.0 .

# Run with environment variables
docker run -p 8080:8080 \
  -e AZURE_OPENAI_API_KEY=your-key \
  -e AZURE_OPENAI_ENDPOINT=your-endpoint \
  -e OPENMETEO_CHAT_ENABLED=true \
  openmeteo-mcp:1.2.0
```

### Production Checklist

- [ ] Set `AZURE_OPENAI_API_KEY` environment variable
- [ ] Set `AZURE_OPENAI_ENDPOINT` environment variable
- [ ] Configure Redis for production (`openmeteo.chat.memory.type=redis`)
- [ ] Enable metrics endpoint (`/actuator/metrics`)
- [ ] Configure logging level
- [ ] Set appropriate session TTL
- [ ] Enable health checks
- [ ] Configure resource limits

## Troubleshooting

### Common Issues

**Build fails with Spring AI errors**

- Ensure Spring AI 2.0.0-M2 is in dependencies
- Check that `ChatModel` bean is available

**Redis connection fails**

- Verify Redis is running: `redis-cli ping`
- Check `spring.data.redis.host` and `port`

**LLM API errors**

- Verify API key is set correctly
- Check endpoint URL format
- Review deployment name matches your Azure resource

**Function calling not working**

- Ensure `openmeteo.chat.enabled=true`
- Check that MCP tools are registered as beans
- Verify LLM supports function calling

## Documentation

- [Implementation Plan](CHATHANDLER_IMPLEMENTATION_PLAN.md)
- [Walkthrough](../../../.gemini/antigravity/brain/2606324f-ce32-4e44-9a69-a6782ee9841b/chathandler_walkthrough.md)
- [ADR-018: ChatHandler Architecture](docs/adr/ADR-018-chathandler-architecture.md)

## Version History

**v1.2.0** (2026-02-02)

- Initial ChatHandler implementation
- 11 MCP tools integration
- Redis conversation memory
- SSE streaming support
- Production metrics

## License

Same as Open-Meteo MCP Server
