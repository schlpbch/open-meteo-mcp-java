# Architecture Diagram - Open-Meteo MCP Java

## System Overview

The Open-Meteo MCP Java server provides weather, air quality, and conversational
AI capabilities through **three distinct API endpoints** in a modular,
containerized architecture:

### 🔌 **Three API Endpoints**

1. **🌐 REST API** - Traditional HTTP REST endpoints for direct integration
2. **🔗 MCP API** - Model Context Protocol for AI tool integration (Claude
   Desktop, IDEs)
3. **💬 Chat API** - Conversational interface with weather expertise and memory

## High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        REST_CLIENT[REST Clients<br/>curl, Postman, etc.]
        MCP_CLIENT[MCP Clients<br/>Claude Desktop, IDE, etc.]
        CHAT_CLIENT[Chat Clients<br/>Web UI, Mobile Apps]
    end

    subgraph "Open-Meteo MCP Server"
        subgraph "API Endpoints"
            REST_API[🌐 REST API<br/>HTTP/JSON Endpoints]
            MCP_API[🔗 MCP API<br/>SSE Protocol]
            CHAT_API[💬 Chat API<br/>Conversational Interface]
        end

        subgraph "Core Services"
            WEATHER[Weather Service]
            AIR[Air Quality Service]
            GEO[Geocoding Service]
            MARINE[Marine Service]
            CHAT[Chat Handler]
        end

        subgraph "AI Integration"
            AI_ROUTER[AI Provider Router]
            AZURE[Azure OpenAI]
            OPENAI[OpenAI]
            ANTHROPIC[Anthropic Claude]
        end

        subgraph "Data & Memory"
            MEMORY[Conversation Memory]
            REDIS[(Redis Cache)]
        end
    end

    subgraph "External APIs"
        OPENMETEO_API[Open-Meteo API<br/>Weather Data]
        GEOCODING_API[Geocoding API<br/>Location Search]
    end

    REST_CLIENT --> REST_API
    MCP_CLIENT --> MCP_API
    CHAT_CLIENT --> CHAT_API

    REST_API --> WEATHER
    REST_API --> AIR
    REST_API --> GEO
    REST_API --> MARINE

    MCP_API --> WEATHER
    MCP_API --> AIR
    MCP_API --> GEO
    MCP_API --> MARINE
    MCP_API --> CHAT

    CHAT_API --> CHAT

    CHAT --> AI_ROUTER
    AI_ROUTER --> AZURE
    AI_ROUTER --> OPENAI
    AI_ROUTER --> ANTHROPIC

    CHAT --> MEMORY
    MEMORY --> REDIS

    WEATHER --> OPENMETEO_API
    AIR --> OPENMETEO_API
    GEO --> GEOCODING_API
    MARINE --> OPENMETEO_API
```

## Component Architecture

```mermaid
graph TB
    subgraph "API Layer"
        REST[REST API Controller]
        MCP[MCP Protocol Handler]
        CHAT[Chat API Controller]
        TOOLS[Tool Registry]
        RESOURCES[Resource Registry]
    end

    subgraph "Service Layer"
        WS[Weather Service]
        AQS[Air Quality Service]
        GS[Geocoding Service]
        MS[Marine Service]
        CS[Chat Service]
    end

    subgraph "Integration Layer"
        HTTP[HTTP Client]
        AI[AI Provider Manager]
        CACHE[Cache Manager]
    end

    subgraph "Model Layer"
        WEATHER_MODEL[Weather Models]
        REQUEST_MODEL[Request Models]
        RESPONSE_MODEL[Response Models]
        CHAT_MODEL[Chat Models]
    end

    subgraph "Infrastructure"
        CONFIG[Configuration]
        METRICS[Metrics & Health]
        LOGGING[Logging]
    end

    REST --> WS
    REST --> AQS
    REST --> GS
    REST --> MS

    MCP --> TOOLS
    MCP --> RESOURCES
    TOOLS --> WS
    TOOLS --> AQS
    TOOLS --> GS
    TOOLS --> MS
    TOOLS --> CS

    CHAT --> CS

    WS --> HTTP
    AQS --> HTTP
    GS --> HTTP
    MS --> HTTP
    CS --> AI
    CS --> CACHE

    WS --> WEATHER_MODEL
    AQS --> WEATHER_MODEL
    GS --> REQUEST_MODEL
    MS --> WEATHER_MODEL
    CS --> CHAT_MODEL

    CONFIG --> WS
    CONFIG --> AQS
    CONFIG --> GS
    CONFIG --> MS
    CONFIG --> CS

    METRICS --> REST
    METRICS --> MCP
    METRICS --> CHAT
    LOGGING --> REST
    LOGGING --> MCP
    LOGGING --> CHAT
```

## Deployment Architecture

```mermaid
graph TB
    subgraph "Docker Compose Stack"
        subgraph "App Container"
            APP[Open-Meteo MCP<br/>Java 25<br/>Port: 8080]
            HEALTH[Health Check<br/>/actuator/health]
        end

        subgraph "Cache Container"
            REDIS_CONTAINER[Redis 7-Alpine<br/>Port: 6379<br/>Persistent Volume]
        end

        subgraph "Network"
            NETWORK[openmeteo-network<br/>Bridge Network]
        end
    end

    subgraph "External Services"
        OPENMETEO[Open-Meteo APIs]
        AI_PROVIDERS[AI Provider APIs<br/>Azure/OpenAI/Anthropic]
    end

    subgraph "Configuration"
        ENV[Environment Variables<br/>.env file]
        VOLUMES[Docker Volumes<br/>redis-data]
    end

    APP --> REDIS_CONTAINER
    APP --> OPENMETEO
    APP --> AI_PROVIDERS

    ENV --> APP
    ENV --> REDIS_CONTAINER

    VOLUMES --> REDIS_CONTAINER

    NETWORK --> APP
    NETWORK --> REDIS_CONTAINER

    HEALTH --> APP
```

## Sequence Flows

### REST API Request Flow

```mermaid
sequenceDiagram
    participant Client as REST Client
    participant RestAPI as REST API Controller
    participant Weather as Weather Service
    participant OpenMeteo as Open-Meteo API

    Client->>RestAPI: HTTP GET /api/weather?lat=47.3&lon=8.5
    activate RestAPI

    RestAPI->>RestAPI: validateParameters()
    RestAPI->>Weather: getWeatherForecast(params)
    activate Weather

    Weather->>Weather: buildApiRequest()
    Weather->>OpenMeteo: HTTP GET /forecast
    activate OpenMeteo
    OpenMeteo-->>Weather: Weather Data JSON
    deactivate OpenMeteo

    Weather->>Weather: parseAndEnrich()
    Weather-->>RestAPI: WeatherForecast Object
    deactivate Weather

    RestAPI->>RestAPI: formatJsonResponse()
    RestAPI-->>Client: HTTP 200 + JSON Response
    deactivate RestAPI
```

### MCP Tool Request Flow

```mermaid
sequenceDiagram
    participant Client as MCP Client
    participant McpAPI as MCP API Handler
    participant Weather as Weather Service
    participant OpenMeteo as Open-Meteo API

    Client->>McpAPI: MCP Tool Call<br/>get_weather_forecast
    activate McpAPI

    McpAPI->>Weather: processWeatherRequest(params)
    activate Weather

    Weather->>Weather: validateParameters()
    Weather->>Weather: buildApiRequest()

    Weather->>OpenMeteo: HTTP GET /forecast
    activate OpenMeteo
    OpenMeteo-->>Weather: Weather Data JSON
    deactivate OpenMeteo

    Weather->>Weather: parseResponse()
    Weather->>Weather: enrichWithCalculations()
    Weather-->>McpAPI: WeatherForecast Object
    deactivate Weather

    McpAPI->>McpAPI: formatMcpResponse()
    McpAPI-->>Client: MCP Tool Response
    deactivate McpAPI
```

### Chat API Interaction Flow

```mermaid
sequenceDiagram
    participant Client as ChatClient
    participant ChatAPI as Chat API Controller
    participant Chat as Chat Handler
    participant Memory as Conversation Memory
    participant Redis as Redis Cache
    participant AI as AI Provider

    Client->>ChatAPI: HTTP POST /api/chat
    activate ChatAPI

    ChatAPI->>Chat: processMessage(query, sessionId)
    activate Chat

    Chat->>Memory: getConversationHistory(sessionId)
    activate Memory
    Memory->>Redis: GET session:sessionId
    activate Redis
    Redis-->>Memory: Conversation Data
    deactivate Redis
    Memory-->>Chat: Message History
    deactivate Memory

    Chat->>Chat: enrichContextWithWeatherData()
    Chat->>Chat: buildAiPrompt(query, history, context)

    Chat->>AI: sendChatRequest(prompt)
    activate AI
    AI-->>Chat: AI Response
    deactivate AI

    Chat->>Memory: saveMessage(sessionId, query, response)
    activate Memory
    Memory->>Redis: SET session:sessionId
    Redis-->>Memory: OK
    deactivate Memory
    deactivate Memory

    Chat-->>ChatAPI: Chat Response
    deactivate Chat

    ChatAPI-->>Client: HTTP 200 + JSON Response
    deactivate ChatAPI
```

### Application Startup Flow

```mermaid
sequenceDiagram
    participant Docker as Docker Compose
    participant Redis as Redis Container
    participant App as App Container
    participant Config as Configuration
    participant MCP as MCP Server
    participant Health as Health Check

    Docker->>Redis: Start Redis Container
    activate Redis
    Redis->>Redis: Initialize & Load Data
    Redis-->>Docker: Container Ready

    Docker->>App: Start App Container
    activate App

    App->>Config: Load Environment Variables
    activate Config
    Config->>Config: Validate AI Provider Keys
    Config->>Config: Setup Redis Connection
    Config-->>App: Configuration Ready
    deactivate Config

    App->>MCP: Initialize MCP Server
    activate MCP
    MCP->>MCP: Register Tools & Resources
    MCP->>MCP: Setup Protocol Handlers
    MCP-->>App: Server Ready

    App->>Health: Start Health Endpoints
    activate Health
    Health-->>Docker: Health Check OK

    Docker->>Docker: All Services Ready
    deactivate Redis
    deactivate App
    deactivate MCP
    deactivate Health
```

### Error Handling Flow

```mermaid
sequenceDiagram
    participant Client as Any Client<br/>(REST/MCP/Chat)
    participant API as API Layer<br/>(REST/MCP/Chat)
    participant Service as Service Layer
    participant External as External API
    participant Fallback as Fallback Handler

    Client->>API: API Request
    activate API

    API->>Service: Process Request
    activate Service

    Service->>External: API Call
    activate External
    External-->>Service: Error Response
    deactivate External

    Service->>Service: Handle Error

    alt Retryable Error
        Service->>External: Retry API Call
        External-->>Service: Success/Failure
    else Non-Retryable Error
        Service->>Fallback: Use Fallback Strategy
        activate Fallback
        Fallback-->>Service: Fallback Response
        deactivate Fallback
    end

    Service-->>API: Result/Error
    deactivate Service

    API->>API: Format Error Response<br/>(JSON/MCP/Chat)
    API-->>Client: API Error Response
    deactivate API
```

## Key Design Principles

### 1. **Multi-API Architecture**

- **🌐 REST API**: Traditional HTTP/JSON for direct system integration
- **🔗 MCP API**: Model Context Protocol for AI tool ecosystems (Claude Desktop,
  IDEs)
- **💬 Chat API**: Conversational interface with weather expertise and
  persistent memory
- Each API serves different use cases while sharing the same core business logic

### 2. **Modular Architecture**

- Separation of concerns between API layers, business logic, and external
  integrations
- Each service is independently testable and maintainable
- Clear interfaces between layers

### 3. **Resilience & Reliability**

- Multiple AI provider fallbacks
- Redis-based conversation memory with TTL
- Comprehensive error handling and retry mechanisms
- Health checks and monitoring endpoints

### 4. **Scalability**

- Stateless application design (state in Redis)
- Docker containerization for horizontal scaling
- Efficient caching strategies
- Resource pooling for HTTP connections

### 5. **Security**

- Environment-based configuration
- Non-root container execution
- API key management through environment variables
- Input validation and sanitization

### 6. **Observability**

- Structured logging with correlation IDs
- Metrics collection (Prometheus-compatible)
- Health check endpoints
- Distributed tracing capabilities

## Technology Stack

- **Runtime**: Java 25, Spring Boot 4.0
- **API Endpoints**:
  - **REST**: Spring WebMVC with HTTP/JSON
  - **MCP**: Model Context Protocol with SSE
  - **Chat**: Spring WebMVC with conversation memory
- **AI Integration**: Spring AI with multiple providers
- **Caching**: Redis for conversation memory
- **HTTP Client**: Spring WebFlux (reactive)
- **Containerization**: Docker with multi-stage builds
- **Orchestration**: Docker Compose
- **Monitoring**: Spring Boot Actuator, Micrometer
- **Configuration**: Spring Boot Configuration with environment variables
- **Testing**: JUnit 5, TestContainers, Mockito
