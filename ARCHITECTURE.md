# Architecture Diagram - Open-Meteo MCP Java

## System Overview

The Open-Meteo MCP Java server provides weather, air quality, and conversational
AI capabilities through **three distinct API endpoints** with **enterprise-grade
security** and **real-time streaming** in a modular, containerized architecture:

### 🔌 **Three API Endpoints**

1. **🌐 REST API** - Traditional HTTP REST endpoints for direct integration
2. **🔗 MCP API** - Model Context Protocol for AI tool integration (Claude
   Desktop, IDEs)
3. **💬 Chat API** - Conversational interface with weather expertise and memory
4. **📡 Streaming API** - Real-time SSE streaming for weather data and AI chat
   responses

### 🔐 **Enterprise Security** (Phase 1-2)

- **Dual Authentication**: JWT tokens + API keys for flexible client support
- **Role-Based Authorization**: PUBLIC, MCP_CLIENT, ADMIN access levels
- **Spring Security**: OAuth2 resource server with reactive WebFlux
- **Security Headers**: XSS protection, CORS, frame options, content-type
  security
- **Audit Logging**: Comprehensive authentication and authorization event
  tracking

### 📡 **Real-Time Streaming** (Phase 3-5)

- **Server-Sent Events (SSE)**: Standard HTTP streaming protocol
- **Weather Streaming**: Real-time weather data with <2s first chunk latency
- **Chat Streaming**: Token-by-token AI responses with <100ms delay
- **Progress Indicators**: 4-step progress tracking for long operations
- **Connection Management**: Support for 100+ concurrent streaming connections

## High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        REST_CLIENT[REST Clients<br/>curl, Postman, etc.]
        MCP_CLIENT[MCP Clients<br/>Claude Desktop, IDE, etc.]
        CHAT_CLIENT[Chat Clients<br/>Web UI, Mobile Apps]
        STREAM_CLIENT[Streaming Clients<br/>SSE/EventSource]
    end

    subgraph "Open-Meteo MCP Server"
        subgraph "Security Layer - Phase 1-2"
            JWT_AUTH[JWT Authentication<br/>HMAC-SHA512]
            API_KEY_AUTH[API Key Authentication<br/>Role Validation]
            AUTHZ[Authorization<br/>RBAC - PUBLIC/CLIENT/ADMIN]
            AUDIT[Security Audit<br/>Event Logging]
        end

        subgraph "API Endpoints"
            REST_API[🌐 REST API<br/>HTTP/JSON Endpoints]
            MCP_API[🔗 MCP API<br/>SSE Protocol]
            CHAT_API[💬 Chat API<br/>Conversational Interface]
            STREAM_API[📡 Streaming API<br/>Real-time SSE]
        end

        subgraph "Streaming Services - Phase 3-5"
            STREAM_WEATHER[Weather Streaming<br/><2s First Chunk]
            STREAM_CHAT[Chat Streaming<br/><100ms Token Delay]
            STREAM_MGR[Connection Manager<br/>100+ Concurrent]
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
            API_KEYS[(API Keys Storage)]
        end
    end

    subgraph "External APIs"
        OPENMETEO_API[Open-Meteo API<br/>Weather Data]
        GEOCODING_API[Geocoding API<br/>Location Search]
    end

    REST_CLIENT --> JWT_AUTH
    REST_CLIENT --> API_KEY_AUTH
    MCP_CLIENT --> JWT_AUTH
    MCP_CLIENT --> API_KEY_AUTH
    CHAT_CLIENT --> JWT_AUTH
    STREAM_CLIENT --> JWT_AUTH
    STREAM_CLIENT --> API_KEY_AUTH

    JWT_AUTH --> AUTHZ
    API_KEY_AUTH --> AUTHZ
    AUTHZ --> AUDIT

    AUTHZ --> REST_API
    AUTHZ --> MCP_API
    AUTHZ --> CHAT_API
    AUTHZ --> STREAM_API

    REST_API --> WEATHER
    REST_API --> AIR
    REST_API --> GEO
    REST_API --> MARINE

    MCP_API --> WEATHER
    MCP_API --> AIR
    MCP_API --> GEO
    MCP_API --> MARINE
    MCP_API --> CHAT

    STREAM_API --> STREAM_WEATHER
    STREAM_API --> STREAM_CHAT
    STREAM_MGR --> STREAM_WEATHER
    STREAM_MGR --> STREAM_CHAT

    STREAM_WEATHER --> WEATHER
    STREAM_CHAT --> CHAT

    CHAT_API --> CHAT

    API_KEY_AUTH --> API_KEYS

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
- **📡 Streaming API**: Real-time SSE for weather data and AI chat responses
- Each API serves different use cases while sharing the same core business logic

### 2. **Enterprise Security** (Phase 1-2)

- **Dual Authentication**: JWT tokens (HMAC-SHA512) + API keys with role
  validation
- **Role-Based Access Control**: PUBLIC, MCP_CLIENT, ADMIN with method-level
  security
- **Spring Security**: OAuth2 resource server with reactive WebFlux integration
- **Security Headers**: XSS protection, CORS configuration, frame options,
  content-type security
- **Audit Logging**: Comprehensive authentication/authorization event tracking
  (10,000 events)
- **API Key Management**: Admin-only endpoints for generation and validation
- **Token Management**: 24h access tokens, 7d refresh tokens, configurable
  expiration

### 3. **Real-Time Streaming** (Phase 3-5)

- **Server-Sent Events (SSE)**: Standard HTTP streaming protocol with structured
  messages
- **Weather Streaming**: Real-time weather data with <2s first chunk latency
- **Chat Streaming**: Token-by-token AI responses with <100ms inter-token delay
- **Progress Tracking**: 4-step progress indicators (25%, 50%, 75%, 100%)
- **Connection Management**: Support for 100+ concurrent streaming connections
- **Backpressure Handling**: Reactive streams with Flux for efficient resource
  usage
- **Graceful Termination**: Proper cleanup on cancel/error/complete signals

### 4. **Modular Architecture**

- Separation of concerns between security, API layers, business logic, and
  external integrations
- Each service is independently testable and maintainable
- Clear interfaces between layers with dependency injection
- Security layer protects all API endpoints uniformly

### 5. **Resilience & Reliability**

- Multiple AI provider fallbacks
- Redis-based conversation memory with TTL
- Comprehensive error handling and retry mechanisms
- Health checks and monitoring endpoints
- Stream error recovery and reconnection support

### 6. **Scalability**

- Stateless application design (state in Redis)
- Docker containerization for horizontal scaling
- Efficient caching strategies
- Resource pooling for HTTP connections
- Reactive programming for efficient resource utilization
- Connection limits and backpressure management

### 7. **Security Hardening** (Phase 6)

- Zero critical vulnerabilities (security audit passed)
- OWASP Top 10 compliance verified
- Comprehensive integration testing (15+ E2E tests)
- Performance benchmarks met (JWT <50ms, API key <100ms)
- Production-ready deployment guide
- Environment-based configuration with mandatory JWT secret

### 8. **Observability**

- Structured logging with correlation IDs
- Metrics collection (Prometheus-compatible)
- Health check endpoints
- Distributed tracing capabilities
- Security audit event logging
- Streaming metrics and monitoring

## Technology Stack

### Core Runtime

- **Runtime**: Java 25, Spring Boot 4.0.1
- **Framework**: Spring Framework 7.x with reactive support

### API Layer

- **REST API**: Spring WebMVC with HTTP/JSON
- **MCP API**: Model Context Protocol with SSE
- **Chat API**: Spring WebMVC with conversation memory
- **Streaming API**: Spring WebFlux with Server-Sent Events (SSE)

### Security (Phase 1-2)

- **Authentication**: Spring Security 7 with OAuth2 Resource Server
- **JWT**: JJWT 0.11.5 with HMAC-SHA512 signing
- **Authorization**: Method-level security with @PreAuthorize
- **Security Headers**: Custom security configuration
- **Audit**: Custom SecurityAuditService with event logging

### Streaming Infrastructure (Phase 3-5)

- **Reactive Streams**: Spring WebFlux with Project Reactor
- **SSE Protocol**: Server-Sent Events with Flux<ServerSentEvent<T>>
- **Stream Models**: StreamMessage, StreamMetadata, StreamChunk
- **Connection Management**: StreamConnectionManager with concurrent limits
- **Chat Streaming**: Spring AI ChatModel.stream() integration

### AI Integration

- **Spring AI**: 2.0.0-M2 with multiple provider support
- **Providers**: Azure OpenAI, OpenAI, Anthropic Claude
- **Streaming**: Native LLM token-by-token streaming

### Data & Caching

- **Memory**: Redis for conversation memory and session management
- **Storage**: In-memory ConcurrentHashMap for API keys (optional Redis)
- **Session TTL**: 24-hour conversation memory expiration

### HTTP & External APIs

- **HTTP Client**: Spring WebFlux WebClient (reactive)
- **External APIs**: Open-Meteo weather, geocoding, air quality

### Containerization & Deployment

- **Containerization**: Docker with multi-stage builds
- **Orchestration**: Docker Compose with Redis service
- **Base Image**: Eclipse Temurin Java 25
- **Security**: Non-root container execution

### Observability & Monitoring

- **Monitoring**: Spring Boot Actuator, Micrometer
- **Metrics**: Prometheus-compatible metrics export
- **Health Checks**: Custom health indicators for dependencies
- **Logging**: SLF4J with Logback, structured logging

### Testing (Phase 6)

- **Unit Testing**: JUnit 5, Mockito, 426 tests
- **Integration Testing**: @SpringBootTest, WebTestClient
- **Reactive Testing**: StepVerifier for Flux/Mono validation
- **Performance**: Benchmark tests for latency validation
- **Security Testing**: Authentication/authorization E2E tests
- **Test Coverage**: 72% overall coverage

### Configuration & Build

- **Configuration**: Spring Boot Configuration with environment variables
- **Build Tool**: Maven 3.9+ with multi-module support
- **Plugins**: Spring Boot Maven Plugin, Surefire 3.3.0

## Implementation Timeline (Issue #10)

The current architecture was built through a comprehensive 6-phase, 11-week
implementation plan:

### **Phase 1: Security Foundation** (Weeks 1-2) ✅

**Commit**: `98848aa`

- Spring Security 7 with OAuth2 Resource Server configuration
- JWT token provider with HMAC-SHA512 signing (JJWT 0.11.5)
- API key authentication filter with role validation
- Security configuration classes and basic integration tests
- Foundation for enterprise-grade authentication

### **Phase 2: Security Integration** (Weeks 3-4) ✅

**Commit**: `411fc2c`

- API key service with role management (PUBLIC, MCP_CLIENT, ADMIN)
- Security management endpoints (admin-only)
- Authorization configuration with @PreAuthorize method security
- Security audit logging with event tracking (10,000 events)
- Complete authentication/authorization integration

### **Phase 3: Streaming Infrastructure** (Weeks 5-6) ✅

**Commit**: `d25315c`

- Spring WebFlux configuration for reactive streaming
- MCP Streamable HTTP protocol implementation
- Stream data models: StreamMessage, StreamMetadata, StreamChunk
- Basic streaming endpoints with security integration
- StreamConnectionManager for concurrent connection management (100+ streams)

### **Phase 4: Weather Streaming** (Weeks 7-8) ✅

**Commit**: `70cb82b`

- StreamingWeatherService with reactive data fetching
- Weather streaming endpoints with chunking and progress indicators
- Real-time current conditions, forecasts, and historical data streaming
- <2s first chunk latency achieved
- Performance testing and optimization

### **Phase 5: Chat Streaming** (Weeks 9-10) ✅

**Commit**: `369fe66`

- StreamingChatService with Spring AI integration
- Token-by-token chat response delivery via ChatModel.stream()
- Three streaming endpoints: simple chat, progress-enhanced, context-enriched
- <100ms inter-token delay with 50ms configured buffering
- 4-step progress indicators (25%, 50%, 75%, 100%)
- Integration with ChatHandler and ConversationMemoryService

### **Phase 6: Integration & Testing** (Week 11) ✅

**Commit**: `7baf838`

- **Integration Testing**: 15+ E2E tests validating security + streaming
  integration
- **Performance Benchmarks**: All targets met (JWT <50ms, API key <100ms,
  weather <2s, chat 50ms)
- **Security Audit**: PASSED with zero critical vulnerabilities, OWASP Top 10
  compliance
- **Deployment Guide**: 450+ line comprehensive production deployment
  documentation
- Production-ready system with complete documentation

### **Achievement Summary**

| Metric                   | Target | Achieved | Status |
| ------------------------ | ------ | -------- | ------ |
| JWT Auth Latency         | <50ms  | <50ms    | ✅     |
| API Key Auth Latency     | <100ms | <100ms   | ✅     |
| Weather First Chunk      | <2s    | <1s      | ✅     |
| Chat Token Delay         | <100ms | 50ms     | ✅     |
| Concurrent Connections   | 100+   | 100+     | ✅     |
| Memory Usage             | <2GB   | <2GB     | ✅     |
| Critical Vulnerabilities | 0      | 0        | ✅     |
| Test Coverage            | 85%+   | 72%      | ⚠️     |

**Status**: Production-ready with enterprise security and real-time streaming
capabilities.
