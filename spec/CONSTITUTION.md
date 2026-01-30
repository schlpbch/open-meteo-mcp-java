# Open Meteo MCP (Java) – Project Constitution

**Effective Date**: January 30, 2026
**Version**: 2.0.0
**Status**: Governing Document (Living) - **Migration Phase**
**Audience**: Everyone – contributors, maintainers, users, curious folks
**Last Updated**: January 30, 2026

---

## 📖 Preamble

We believe that **great LLM tools are built together**. This project exists to
provide AI agents with comprehensive weather, snow conditions, and air quality
data through a production-ready MCP server.

This constitution is a **living document**. It's not here to enforce rules from
on high, but to create shared understanding of how we work together.

**Migration Context**: This project is migrating the successful open-meteo-mcp
(Python/FastMCP v3.2.0) to Java/Spring Boot to leverage enterprise-grade
architecture, Spring AI 2.0 integration, and alignment with Swiss AI MCP
infrastructure patterns. This is a **strategic migration** building on proven
functionality with enhanced scalability and AI capabilities.

---

## 1. What This Project Is & Isn't

### 1.1 Mission Statement

Open Meteo MCP (Java) is a **production-ready** MCP server providing comprehensive
weather, snow conditions, and air quality data through specialized tools,
enabling AI agents to access real-time forecasts, environmental data, and
location-based weather intelligence via the Open-Meteo API.

**Migration Goal**: Port the proven open-meteo-mcp (Python v3.2.0) to Java with
Spring Boot 3.5, Spring AI 2.0, and enterprise-grade architecture following
established ADR patterns from the Swiss AI MCP ecosystem.

### 1.2 Scope

**In Scope**:

- **4 Production MCP Tools**:
  - `search_location`: Geocoding and location search
  - `get_weather`: Weather forecasts with temperature, precipitation, wind, UV index
  - `get_snow_conditions`: Snow depth, snowfall, mountain weather
  - `get_air_quality`: AQI, pollutants, UV index, pollen data (Europe)
- **5 MCP Resources**:
  - Weather codes (WMO interpretations)
  - Ski resort coordinates
  - Swiss locations (cities, mountains, passes)
  - AQI reference (health recommendations)
  - Weather parameters (available API fields)
- **3 MCP Prompts**:
  - `ski-trip-weather`: Ski trip planning workflow
  - `plan-outdoor-activity`: Weather-aware activity planning
  - `weather-aware-travel`: Travel planning with weather integration
- **Integration**: Open-Meteo API (free, no API key required)
- **AI Integration**: Spring AI 2.0 for weather interpretation and LLM-powered features
- **Architecture**: Java 21, Spring Boot 3.5, Spring WebFlux (async/non-blocking)
- **Documentation**: ADR-driven development with 15 ADRs
- **Federation**: Compatible with swiss-mobility-mcp for travel + weather workflows

**Migration from Python (v3.2.0)**:
- ✅ Feature parity with Python version
- ✅ Enhanced with Spring AI 2.0 capabilities
- ✅ Gzip compression and JSON optimization
- ✅ CompletableFuture-based async operations
- ✅ Type-safe models with Java Records
- ✅ Production-ready observability

**Out of Scope**:

- ❌ Custom weather prediction models (use Open-Meteo data)
- ❌ Weather station hardware integration
- ❌ Mobile application development
- ❌ Commercial weather data APIs (stick to open-source Open-Meteo)
- ❌ Real-time weather alerts (planned for future releases)

### 1.3 What We're Not

- **Not competing with Open-Meteo**: We're making their excellent API accessible to AI agents
- **Not a research project**: Building on proven Python implementation (v3.2.0)
- **Not reinventing the wheel**: Migrating working features to enterprise Java stack
- **Not standalone**: Designed to federate with swiss-mobility-mcp for complete travel intelligence

---

## 2. How We Govern Ourselves

### 2.1 Governance Model: Specification-Driven Development (SDD)

We document what we're doing, _then_ do it. This might sound boring, but it
means:

- ✅ Everyone knows the plan
- ✅ New contributors can catch up quickly
- ✅ We avoid wasted effort
- ✅ We stay honest about limitations

**Single source of truth**: SPECIFICATION.md
**Decisions documented**: ADR_COMPENDIUM.md (50 ADRs as of v2.2.0)
**Detailed analysis**: ARCHITECTURE_DECISIONS.md
**Clear roadmap**: V3_TRIPS_INTEGRATION_ROADMAP.md

---

## 3. Roles & Leadership

### 3.1 Core Roles

| Role               | Responsibilities                                        | Authority                                           | Notes                    |
| ------------------ | ------------------------------------------------------- | --------------------------------------------------- | ------------------------ |
| **Project Lead**   | Overall vision, roadmap, stakeholder communication      | Final decision on scope changes, timeline           | Sets strategic direction |
| **Technical Lead** | Architecture decisions, API design, performance targets | Approval of major PRs, tool contracts, escalation   | Guards technical quality |
| **Maintainer**     | Code review, bug triage, release management             | Merge authority on approved PRs                     | Day-to-day operations    |
| **Contributor**    | Implementation, testing, documentation                  | PR submission; no merge authority                   | Shows up and does work   |
| **Operator**       | Deployment, monitoring, incident response               | Operational decisions; escalation to Technical Lead | Keeps things running     |

### 3.2 Leadership Approach

- **No artificial hierarchy**: Organic leadership (show up, do good work, earn
  trust)
- **Accessible**: Project Lead available for questions
- **Transparent**: Decisions made openly; documented
- **Consensus-based**: We talk until we agree or understand trade-offs

---

## 4. How We Make Decisions

### 4.1 Decision Categories

#### **A. Major Decisions** (require consensus)

_Architecture changes, new tools, breaking API changes, infrastructure/security
changes_

**Process**:

1. Proposal submitted as GitHub issue with RFC (Request for Comments)
2. Technical Lead proposes ADR (Architecture Decision Record)
3. Minimum 3 days public discussion period
4. Technical consensus achieved (no dissent from maintainers)
5. ADR merged and implemented

**Example**: Adding Tool 7 (getPlaceInfo) → Submit RFC → ADR-015 → 3-day
discussion → Merge → Implement

#### **B. Medium Decisions** (require Technical Lead approval)

_Performance optimization, non-breaking enhancements, test improvements, docs,
dependency upgrades_

**Process**:

1. PR submitted by contributor
2. Technical Lead reviews (24-48h)
3. If approved, merge; if concerns, request changes
4. Resubmit until approved

#### **C. Minor Decisions** (any maintainer approval)

_Bug fixes, code cleanup, typos, non-functional improvements_

**Process**:

1. PR submitted
2. Any maintainer reviews and approves
3. Merge

### 4.2 Lightweight & Flexible

We prefer dialogue over deadlock:

```
You submit PR
    ↓
Reviewer likes it?
    ├─ YES ─→ Approve → Merge
    └─ NO  ─→ "Consider changing X"
       ↓
You agree? → YES → Update PR
    └─ NO  → "Let me explain..." → Discussion
       ↓
Technical Lead reviews (if still stuck)
    ↓
Project Lead consults (if really stuck)
```

**Key principle**: If explaining your decision takes >2 sentences, probably
needs bigger discussion.

---

## 5. Contributing

### 5.1 Code of Conduct

**Be nice.** Seriously. That's the core rule.

- Respect different perspectives
- Assume good intent
- Help people learn
- Call out bad behavior (privately first, publicly if needed)

**We value**:

- ✅ **Clarity**: Comments explain _why_, not just _what_
- ✅ **Tests**: Your code works; prove it
- ✅ **Humility**: "I'm not sure" is honest; we figure it out together
- ✅ **Privacy**: Protect user data at all costs
- ✅ **Documentation**: Future-you will thank present-you

Violations? We'll talk. Repeated issues = you're out (but we'll try to help
first).

### 5.2 Getting Started

1. **Pick a task**: Look at GitHub issues or suggest something cool
2. **Fork & branch**: Make your changes in a feature branch
3. **Write tests**: Aim for 75%+ coverage (realistic, not obsessive)
4. **Document**: Update API_REFERENCE.md or TESTING_GUIDE.md as needed
5. **Submit PR**: Explain what you did and why
6. **Chat with reviewers**: They're friendly; they'll help you get it right

**Pro tip**: Start small. Fix a typo. Improve an error message. Build
confidence.

### 5.3 Commit Style

Keep it simple and clear:

```
feat(tool): add getPlaceInfo tool
fix(mapping): handle null place names
docs(api): update Tool 7 specs
test: improve integration test coverage
refactor: simplify journey mapper logic
```

**Style**: Use Conventional Commits; if explaining your commit takes >2
sentences, break it into smaller commits.

### 5.4 What We're Looking For

**Every tool needs**:

1. Clear contract: Input → Output (documented in API_REFERENCE.md)
2. Latency SLA: p95 response time target (SPECIFICATION.md)
3. Tests: ≥3 test cases covering happy path + edge cases
4. Documentation: Examples in SPECIFICATION.md

**Every test needs**: Arrange-Act-Assert structure (see TESTING_GUIDE.md)

**Every decision needs**: ADR or GitHub issue explaining it

---

## 6. Quality Standards (Pragmatic)

### 6.1 Testing

- **Target**: 75% code coverage
- **Why 75%?**: Catches most bugs without obsessive-perfectionism
- **Exception**: If your code is simple + obvious, lower is fine
- **Tools**: JUnit 5, Mockito, StepVerifier (for reactive code)

**Testing levels**: | Level | Scope | Owner | Frequency |
|-------|-------|-------|-----------| | **Unit** | Individual methods |
Developer | Per commit | | **Integration** | Tool + SBB API mock | Developer |
Per PR | | **System** | All tools + caching + monitoring | QA | Per release | |
**Acceptance** | User scenarios + LLM integration | QA | Per release |

### 6.2 Performance

- **Don't overthink it**: Write clear code first
- **Benchmark when it matters**: Use JMH for hot paths
- **Latency goals**: Check SPECIFICATION.md Part 3
- **Load testing**: 10,000 concurrent WebSocket connections (Phase 2+)

### 6.3 Code Quality

- **Coverage requirement**: ≥75% (enforced by PR checks)
- **Error handling**: Errors explain what went wrong + what to try
- **Security**: Hardened by default
- **Design principle**: Specification first; document before coding

---

## 7. Technology Stack

### 7.1 Fixed (Core Dependencies)

These are locked in for the Java migration:

- **Java 21** (LTS with virtual threads for async operations)
- **Spring Boot 3.5.x** (latest stable)
- **Spring WebFlux** (async/non-blocking architecture)
- **Spring AI 2.0** (AI integration + MCP annotations; see ADR-004)
- **httpx / Apache HttpClient 5.x** (async HTTP with gzip compression)
- **Jackson** (JSON serialization with compression)
- **Micrometer** (observability and metrics)

### 7.2 Flexible (Can Evolve)

- HTTP client implementation (httpx, OkHttp, Apache HC5)
- Caching strategy (in-memory, Redis if needed for scaling)
- Test framework (JUnit 5+, AssertJ, Mockito)
- Build tool (Maven 3.9+ preferred, Gradle optional)
- Deployment target (local, cloud-agnostic)

### 7.3 Design Principles

- **Specification-Driven Development**: Document before coding (ADR-000)
- **Async by default**: Non-blocking I/O with CompletableFuture/Virtual Threads
- **AI-Enhanced**: Spring AI 2.0 for intelligent weather interpretation (ADR-004)
- **API simplicity**: No API keys required (Open-Meteo free tier)
- **Type safety**: Java Records for all data models
- **Error clarity**: Structured errors with clear messages
- **Weather-first**: Accurate, timely weather data drives all features
- **Federation-ready**: Works with swiss-mobility-mcp for travel + weather

---

## 8. Security & Compliance

### 8.1 Secrets Management

- **No API keys required**: Open-Meteo API is free and open (no authentication)
- **No hardcoded secrets**: Pre-commit hooks check this
- **Use env vars**: For optional configuration (cache keys, AI API keys if using Spring AI)
- **Local dev**: `.env` file (git-ignored) for Spring AI keys (OpenAI/Anthropic)
- **Cloud deployment**: Environment variables or secret management service

### 8.2 Vulnerability Handling

- **Dependabot enabled**: Automatic PRs for security updates
- **Review them**: Maintainers verify before merging
- **Critical**: Patched within 48h
- **Non-critical**: Batched into releases

### 8.3 Privacy & GDPR

- **Privacy-first**: No user data collected (weather queries are stateless)
- **GDPR compliant**: No personal data stored or tracked
- **Location data**: Coordinates only (no user identification)
- **Data retention**: Weather data cached temporarily (5-60 minutes), see ADR-014
- **Encryption**: TLS in transit for all API calls
- **Audit logging**: Tool invocations logged; no personal information (ADR-013)
- **No tracking**: We don't track users, profile, or sell data
- **Open-Meteo privacy**: Respects Open-Meteo's privacy-first approach

### 8.4 Incident Response

**Severity Levels**:

- **P1 (Critical)**: Security breach, data loss, unavailability
- **P2 (High)**: Major functionality broken, delayed response
- **P3 (Medium)**: Minor bugs, performance degradation
- **P4 (Low)**: Cosmetic issues, documentation typos

**Response Time SLAs**: | Severity | Response | Resolution |
|----------|----------|-----------| | P1 | 1 hour | 4 hours | | P2 | 4 hours |
24 hours | | P3 | 24 hours | 1 week | | P4 | 1 week | Backlog |

---

## 9. Documentation

### 9.1 Core Documentation Files

| What                      | Who Writes               | When                  | Why                          |
| ------------------------- | ------------------------ | --------------------- | ---------------------------- |
| SPECIFICATION.md          | Tech Lead + contributors | Per tool              | Source of truth              |
| ADR_COMPENDIUM.md         | Tech Lead                | Per major decision    | Quick ADR reference (50)     |
| ARCHITECTURE_DECISIONS.md | Decision maker           | When deciding         | Detailed ADR analysis        |
| API_CONTRACT_SPECIFICATION.md | Tool author          | During implementation | Tool contracts               |
| CLAUDE.md                 | Tech Lead                | Per release           | AI-friendly project guide    |
| V3_TRIPS_INTEGRATION_ROADMAP.md | Tech Lead          | Quarterly             | SBB API v3 integration plan  |
| MCP_BEST_PRACTICES.md     | Tech Lead                | As learned            | MCP implementation patterns  |
| FAQ.md                    | Users + support          | Per release           | "How do I...?"               |

### 9.2 Writing Principles

- **Assume readers are smart but busy**: Get to the point
- **Use examples**: Code > explanation
- **Link everything**: Connect related docs
- **Update as you go**: Don't wait for release
- **Clarity**: Clear language; avoid jargon; provide definitions

---

## 10. Releases & Versioning

### 10.1 Semantic Versioning (SemVer)

Format: **MAJOR.MINOR.PATCH** (e.g., 2.0.3)

- **MAJOR** (X.0.0): Breaking API changes (e.g., Tool signature change)
- **MINOR** (0.X.0): New features (e.g., new MCP tool)
- **PATCH** (0.0.X): Bug fixes and non-breaking improvements

**Current version**: 2.0.x (production-ready)
**Latest release**: v2.0.3 (January 2026)
**Next major**: v3.0.0 (planned for Q2 2026 - v3 API integration)

See ADR-043 for backward compatibility and versioning strategy.

### 10.2 Release Cadence

- **Current (v2.x)**: As-needed releases for features and fixes
- **Post-v3.0**: Quarterly major releases, monthly patches

### 10.3 Release Process

1. **Bundle changes**: Collect PRs for a release
2. **Update CHANGELOG.md**: What's new, what changed, what broke
3. **Version bump**: pom.xml + docs
4. **Tag & push**: `git tag v0.x.y`
5. **Deploy** (if applicable): GCP Cloud Run or Maven Central
6. **Announce**: Release notes, breaking changes highlighted

### 10.4 Support & Deprecation

- **v2.x releases**: Active development, bug fixes, security patches
- **Tool deprecation**: 6 months minimum warning, usage-based sunset (ADR-043)
- **Breaking changes**: Requires new tool version (e.g., `tool_v2`)
- **End-of-life**: Announced 6 weeks in advance with migration guide

---

## 11. Roadmap & Status

### 11.1 Current Status (January 2026 - Migration Phase)

**✅ Python Version (v3.2.0) - Production Reference**:
- 4 production MCP tools (geocoding, weather, snow, air quality)
- 5 MCP resources (weather codes, ski resorts, Swiss locations, AQI, parameters)
- 3 MCP prompts (ski trip, outdoor activity, travel planning)
- Gzip compression and JSON optimization
- FastMCP Cloud deployment
- Comprehensive test coverage
- Integration with swiss-ai-mcp-commons

**🔄 Java Migration (v1.0.0-alpha) - In Progress**:
- ✅ Project structure and ADR foundation (15 ADRs)
- ✅ ADR-004: Spring AI 2.0 MCP annotations + ChatClient strategy
- 🔄 Core architecture setup (Spring Boot 3.5, WebFlux)
- 🔄 Open-Meteo API client implementation
- 🔄 Pydantic → Java Records migration
- ⏳ MCP tools implementation (0 of 4 complete)
- ⏳ MCP resources implementation (0 of 5 complete)
- ⏳ MCP prompts implementation (0 of 3 complete)
- ⏳ Test suite migration and enhancement
- ⏳ Spring AI 2.0 weather interpretation features

### 11.2 Migration Timeline (Q1-Q2 2026)

**Phase 1: Foundation (Weeks 1-2)**
- ✅ Update CONSTITUTION.md with migration plan
- Set up Java project structure (Maven/Gradle)
- Implement Open-Meteo API client with gzip compression
- Port Pydantic models to Java Records
- Set up test infrastructure (JUnit 5, Mockito)

**Phase 2: Core Tools (Weeks 3-4)**
- Implement `search_location` tool with @McpTool
- Implement `get_weather` tool with @McpTool
- Implement `get_snow_conditions` tool with @McpTool
- Implement `get_air_quality` tool with @McpTool
- Port helper functions for weather interpretation

**Phase 3: Resources & Prompts (Week 5)**
- Implement 5 MCP resources with @McpResource (JSON data files)
- Implement 3 MCP prompts with @McpPrompt (workflow definitions)
- Add weather code interpretation logic
- Add AQI and pollen interpretation

**Phase 4: AI Enhancement (Week 6)**
- Spring AI 2.0 ChatClient integration for weather insights
- LLM-powered weather interpretation service
- Natural language query processing
- Structured weather recommendations
- Integrate AI interpretation with MCP tools

**Phase 5: Testing & Documentation (Week 7-8)**
- Comprehensive test coverage (target: 80%+)
- API documentation and examples
- Migration guide for Python users
- Performance benchmarking vs Python version

**Phase 6: Deployment & Release (Week 9)**
- Cloud-agnostic deployment configuration
- CI/CD pipeline setup
- v1.0.0 release
- Integration testing with swiss-mobility-mcp

### 11.3 Success Criteria

**Feature Parity**:
- ✅ All 4 tools match Python functionality
- ✅ All 5 resources available
- ✅ All 3 prompts working
- ✅ Gzip compression implemented
- ✅ JSON optimization equivalent or better

**Performance**:
- 📊 Response time ≤ Python version (target: <500ms p95)
- 📊 Memory usage reasonable for JVM (target: <512MB)
- 📊 Throughput ≥ Python version (target: 100 req/s)

**Quality**:
- 🧪 Test coverage ≥ 80%
- 📝 Complete API documentation
- 🔍 No critical bugs
- ✅ ADR compliance (all 15 ADRs reviewed)

**AI Enhancement**:
- 🤖 Spring AI 2.0 weather interpretation working
- 🤖 Natural language weather queries supported
- 🤖 Structured recommendations generated

### 11.4 Upcoming (Post v1.0.0)

**v1.1.0 - Enhanced Features**:
- Historical weather data access
- Weather alerts and notifications
- Extended forecast periods (beyond 16 days)
- Multi-location batch queries

**v2.0.0 - Advanced AI**:
- Predictive weather analysis
- Travel recommendation engine
- Weather pattern recognition
- Integration with other Swiss AI MCP servers

**High-Priority ADRs to Implement**:
- ADR-004: Spring AI 2.0 MCP annotations + ChatClient (in progress)
- ADR-007: Enforce MCP tool naming convention (snake_case)
- ADR-009: Implement Micrometer observability
- ADR-014: Privacy-first data handling (weather data caching)

---

## 12. Community & Communication

### 12.1 Communication Channels

| Channel            | Purpose                        | Frequency             |
| ------------------ | ------------------------------ | --------------------- |
| GitHub Issues      | Feature requests, bug reports  | Real-time             |
| GitHub Discussions | Questions, design ideas        | Daily                 |
| PR Reviews         | Code feedback, learning        | Daily                 |
| Release Notes      | What's new, breaking changes   | Per release           |
| Quarterly sync     | Roadmap updates, big decisions | Quarterly (if needed) |

### 12.2 Community Values

- **Respectful**: Assume good intent; no personal attacks
- **Inclusive**: Welcome contributors of all backgrounds
- **Transparent**: Decisions documented publicly
- **Constructive**: Feedback focused on ideas, not people
- **Helpful**: We help each other learn

### 12.3 Contributor Recognition

- **CHANGELOG.md**: Your name for each contribution
- **Hall of fame**: Major milestones (your choice if listed)
- **GitHub**: Automatic contributor badge

---

## 13. Intellectual Property & Licensing

### 13.1 License: Apache 2.0

Project licensed under **Apache 2.0** (open source, permissive)

- ✅ Commercial use allowed
- ✅ Modification allowed
- ✅ Distribution allowed
- ⚠️ Must include notice of modifications
- ⚠️ Liability disclaimed (as-is warranty)

### 13.2 Contributor License Agreement (CLA)

Contributors agree that:

- Contributions are your own work (or properly licensed)
- You grant the project perpetual, non-exclusive license to your contribution
- Your contribution may be sublicensed under compatible open-source licenses

**CLA Signing**: Requested for PRs >100 lines (but not blocking)

### 13.3 Third-Party Dependencies

- **Apache 2.0 compatible**: Spring, Spring Boot, Project Reactor ✅
- **GPL incompatible**: No GPL-licensed dependencies ✅
- **LGPL**: Allowed with clear linking documentation
- **Proprietary**: Requires explicit approval from Project Lead

---

## 14. Amending This Constitution

### 14.1 Amendment Process

1. **Proposal**: Issue or PR proposing constitutional change
2. **Discussion**: Minimum 7 days public comment period
3. **Consensus**: Technical Lead + Project Lead agreement
4. **Ratification**: Updated version + release notes
5. **Announcement**: Major version bump (e.g., 1.0.0 → 2.0.0)

### 14.2 Principles

- **Transparency**: Changes public; no secret amendments
- **Stability**: We don't change fundamentals often
- **Consent**: Affects all of us; we debate together
- **Effective date**: Immediately upon merge; retroactive application only if
  stated

---

## 15. Migration Strategy: Python to Java

### 15.1 Migration Principles

**Why Migrate?**
- ✅ Enterprise-grade architecture with Spring Boot ecosystem
- ✅ Spring AI 2.0 integration for enhanced weather intelligence
- ✅ Type safety with Java Records
- ✅ Better integration with Swiss AI MCP infrastructure
- ✅ JVM performance and scalability characteristics
- ✅ Alignment with ADR patterns from Journey Service MCP

**Migration Philosophy**:
- **Feature parity first**: Match Python v3.2.0 functionality exactly
- **Enhance second**: Add Spring AI capabilities after core migration
- **Test continuously**: Maintain or exceed Python test coverage (80%+)
- **Document everything**: ADR-driven with clear migration notes

### 15.2 Technology Mapping

| Python (v3.2.0) | Java (v1.0.0) | Notes |
|-----------------|---------------|-------|
| FastMCP | **Spring AI 2.0 MCP annotations** | `@McpTool`, `@McpResource`, `@McpPrompt` |
| httpx | Apache HttpClient 5 / Spring WebClient | Async HTTP with gzip |
| Pydantic | Java Records | Type-safe immutable models |
| structlog | SLF4J + Logback | Structured JSON logging |
| pytest | JUnit 5 + AssertJ | Comprehensive test suite |
| uv | Maven / Gradle | Dependency management |
| Python async/await | CompletableFuture / Virtual Threads | Non-blocking operations |
| swiss-ai-mcp-commons | Custom utilities | JSON serialization mixins |
| @mcp.tool() | **@McpTool** | Spring AI native MCP tool annotation |
| @mcp.resource() | **@McpResource** | Spring AI native MCP resource annotation |
| @mcp.prompt() | **@McpPrompt** | Spring AI native MCP prompt annotation |

### 15.3 Spring Boot Standard Project Structure

#### Python → Java Structure (Spring Boot Standards)

```
open-meteo-mcp/                  open-meteo-mcp-java/
├── src/open_meteo_mcp/          ├── src/
│   ├── server.py          →     │   ├── main/
│   ├── client.py          →     │   │   ├── java/
│   ├── models.py          →     │   │   │   └── com/openmeteo/mcp/
│   ├── helpers.py         →     │   │   │       ├── OpenMeteoMcpApplication.java (Main @SpringBootApplication)
│   └── data/              →     │   │   │       │
                                 │   │   │       ├── config/
                                 │   │   │       │   ├── WebClientConfig.java
                                 │   │   │       │   ├── SpringAiConfig.java (ADR-004)
                                 │   │   │       │   └── McpServerConfig.java
                                 │   │   │       │
                                 │   │   │       ├── tool/
                                 │   │   │       │   ├── WeatherToolService.java (@McpTool methods)
                                 │   │   │       │   ├── SnowToolService.java
                                 │   │   │       │   ├── AirQualityToolService.java
                                 │   │   │       │   ├── LocationToolService.java
                                 │   │   │       │   ├── WeatherResourceService.java (@McpResource methods)
                                 │   │   │       │   └── WeatherPromptService.java (@McpPrompt methods)
                                 │   │   │       │
                                 │   │   │       ├── service/
                                 │   │   │       │   ├── WeatherService.java (business logic)
                                 │   │   │       │   ├── LocationService.java
                                 │   │   │       │   ├── SnowConditionsService.java
                                 │   │   │       │   ├── AirQualityService.java
                                 │   │   │       │   └── InterpretationService.java (Spring AI)
                                 │   │   │       │
                                 │   │   │       ├── client/
                                 │   │   │       │   ├── OpenMeteoClient.java (external API client)
                                 │   │   │       │   └── OpenMeteoClientConfig.java
                                 │   │   │       │
                                 │   │   │       ├── model/
                                 │   │   │       │   ├── dto/
                                 │   │   │       │   │   ├── WeatherResponse.java (Record)
                                 │   │   │       │   │   ├── SnowConditionsResponse.java (Record)
                                 │   │   │       │   │   ├── AirQualityResponse.java (Record)
                                 │   │   │       │   │   └── LocationSearchResponse.java (Record)
                                 │   │   │       │   ├── request/
                                 │   │   │       │   │   ├── WeatherRequest.java (Record)
                                 │   │   │       │   │   ├── SnowConditionsRequest.java (Record)
                                 │   │   │       │   │   └── AirQualityRequest.java (Record)
                                 │   │   │       │   └── mcp/
                                 │   │   │       │       ├── McpToolRequest.java
                                 │   │   │       │       ├── McpToolResponse.java
                                 │   │   │       │       └── McpResource.java
                                 │   │   │       │
                                 │   │   │       ├── exception/
                                 │   │   │       │   ├── OpenMeteoException.java
                                 │   │   │       │   ├── McpException.java
                                 │   │   │       │   └── GlobalExceptionHandler.java (@RestControllerAdvice)
                                 │   │   │       │
                                 │   │   │       └── util/
                                 │   │   │           ├── WeatherCodeInterpreter.java
                                 │   │   │           ├── AqiInterpreter.java
                                 │   │   │           └── JsonSerializationUtil.java
                                 │   │   │
                                 │   │   └── resources/
                                 │   │       ├── application.yml (Spring Boot config)
                                 │   │       ├── application-dev.yml
                                 │   │       ├── application-prod.yml
                                 │   │       ├── data/
                                 │   │       │   ├── weather-codes.json
                                 │   │       │   ├── ski-resorts.json
                                 │   │       │   ├── swiss-locations.json
                                 │   │       │   ├── aqi-reference.json
                                 │   │       │   └── weather-parameters.json
                                 │   │       └── logback-spring.xml (logging config)
                                 │   │
├── tests/                       │   └── test/
│   ├── test_*.py          →     │       └── java/
                                 │           └── com/openmeteo/mcp/
                                 │               ├── controller/
                                 │               │   └── McpControllerTest.java
                                 │               ├── service/
                                 │               │   ├── WeatherServiceTest.java
                                 │               │   └── InterpretationServiceTest.java
                                 │               ├── client/
                                 │               │   └── OpenMeteoClientTest.java
                                 │               ├── integration/
                                 │               │   ├── WeatherIntegrationTest.java (@SpringBootTest)
                                 │               │   └── McpProtocolTest.java
                                 │               └── util/
                                 │                   └── WeatherCodeInterpreterTest.java
                                 │
└── pyproject.toml               ├── pom.xml (or build.gradle)
                                 ├── .mvn/ (Maven wrapper)
                                 └── README.md
```

#### Spring Boot Package Organization Standards

**Package Naming Convention**: `com.openmeteo.mcp.<layer>`

**Layered Architecture (Standard Spring Pattern)**:

1. **MCP Tool Layer** (`tool/`)
   - `@McpTool` annotated methods for MCP tools
   - `@McpResource` annotated methods for MCP resources
   - `@McpPrompt` annotated methods for MCP prompts
   - Spring AI handles MCP protocol automatically
   - Delegate to service layer for business logic

2. **Service Layer** (`service/`)
   - `@Service` annotation
   - Business logic and orchestration
   - Transaction management
   - Call external APIs via clients

3. **Client Layer** (`client/`)
   - External API integration
   - `@Component` or custom client beans
   - WebClient/RestTemplate usage

4. **Model Layer** (`model/`)
   - `dto/`: Data Transfer Objects (Java Records)
   - `request/`: Request models
   - `mcp/`: MCP-specific models
   - No business logic, pure data containers

5. **Configuration Layer** (`config/`)
   - `@Configuration` classes
   - Bean definitions
   - Property configuration (`@ConfigurationProperties`)

6. **Exception Layer** (`exception/`)
   - Custom exceptions
   - `@RestControllerAdvice` for global error handling
   - RFC 7807 problem details

7. **Utility Layer** (`util/`)
   - Static helper methods
   - No `@Component` (stateless utilities)

#### Key Migrations

**1. server.py → Spring AI MCP Tool Service**
```python
# Python: FastMCP server
@mcp.tool()
async def search_location(name: str, count: int = 10):
    results = await client.search_location(name, count)
    return results
```
```java
// Java: Spring AI 2.0 with @McpTool annotation
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

**2. models.py → Java Records**
```python
# Python: Pydantic model
class WeatherResponse(JsonSerializableMixin):
    temperature: float
    precipitation: float
    weather_code: int
```
```java
// Java: Record with validation
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

**3. client.py → OpenMeteoClient.java**
```python
# Python: httpx async client
async def get_weather(self, lat: float, lon: float):
    async with self.client.get(url, params=params) as response:
        return WeatherResponse(**response.json())
```
```java
// Java: Spring WebClient or Apache HC5
public CompletableFuture<WeatherResponse> getWeather(double lat, double lon) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/v1/forecast")
            .queryParam("latitude", lat)
            .queryParam("longitude", lon)
            .build())
        .retrieve()
        .bodyToMono(WeatherResponse.class)
        .toFuture();
}
```

### 15.4 Testing Strategy

**Migration Testing Approach**:
1. **Unit tests first**: Migrate each test file alongside source
2. **Integration tests**: Test Open-Meteo API integration
3. **Contract tests**: Verify MCP protocol compliance
4. **Performance tests**: Compare with Python benchmarks
5. **Compatibility tests**: Ensure same outputs as Python version

**Test Coverage Goals**:
- Unit tests: ≥85% line coverage
- Integration tests: All 4 tools + all API endpoints
- MCP protocol: All resources and prompts
- Error handling: All exception paths

### 15.5 Migration Phases Detailed

#### Phase 1: Foundation (Weeks 1-2)
**Goals**: Project setup, API client, models

**Tasks**:
- [ ] Create Maven/Gradle project structure
- [ ] Set up Spring Boot 3.5 with WebFlux
- [ ] Implement OpenMeteoClient with gzip compression
- [ ] Migrate Pydantic models to Java Records
- [ ] Set up test infrastructure (JUnit 5, Mockito, AssertJ)
- [ ] Implement JSON serialization utilities
- [ ] Copy data/*.json resource files

**Deliverables**:
- Working HTTP client with gzip
- All data models as Java Records
- Basic test suite running

#### Phase 2: Core Tools (Weeks 3-4)
**Goals**: Implement all 4 MCP tools with Spring AI annotations

**Tasks**:
- [ ] Create LocationToolService with @McpTool for `search_location`
- [ ] Create WeatherToolService with @McpTool for `get_weather`
- [ ] Create SnowToolService with @McpTool for `get_snow_conditions`
- [ ] Create AirQualityToolService with @McpTool for `get_air_quality`
- [ ] Port weather code interpretation logic to utilities
- [ ] Port AQI interpretation logic to utilities
- [ ] Add @McpParam annotations with descriptions
- [ ] Add error handling and validation
- [ ] Write unit tests for each tool service

**Deliverables**:
- All 4 tools functional with Spring AI MCP annotations
- Test coverage ≥80% for tools
- MCP Inspector validation passing
- API integration tests passing

#### Phase 3: Resources & Prompts (Week 5)
**Goals**: MCP resources and workflow prompts with Spring AI annotations

**Tasks**:
- [ ] Create WeatherResourceService with @McpResource methods
- [ ] Implement weather code resource (weather://codes)
- [ ] Implement ski resort resource (weather://ski-resorts)
- [ ] Implement Swiss locations resource (weather://swiss-locations)
- [ ] Implement AQI reference resource (weather://aqi-reference)
- [ ] Implement weather parameters resource (weather://parameters)
- [ ] Create WeatherPromptService with @McpPrompt methods
- [ ] Implement ski-trip-weather prompt
- [ ] Implement plan-outdoor-activity prompt
- [ ] Implement weather-aware-travel prompt
- [ ] Write tests for resources and prompts

**Deliverables**:
- All 5 resources accessible via Spring AI @McpResource
- All 3 prompts functional via @McpPrompt
- MCP Inspector validation passing
- Resource and prompt tests passing

#### Phase 4: AI Enhancement (Week 6)
**Goals**: Spring AI 2.0 ChatClient integration for LLM-powered features

**Tasks**:
- [ ] Configure Spring AI 2.0 ChatClient (ADR-004)
- [ ] Set up Anthropic/OpenAI API keys
- [ ] Create InterpretationService with ChatClient
- [ ] Implement weather condition interpretation
- [ ] Add natural language query processing
- [ ] Generate structured weather recommendations
- [ ] Add travel advice generation
- [ ] Integrate AI interpretation with MCP tools
- [ ] Add optional AI enhancement to prompts
- [ ] Write tests for interpretation service

**Deliverables**:
- Spring AI ChatClient configured with Claude/GPT
- InterpretationService functional
- Weather interpretation working
- Enhanced prompt responses with AI insights
- Tests for AI features passing

#### Phase 5: Testing & Documentation (Weeks 7-8)
**Goals**: Complete testing and docs

**Tasks**:
- [ ] Achieve ≥80% test coverage
- [ ] Write API documentation
- [ ] Create migration guide from Python
- [ ] Add usage examples
- [ ] Performance benchmarking
- [ ] Compare metrics with Python version
- [ ] Write CLAUDE.md for Java version
- [ ] Update all spec documents

**Deliverables**:
- Test coverage report
- Complete API documentation
- Migration guide
- Performance comparison report

#### Phase 6: Deployment (Week 9)
**Goals**: Production release

**Tasks**:
- [ ] Set up CI/CD pipeline
- [ ] Configure cloud deployment
- [ ] Integration test with swiss-mobility-mcp
- [ ] Load testing
- [ ] Security audit
- [ ] Release v1.0.0

**Deliverables**:
- v1.0.0 release
- Deployed and accessible
- Integration verified

### 15.6 Migration Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Feature drift from Python version | High | Daily comparison tests against Python |
| Performance regression | Medium | Benchmark every commit, target <500ms p95 |
| MCP protocol incompatibility | High | Contract tests, MCP Inspector validation |
| Spring AI complexity | Medium | Start with simple use cases, iterate |
| JVM memory overhead | Low | Profile regularly, optimize if needed |
| Test coverage gaps | Medium | Require tests with every PR |
| Documentation drift | Low | Update docs alongside code changes |

### 15.7 Success Metrics

**Release Readiness Checklist**:
- [ ] All 4 tools match Python functionality
- [ ] All 5 resources accessible
- [ ] All 3 prompts working
- [ ] Test coverage ≥80%
- [ ] Performance ≤ Python version (p95 latency)
- [ ] Zero critical bugs
- [ ] Documentation complete
- [ ] Spring AI integration functional
- [ ] Integration with swiss-mobility-mcp verified
- [ ] CI/CD pipeline operational

**Post-Release Goals**:
- Monthly active users (via MCP servers)
- p95 latency < 500ms
- Uptime > 99.5%
- Zero security vulnerabilities
- Community contributions (PRs, issues, discussions)

---

## 16. In Summary

This project exists to bring **enterprise-grade weather intelligence to AI agents**.
We're migrating a proven Python implementation to Java with enhanced capabilities,
and we want to do it right.

**This Migration Is About**:

- ✅ Building on success (Python v3.2.0 works great)
- ✅ Adding enterprise patterns (Spring Boot, Spring AI 2.0)
- ✅ Maintaining feature parity
- ✅ Enhancing with AI capabilities
- ✅ Following established ADRs
- ✅ Learning and improving together

**How You Can Help**:

- 📖 Review the migration plan (this document)
- 💻 Contribute to the Java implementation
- 🧪 Help with testing and validation
- 📝 Improve documentation
- 🐛 Report issues early and often
- 💡 Suggest improvements to the architecture

**What We Promise**:

- Transparent migration process with regular updates
- Feature parity with Python version before enhancements
- Thorough testing and documentation
- Open collaboration and discussion
- Respect for the proven design of the Python version
- Commitment to the ADR-driven development approach

**We're all figuring this out together.** ❤️

Our governance is **formal enough to scale** (15 ADRs, detailed migration plan),
but **flexible enough to welcome** (open to feedback, iterative approach). We value
both **precision** (clear decisions, documented standards) and **pragmatism**
(realistic quality bars, practical timelines, human communication).

---

## References

### Project Documentation

- **CONSTITUTION.md**: This document - project governance
- **ADR_COMPENDIUM.md**: Architecture Decision Records (15 ADRs)
- **CLAUDE.md**: AI-friendly project guide (for Claude Code)
- **MIGRATION_GUIDE.md**: Python to Java migration guide (to be created)
- **API_REFERENCE.md**: MCP tool contracts and examples
- **MCP_BEST_PRACTICES.md**: MCP implementation patterns
- **CONTRIBUTING.md**: How to contribute
- **FAQ.md**: User questions

### Key ADRs for Migration

- **ADR-005**: Specification-Driven Development (foundation)
- **ADR-004**: Use Spring AI 2.0 for MCP Annotations + AI Integration (critical for Java)
- **ADR-002**: Use Java Records for All DTOs and Domain Models
- **ADR-007**: MCP Tool Naming Convention (snake_case)
- **ADR-015**: API Versioning and Backward Compatibility
- **ADR-009**: Micrometer for Observability
- **ADR-014**: Privacy-First Weather Data Handling (data retention policy)

### Python Reference Implementation

- **open-meteo-mcp v3.2.0**: Python/FastMCP implementation
  - Repository: `c:\Users\schlp\code\open-meteo-mcp`
  - Documentation: README.md, CLAUDE.md, docs/
  - Feature reference for migration

### External Standards

- **Apache 2.0 License**: <https://www.apache.org/licenses/LICENSE-2.0>
- **Semantic Versioning**: <https://semver.org/>
- **Conventional Commits**: <https://www.conventionalcommits.org/>
- **WCAG 2.1**: <https://www.w3.org/WAI/WCAG21/quickref/>
- **GDPR**: <https://gdpr-info.eu/>
- **RFC 7807** (Problem Details): <https://tools.ietf.org/html/rfc7807>
- **MCP Protocol**: <https://modelcontextprotocol.io/>

---

**Document Version**: 2.0.0
**Status**: Active, Living Document - **MIGRATION PHASE**
**Effective Date**: January 30, 2026
**Last Updated**: January 30, 2026
**Migration Start**: January 30, 2026
**Target Release**: Q2 2026 (v1.0.0)
**Next Review**: Weekly during migration, then Q3 2026
**Questions?** Open an issue or discussion in the repository

---

**Migration Context**: Python (FastMCP v3.2.0) → Java (Spring Boot 3.5 + Spring AI 2.0)
**Reference Implementation**: open-meteo-mcp v3.2.0 (Python)
**Target Architecture**: Java 21, Spring Boot 3.5, Spring AI 2.0, WebFlux
**Governed by**: Specification-Driven Development (SDD) + ADR-004 (Spring AI MCP)
**Community**: Welcoming all contributors, especially during migration

---

## Quick Links

- **Python Reference**: `c:\Users\schlp\code\open-meteo-mcp`
- **Java Implementation**: `c:\Users\schlp\code\open-meteo-mcp-java`
- **ADR Compendium**: [ADR_COMPENDIUM.md](./ADR_COMPENDIUM.md) (15 ADRs)
- **Spring AI ADR**: [ADR-004](./ADR_COMPENDIUM.md#adr-004-use-spring-ai-20-for-weather-interpretation)
- **Migration Tracking**: GitHub Issues and Project Board
