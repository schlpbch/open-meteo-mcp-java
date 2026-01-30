# Journey Service MCP - Architecture Decision Records (ADR) Compendium

**Document Version**: 2.4.0 **Last Updated**: 2026-01-30 **Total ADRs**: 51 (32
Accepted, 15 Proposed, 3 Superseded, 1 Deprecated)

**Related Documents**:

- [CONSTITUTION.md](./CONSTITUTION.md) - Project governance and principles

---

## Status Legend

- ✅ **Accepted** - Currently in use and actively maintained
- 🔄 **Proposed** - Under consideration, not yet implemented
- ⛔ **Superseded** - Replaced by another ADR (see cross-reference)
- 🗑️ **Deprecated** - No longer applicable, kept for historical context
- ❌ **Rejected** - Considered but not adopted

---

## Quick Reference by Category

### Infrastructure & Deployment

- [ADR-028: Zero-Docker Development Strategy](#adr-028-zero-docker-development-strategy)
  ✅
- [ADR-010: Use GitHub for Source Control and Cloud Build for CI/CD](#adr-010-use-github-for-source-control-and-cloud-build-for-cicd)
  ✅

### Architecture & Design

- [ADR-001: Use Reactive Architecture (Project Reactor)](#adr-001-use-reactive-architecture-project-reactor)
  ✅
- [ADR-027: Use Java Records for All DTOs and Domain Models](#adr-027-use-java-records-for-all-dtos-and-domain-models)
  ✅
- [ADR-030: MCP Sampling for Agentic Data Synthesis](#adr-030-mcp-sampling-for-agentic-data-synthesis)
  ✅
- [ADR-051: Use Spring AI 2.0 for AI Integration](#adr-051-use-spring-ai-20-for-ai-integration)
  🔄

### Development Workflow

- [ADR-021: Use Maven Daemon and Build Profiles for Fast Development](#adr-021-use-maven-daemon-and-build-profiles-for-fast-development)
  ✅
- [ADR-023: Use Parallel Test Execution with Smart Categorization](#adr-023-use-parallel-test-execution-with-smart-categorization)
  ✅

### Quality & Testing

- [ADR-015: Use Test Pyramid Strategy with Reactive Testing](#adr-015-use-test-pyramid-strategy-with-reactive-testing)
  ✅
- [ADR-013: Use Structured JSON Logging with PII Masking](#adr-013-use-structured-json-logging-with-pii-masking)
  ✅
- [ADR-009: Use Micrometer for Observability](#adr-009-use-micrometer-for-observability)
  ✅
- [ADR-024: Use Loki + Promtail + Grafana for Observability](#adr-024-use-loki--promtail--grafana-for-observability)
  ✅

### Documentation & Governance

- [ADR-000: Specification-Driven Development with Round-Trip Engineering](#adr-000-specification-driven-development-with-round-trip-engineering)
  ✅
- [ADR-022: Organize Documentation by Purpose, Not Type](#adr-022-organize-documentation-by-purpose-not-type)
  ✅
- [ADR-025: Use Specification-Driven Development with Phase-Based Planning](#adr-025-use-specification-driven-development-with-phase-based-planning)
  ✅
- [ADR-026: Use Git History Preservation for Documentation Reorganization](#adr-026-use-git-history-preservation-for-documentation-reorganization)
  ✅
- [ADR-008: Use Semantic Versioning (SemVer)](#adr-008-use-semantic-versioning-semver)
  ✅

### Integration & APIs

- [ADR-019: Use MCP Protocol 2025-03-26 with Streamable HTTP Transport](#adr-019-use-mcp-protocol-2025-03-26-with-streamable-http-transport)
  ✅
- [ADR-020: Use API-First Strategy with Fallback for SBB Integration](#adr-020-use-api-first-strategy-with-fallback-for-sbb-integration)
  ✅
- [ADR-012B: Use Error Handling & Circuit Breaking for External APIs](#adr-012b-use-error-handling--circuit-breaking-for-external-apis)
  ✅

### State & Caching

### Security & Compliance

- [ADR-006: Use Spring Security for Authentication](#adr-006-use-spring-security-for-authentication)
  ✅
- [ADR-007: Use Event Sourcing for Audit Trail](#adr-007-use-event-sourcing-for-audit-trail)
  🔄
- [ADR-048: Data Retention and Privacy Policy](#adr-048-data-retention-and-privacy-policy)
  🔄

### Development Standards

- [ADR-039: Java 21 Preview Features Policy](#adr-039-java-21-preview-features-policy)
  🔄
- [ADR-042: Reactive Error Propagation Patterns](#adr-042-reactive-error-propagation-patterns)
  🔄
- [ADR-045: JSON Schema Validation for MCP Tool Inputs](#adr-045-json-schema-validation-for-mcp-tool-inputs)
  🔄

### API Design

- [ADR-040: MCP Tool Naming Convention](#adr-040-mcp-tool-naming-convention) 🔄
- [ADR-043: API Versioning and Backward Compatibility](#adr-043-api-versioning-and-backward-compatibility)
  🔄

### Performance & Scalability

- [ADR-041: Cache TTL Strategy by Data Volatility](#adr-041-cache-ttl-strategy-by-data-volatility)
  🔄
- [ADR-046: Performance SLAs and Monitoring](#adr-046-performance-slas-and-monitoring)
  🔄

### Operations & Reliability

- [ADR-044: Resilience4j Configuration Strategy](#adr-044-resilience4j-configuration-strategy)
  🔄
- [ADR-047: Graceful Shutdown and In-Flight Request Handling](#adr-047-graceful-shutdown-and-in-flight-request-handling)
  🔄
- [ADR-050: Health Check Strategy](#adr-050-health-check-strategy) 🔄

---

## Table of Contents

- [ADR-000: Specification-Driven Development with Round-Trip Engineering](#adr-000-specification-driven-development-with-round-trip-engineering)

- [ADR-005: Use Per-User Rolling Window Rate Limiting](#adr-005-use-per-user-rolling-window-rate-limiting)
  🗑️
- [ADR-006: Use Spring Security for Authentication](#adr-006-use-spring-security-for-authentication)
- [ADR-007: Use Event Sourcing for Audit Trail](#adr-007-use-event-sourcing-for-audit-trail)
- [ADR-008: Use Semantic Versioning (SemVer)](#adr-008-use-semantic-versioning-semver)
- [ADR-009: Use Micrometer for Observability](#adr-009-use-micrometer-for-observability)
- [ADR-010: Use GitHub for Source Control and Cloud Build for CI/CD](#adr-010-use-github-for-source-control-and-cloud-build-for-cicd)
- [ADR-012B: Use Error Handling & Circuit Breaking for External APIs](#adr-012b-use-error-handling--circuit-breaking-for-external-apis)
- [ADR-013: Use Structured JSON Logging with PII Masking](#adr-013-use-structured-json-logging-with-pii-masking)
- [ADR-015: Use Test Pyramid Strategy with Reactive Testing](#adr-015-use-test-pyramid-strategy-with-reactive-testing)
- [ADR-019: Use MCP Protocol 2025-03-26 with Streamable HTTP Transport](#adr-019-use-mcp-protocol-2025-03-26-with-streamable-http-transport)
- [ADR-020: Use API-First Strategy with Fallback for SBB Integration](#adr-020-use-api-first-strategy-with-fallback-for-sbb-integration)
- [ADR-022: Organize Documentation by Purpose, Not Type](#adr-022-organize-documentation-by-purpose-not-type)
- [ADR-023: Use Parallel Test Execution with Smart Categorization](#adr-023-use-parallel-test-execution-with-smart-categorization)
- [ADR-025: Use Specification-Driven Development with Phase-Based Planning](#adr-025-use-specification-driven-development-with-phase-based-planning)
- [ADR-026: Use Git History Preservation for Documentation Reorganization](#adr-026-use-git-history-preservation-for-documentation-reorganization)
- [ADR-027: Use Java Records for All DTOs and Domain Models](#adr-027-use-java-records-for-all-dtos-and-domain-models)

- [ADR-030: MCP Sampling for Agentic Data Synthesis](#adr-030-mcp-sampling-for-agentic-data-synthesis)
  -swiss-travel-ecosystem)
- [ADR-034: MCP Resources and Prompts for LLM Guidance](#adr-034-mcp-resources-and-prompts-for-llm-guidance)
- [ADR-039: Java 21 Preview Features Policy](#adr-039-java-21-preview-features-policy)
- [ADR-040: MCP Tool Naming Convention](#adr-040-mcp-tool-naming-convention)
- [ADR-041: Cache TTL Strategy by Data Volatility](#adr-041-cache-ttl-strategy-by-data-volatility)
- [ADR-042: Reactive Error Propagation Patterns](#adr-042-reactive-error-propagation-patterns)
- [ADR-043: API Versioning and Backward Compatibility](#adr-043-api-versioning-and-backward-compatibility)
- [ADR-044: Resilience4j Configuration Strategy](#adr-044-resilience4j-configuration-strategy)
- [ADR-045: JSON Schema Validation for MCP Tool Inputs](#adr-045-json-schema-validation-for-mcp-tool-inputs)
- [ADR-046: Performance SLAs and Monitoring](#adr-046-performance-slas-and-monitoring)
- [ADR-047: Graceful Shutdown and In-Flight Request Handling](#adr-047-graceful-shutdown-and-in-flight-request-handling)
- [ADR-048: Data Retention and Privacy Policy](#adr-048-data-retention-and-privacy-policy)
- [ADR-049: Thread Pool Sizing for Reactive Workloads](#adr-049-thread-pool-sizing-for-reactive-workloads)
- [ADR-050: Health Check Strategy](#adr-050-health-check-strategy)

---

## ADR-000: Specification-Driven Development with Round-Trip Engineering

**Status**: Accepted  
**Date**: 2025-12-15  
**Deciders**: Architecture Team  
**Context**: Documentation Governance

### [ADR-000] Context

As the Journey Service MCP evolves, we need to ensure that implemented features
are always documented, specifications remain synchronized with actual code, and
documentation can be used to understand and regenerate the system. Without this
discipline, documentation becomes stale.

### [ADR-000] Decision

**All implemented features MUST be represented in specification documents.**

1. **Specification-First Development**: New features start with specification
   updates.
2. **Round-Trip Engineering**: Code changes must be reflected back in
   specifications.
3. **Living Documentation**: Specs are living documents.
4. **Documentation Hierarchy**:
   - `ARCHITECTURE.md` (System-level)
   - `specification/DESIGN.md` (Philosophy)
   - `specification/SPECIFICATION.md` (Technical specs)

### [ADR-000] Consequences

- ✅ Single Source of Truth
- ✅ Onboarding made easier
- ⚠️ Additional Work (docs update required for every change)
- ℹ️ Documentation Debt needed for existing features

---

## ADR-006: Use Spring Security for Authentication

**Date**: December 10, 2025  
**Status**: Accepted  
**Context**: System needs to secure endpoints against unauthorized access.

### [ADR-006] Decision

Use **Spring Security 6.x** with dual authentication:

1. **OAuth2** for LLM platforms
2. **API Key** for programmatic access

### [ADR-006] Rationale

- ✅ Industry-standard, battle-tested
- ✅ Auditability and Compliance

---

---

## ADR-008: Use Semantic Versioning (SemVer)

**Date**: December 10, 2025  
**Status**: Accepted

### [ADR-008] Decision

Follow **Semantic Versioning 2.0.0** (MAJOR.MINOR.PATCH).

### [ADR-008] Rationale

- ✅ Standard and predictable
- ✅ Automatable tooling support

---

## ADR-009: Use Micrometer for Observability

**Date**: December 10, 2025  
**Status**: Accepted

### [ADR-009] Decision

Use **Micrometer** for metrics with support for Prometheus, Jaeger, and ELK.

### [ADR-009] Rationale

- ✅ Vendor-neutral facade
- ✅ Spring Boot integration

---

## ADR-010: Use GitHub for Source Control and Cloud Build for CI/CD

**Status**: ✅ Accepted  
**Date**: December 10, 2025  
**Updated**: January 6, 2026 (clarified CI/CD tooling)  
**Deciders**: Architecture Team

### [ADR-010] Context

Need centralized source control with automated build, test, and deployment
pipeline. Must integrate with GCP infrastructure.

### [ADR-010] Decision

Use **GitHub** for source control and **Google Cloud Build** for CI/CD pipeline.

- **GitHub**: Source control, code review, collaboration
- **Cloud Build**: Automated builds, tests, container image creation, deployment
  to Cloud Run

### [ADR-010] Rationale

- ✅ GitHub: Industry standard, excellent collaboration features, familiar to
  developers
- ✅ Cloud Build: Native GCP integration, no credential management, direct
  access to GCP services
- ✅ Separation of concerns: GitHub for code, Cloud Build for infrastructure
- ✅ Cost-effective: Cloud Build free tier covers most usage

### [ADR-010] Alternatives Considered

1. **GitHub Actions for full CI/CD** - Rejected due to complex GCP credential
   management and slower GCP integration
2. **GitLab CI/CD** - Rejected due to team familiarity with GitHub
3. **Jenkins** - Rejected due to operational overhead (self-hosting required)

### [ADR-010] Consequences

- **Positive**: Best-in-class tools for each purpose, native GCP integration,
  minimal credential management
- **Negative**: Two systems to manage (GitHub + Cloud Build)
- **Mitigation**: Clear documentation, automated triggers from GitHub to Cloud
  Build

### [ADR-010] Implementation Notes

- GitHub branch protection rules enforce code review
- Cloud Build triggers automatically on push to `main` or pull requests
- Build configuration in `cloudbuild.yaml`
- Secrets managed via GCP Secret Manager

### [ADR-010] Related ADRs

---

## ADR-013: Use Structured JSON Logging with PII Masking

**Date**: December 10, 2025  
**Status**: Accepted

### [ADR-013] Decision

Implement **JSON structured logging** with **automatic PII masking**.

### [ADR-013] Rationale

- ✅ Machine-queryable (ELK)
- ✅ GDPR compliance (PII masked)

---

## ADR-015: Use Test Pyramid Strategy

**Date**: December 10, 2025  
**Status**: Accepted  
**Context**: ode requires specific testing strategies.

### [ADR-015] Decision

Adopt **test pyramid**:

- 70% Unit Tests (StepVerifier)
- 20% Integration Tests (Mocked externals)
- 10% E2E Tests (Real calls)

### [ADR-015] Rationale

- ✅ Fast feedback loop
- ✅ Robust coverage of reactive edge cases

---

## ADR-020: Use API-First Strategy with Fallback for SBB Integration

**Date**: December 14, 2025  
**Status**: Accepted

### [ADR-020] Decision

Implement **API-first** strategy with **automatic fallback** to in-memory data
(50 Swiss stations) if SBB API is unavailable.

### [ADR-020] Rationale

- ✅ High availability
- ✅ Development possible without credentials

---

## ADR-022: Organize Documentation by Purpose, Not Type

**Date**: December 14, 2025  
**Status**: Accepted

### [ADR-022] Decision

Consolidate all documentation under `specification/` organized by purpose
(architecture, api, tools, operations, testing, development).

### [ADR-022] Rationale

- ✅ Single source of truth
- ✅ Better discoverability

---

## ADR-023: Use Parallel Test Execution with Smart Categorization

**Date**: December 14, 2025  
**Status**: Accepted

### [ADR-023] Decision

Implement **parallel test execution** with **smart categorization** (test
profiles: fast, unit, integration).

### [ADR-023] Rationale

- ✅ 70% faster test execution
- ✅ Efficient resource utilization

---

---

## ADR-025: Use Specification-Driven Development with Phase-Based Planning

**Date**: December 14, 2025  
**Status**: Accepted

### [ADR-025] Decision

Adopt **specification-driven development** with **phase-based planning** (Phases
0-10).

### [ADR-025] Rationale

- ✅ Clear roadmap and requirements
- ✅ Stakeholder alignment

---

## ADR-026: Use Git History Preservation for Documentation Reorganization

**Date**: December 14, 2025  
**Status**: Accepted

### [ADR-026] Decision

Always use **`git mv`** for moving files to preserve history.

### [ADR-026] Rationale

- ✅ Maintains git blame and audit trail

---

## ADR-027: Use Java Records for All DTOs and Domain Models

**Date**: December 15, 2025  
**Status**: Accepted

### [ADR-027] Decision

Use **Java 21 records** for all DTOs, API responses, and domain models.

### [ADR-027] Rationale

- ✅ Immutability by default
- ✅ Zero boilerplate
- ✅ Compact and clear intent

---

## ADR-028: Zero-Docker Development Strategy

**Date**: December 20, 2025  
**Status**: Accepted  
**Context**: Local development needs to be fast and lightweight. Docker-based
flows can be resource-intensive.

### [ADR-028] Decision

Adopt a **"Profile-First" strategy**:

- Use `dev` profile with **embedded services** (Embedded Redis)
- Eliminate local Docker dependency ("Zero-Docker")

### [ADR-028] Rationale

- ✅ Faster startup (no container overhead)
- ✅ Simplified workflow (Maven only)
- ✅ Reduced resource usage

---

---

## ADR-031: Use RFC 7807 Structured Error Propagation (Phase 1)

**Date**: December 22, 2025  
**Status**: Accepted  
**Context**: Errors from the SBB API were previously swallowed or returned as
generic 500 errors.

### [ADR-031] Decision

Implement **Structured Error Propagation** based on **RFC 7807 (Problem
Details)**.

### [ADR-031] Rationale

- ✅ **Transparency**: Exposes the root cause from the upstream provider.
- ✅ **Actionability**: Provides `displayResolution` for immediate user fix.
- ✅ **Traceability**: Propagates `traceId`.
- ✅ **Localization**: Leverages localized messages directly from SBB.

### [ADR-031] Implementation

Intercept SBB API 4xx/5xx responses in `SbbWebClient`, throw `SbbApiException`,
and handle it in `GlobalExceptionHandler` to return a standardized
`ErrorResponse`.

---

## ADR-034: MCP Resources and Prompts for LLM Guidance

**Status**: ✅ Accepted  
**Date**: January 5, 2026  
**Deciders**: Architecture Team  
**MCP Protocol**: 2025-03-26

### [ADR-034] Context

LLMs interacting with MCP tools often:

- Hallucinate parameter values (e.g., invalid fare types, non-existent travel
  classes)
- Miss important workflow steps (e.g., forget to check pricing before booking)
- Don't understand data formats (e.g., Swiss station codes, weather parameters)
- Struggle with multi-step workflows (e.g., plan → price → book sequence)

Traditional approaches:

1. **Tool descriptions only**: Limited space, LLM still hallucinates
2. **Extensive prompting**: Requires client-side configuration, not portable
3. **Trial and error**: LLM learns from errors, poor user experience

### [ADR-034] Decision

Implement **MCP Resources** (static reference data) and **MCP Prompts**
(workflow templates) to guide LLM behavior.

**MCP Resources** (read-only reference data):

- Fare types (e.g., "Half-Fare", "GA", "Point-to-Point")
- Travel classes (e.g., "First", "Second")
- Weather parameters (e.g., temperature, precipitation, wind speed)
- Swiss ski resorts (e.g., Zermatt, Verbier, St. Moritz)
- Station lists (major Swiss cities)

**MCP Prompts** (workflow templates):

- "plan-and-price-trip": Complete journey planning workflow
- "compare-total-costs": Compare different fare types
- "ski-trip-weather": Weather-aware ski trip planning
- "outdoor-activity": Weather-appropriate activity suggestions

### [ADR-034] Rationale

- ✅ **Reduces Hallucination**: LLM reads valid values from resources
- ✅ **Improves Tool Usage**: Prompts guide multi-step workflows
- ✅ **Self-Documenting**: Resources explain data formats
- ✅ **Portable**: Works across all LLM clients (Claude, ChatGPT, etc.)
- ✅ **Discoverable**: LLM can list available resources and prompts
- ✅ **Versioned**: Resources and prompts evolve with API

### [ADR-034] Alternatives Considered

1. **Tool descriptions only** - Rejected due to limited space and continued
   hallucination
2. **Client-side prompting** - Rejected due to lack of portability
3. **Hardcoded examples** - Rejected due to maintenance burden

### [ADR-034] Consequences

- **Positive**: Better LLM behavior, fewer errors, improved user experience
- **Negative**: Additional implementation work, resource maintenance
- **Mitigation**: Automated resource generation from APIs, clear versioning

### [ADR-034] Implementation Examples

**Resource Example** (`fare-types.json`):

```json
{
  "fareTypes": [
    { "code": "HALF_FARE", "name": "Half-Fare Card", "discount": "50%" },
    { "code": "GA", "name": "General Abonnement", "discount": "100%" }
  ]
}
```

**Prompt Example** (`plan-and-price-trip`):

```text
You are helping a user plan a train journey in Switzerland.
Steps:
1. Use searchLocations to find origin/destination
2. Use planJourney to find connections
3. Use getJourneyPricing to calculate costs
4. Present options with prices
```

### [ADR-034] Implementation Notes

- Resources stored as JSON files, loaded at startup
- Prompts defined as text templates with structured metadata
- Registered via `ResourceRegistry` and `PromptRegistry` (sbb-mcp-commons)
- Discoverable via MCP protocol (`resources/list`, `prompts/list`)

### [ADR-034] Related ADRs

- [ADR-019](#adr-019-use-mcp-protocol-2025-03-26-with-streamable-http-transport) -
  MCP protocol version
- [ADR-030](#adr-030-mcp-sampling-for-agentic-data-synthesis) - Agentic behavior
- [ADR-032](#adr-032-extract-common-infrastructure-to-sbb-mcp-commons) -
  Registry infrastructure

---

---

## ADR-040: MCP Tool Naming Convention

**Status**: 🔄 Proposed **Date**: 2026-01-28 **Deciders**: Architecture Team
**Context**: API Design & Standards

### [ADR-040] Context

MCP tools currently use `category__tool_name` pattern (e.g.,
`journey__find_trips`, `place__find_places`). With 16 tools and plans for 50+,
we need formal documentation to ensure:

- Consistent naming across all tools
- Easy discoverability for LLMs
- Clear namespace boundaries
- Scalable to 100+ tools without conflicts

Without formal conventions, we risk:

- Inconsistent naming (e.g., `journey__find_trips` vs `trip__find`)
- Namespace collisions
- Poor LLM discoverability
- Confusion about tool purpose

### [ADR-040] Decision

Adopt **hierarchical naming convention** with strict rules:

**Format**:

```
{category}__{verb}_{noun}[_{qualifier}]

Components:
- category: Domain namespace (required)
- verb: Action being performed (required)
- noun: Resource being acted upon (required)
- qualifier: Additional context (optional)
```

**Naming Rules**:

1. **Category** (Domain Namespace):
   - Values: `journey`, `place`, `formation`, `eco`, `transfer`, `events`
   - Purpose: Groups related functionality
   - Separator: Double underscore (`__`)

2. **Verb** (Action):
   - Common: `find`, `get`, `compare`, `optimize`, `create`, `update`, `delete`
   - `find`: Search/filter operations returning multiple results
   - `get`: Retrieve specific resource by ID
   - `compare`: Side-by-side comparison of options
   - `optimize`: Improve/adjust existing resource

3. **Noun** (Resource):
   - Plural for collections: `trips`, `places`, `alerts`
   - Singular for specific items: `trip_details`, `train_formation`
   - Domain-specific: `service_alerts`, `eco_comparison`

4. **Qualifier** (Optional Context):
   - Location: `by_location`, `by_polygon`, `at_transfer`
   - Source: `from_leg`, `by_name`
   - Type: `by_origin_destination`
   - Separator: Single underscore (`_`)

**Examples with Rationale**:

```
✅ journey__find_trips
   - Category: journey (planning domain)
   - Verb: find (search operation)
   - Noun: trips (plural collection)

✅ journey__find_alternatives_at_transfer
   - Category: journey
   - Verb: find
   - Noun: alternatives
   - Qualifier: at_transfer (context)

✅ place__find_places_by_location
   - Category: place
   - Verb: find
   - Noun: places
   - Qualifier: by_location (search method)

✅ journey__get_trip_details
   - Category: journey
   - Verb: get (specific retrieval)
   - Noun: trip_details (singular)

❌ find_trips (missing category)
❌ journey_find_trips (single underscore instead of double)
❌ journey__trips (missing verb)
❌ journey__get_trips_details (inconsistent plural/singular)
```

### [ADR-040] Rationale

- ✅ **Improves Discoverability**: LLMs can browse by category
- ✅ **Prevents Collisions**: Namespace isolation via category
- ✅ **Self-Documenting**: Name describes purpose and scope
- ✅ **Scales Gracefully**: Works with 100+ tools
- ✅ **REST-like**: Familiar resource-oriented pattern
- ✅ **Consistent**: Clear rules eliminate ambiguity

### [ADR-040] Alternatives Considered

1. **Flat naming** (e.g., `find_trips`) - Rejected due to namespace collisions
2. **Dot notation** (e.g., `journey.find.trips`) - Rejected due to MCP protocol
   compatibility
3. **CamelCase** (e.g., `journeyFindTrips`) - Rejected due to poor readability
4. **Path-like** (e.g., `journey/find/trips`) - Rejected due to MCP naming
   restrictions

### [ADR-040] Consequences

- **Positive**:
  - Clear, predictable tool names
  - Easy for LLMs to understand tool purpose
  - Scalable to large tool inventories
  - Improved documentation and discoverability
- **Negative**:
  - Longer tool names (avg 25-35 characters)
  - Requires discipline to follow convention
- **Mitigation**:
  - Code review checklist includes naming validation
  - Automated tests verify naming pattern
  - Documentation generator validates conventions

### [ADR-040] Implementation Notes

**Naming Validation** (unit test):

```java
@Test
void toolNamesShouldFollowConvention() {
    List<McpTool> tools = applicationContext.getBeansOfType(McpTool.class);

    for (McpTool tool : tools) {
        String name = tool.name();
        assertThat(name)
            .matches("^[a-z]+__[a-z]+_[a-z]+(_[a-z]+)*$")
            .withFailMessage("Tool name '%s' does not follow convention", name);
    }
}
```

**Category Registry**:

```java
public enum ToolCategory {
    JOURNEY("journey", "Journey planning and scheduling"),
    PLACE("place", "Stations, addresses, and points of interest"),
    FORMATION("formation", "Train composition and facilities"),
    ECO("eco", "Environmental impact and sustainability"),
    TRANSFER("transfer", "Connection optimization"),
    EVENTS("events", "Real-time service updates");

    private final String prefix;
    private final String description;
}
```

### [ADR-040] Migration Plan

**Existing Tools** (no change needed):

- All 16 current tools already follow this convention
- Validates convention is well-established

**Future Tools**:

1. Choose category from approved list
2. Select appropriate verb (find/get/compare/optimize)
3. Name resource (plural for collections, singular for items)
4. Add qualifier if needed for disambiguation
5. Validate with regex pattern in unit test

### [ADR-040] Related ADRs

- [ADR-019](#adr-019-use-mcp-protocol-2025-03-26-with-streamable-http-transport) -
  MCP protocol constraints
- [ADR-034](#adr-034-mcp-resources-and-prompts-for-llm-guidance) - Tool
  organization for LLM guidance
- [ADR-043](#adr-043-api-versioning-and-backward-compatibility) - Versioning
  strategy

---

## ADR-041: Cache TTL Strategy by Data Volatility

**Status**: 🔄 Proposed **Date**: 2026-01-28 **Deciders**: Architecture Team
**Context**: Performance & Caching

### [ADR-041] Context

Current caching uses mixed TTLs (24h for stations, 5min for journeys) but lacks
formal documentation on TTL selection criteria. Without clear strategy,
developers must guess appropriate TTL values, leading to:

- Over-caching: Stale data shown to users
- Under-caching: Unnecessary API calls and poor performance
- Inconsistent TTLs: Similar data with wildly different cache durations
- No rationale for TTL choices

### [ADR-041] Decision

Implement **tiered TTL strategy** based on data volatility and business
requirements.

**Tier 1: Static Reference Data (24 hours)**

```yaml
Data Types:
  - Station lists and metadata
  - Operator information
  - Train types and vehicle modes
  - Service calendars (holiday schedules)

Rationale: Changes infrequently (monthly/yearly)
TTL: 24 hours (86400 seconds)
Cache Key Example: 'station:metadata:8503000'
Invalidation: Manual flush on SBB API updates
```

**Tier 2: Semi-Static Data (1 hour)**

```yaml
Data Types:
  - Station facilities (ticket machines, restrooms)
  - Transfer time recommendations
  - Train formation patterns (typical car count)

Rationale: Occasionally updated (weekly/monthly)
TTL: 1 hour (3600 seconds)
Cache Key Example: 'station:facilities:8503000'
Invalidation: Time-based only
```

**Tier 3: Dynamic Data (5 minutes)**

```yaml
Data Types:
  - Journey search results
  - Pricing information
  - Available connections
  - Eco-comparison calculations

Rationale: Affected by timetable changes and availability
TTL: 5 minutes (300 seconds)
Cache Key Example: 'journey:search:8503000:8507000:2026-01-28T10:00'
Invalidation: Time-based + service alerts
```

**Tier 4: Real-Time Data (30 seconds)**

```yaml
Data Types:
  - Service alerts and disruptions
  - Platform changes
  - Delay information
  - Real-time train positions

Rationale: Time-critical information
TTL: 30 seconds
Cache Key Example: 'alerts:active:8503000'
Invalidation: Time-based + WebSocket updates
```

**Tier 5: Never Cache**

```yaml
Data Types:
  - Booking confirmations
  - Payment transactions
  - User sessions (handled separately)
  - Authentication tokens

Rationale: Must always be fresh and accurate
TTL: None
Cache: Never
```

### [ADR-041] Rationale

- ✅ **Balances Freshness vs Performance**: Appropriate TTL for each data type
- ✅ **Reduces API Costs**: Fewer unnecessary calls to SBB API
- ✅ **Ensures Critical Data Fresh**: Time-sensitive data has short TTL
- ✅ **Clear Guidelines**: Developers know which TTL to use
- ✅ **Predictable Performance**: Consistent cache behavior
- ✅ **Cost Optimization**: 90%+ cache hit rate on static data reduces SBB API
  costs

### [ADR-041] Alternatives Considered

1. **Single TTL for all data** (e.g., 5 minutes) - Rejected due to poor
   performance (too many cache misses on static data)
2. **No caching** - Rejected due to unacceptable latency and API costs
3. **Infinite TTL with manual invalidation** - Rejected due to operational
   complexity and stale data risk
4. **Client-controlled TTL** - Rejected due to inconsistent behavior and cache
   pollution

### [ADR-041] Consequences

- **Positive**:
  - Optimal balance of freshness and performance
  - Reduced SBB API usage (cost savings)
  - Clear decision framework for new data types
  - Predictable cache behavior
- **Negative**:
  - More complex cache configuration
  - Need to monitor cache hit rates per tier
  - Potential for stale data within TTL window
- **Mitigation**:
  - Prometheus metrics track hit rates per tier
  - Alerts if hit rates drop below thresholds
  - Documentation clearly maps data types to tiers

### [ADR-041] Implementation Notes

**Redis Configuration** (`application.yml`):

```yaml
cache:
  tiers:
    static-reference:
      ttl-seconds: 86400 # 24 hours
      max-entries: 10000
      target-hit-rate: 0.95
    semi-static:
      ttl-seconds: 3600 # 1 hour
      max-entries: 5000
      target-hit-rate: 0.90
    dynamic:
      ttl-seconds: 300 # 5 minutes
      max-entries: 1000
      target-hit-rate: 0.70
    realtime:
      ttl-seconds: 30
      max-entries: 500
      target-hit-rate: 0.60
```

**Cache Service Implementation**:

```java
@Service
public class TieredCacheService {

    public <T> Mono<T> get(String key, CacheTier tier,
                           Supplier<Mono<T>> fetcher) {
        return reactiveRedisTemplate.opsForValue()
            .get(key)
            .switchIfEmpty(fetcher.get()
                .flatMap(value -> cache(key, value, tier)
                    .thenReturn(value)));
    }

    private <T> Mono<Boolean> cache(String key, T value, CacheTier tier) {
        Duration ttl = Duration.ofSeconds(tier.getTtlSeconds());
        return reactiveRedisTemplate.opsForValue()
            .set(key, value, ttl);
    }
}

public enum CacheTier {
    STATIC_REFERENCE(86400, 0.95),
    SEMI_STATIC(3600, 0.90),
    DYNAMIC(300, 0.70),
    REALTIME(30, 0.60);

    private final long ttlSeconds;
    private final double targetHitRate;
}
```

### [ADR-041] Monitoring Strategy

**Metrics to Track**:

```java
// Cache hit rate per tier
cache_hit_rate{tier="static-reference"} > 0.95
cache_hit_rate{tier="semi-static"} > 0.90
cache_hit_rate{tier="dynamic"} > 0.70
cache_hit_rate{tier="realtime"} > 0.60

// Alert if hit rates drop
alert: CacheTierHitRateLow
  expr: cache_hit_rate < tier_target_hit_rate
  for: 10m
  annotations:
    summary: "Cache tier {{$labels.tier}} hit rate below target"
```

**Dashboard Panels**:

- Hit rate per tier (line chart)
- Cache size per tier (gauge)
- TTL distribution (histogram)
- API calls saved (counter)

### [ADR-041] Decision Tree for New Data

When adding new cached data, use this decision tree:

```
Does data change during the day?
├─ No → Static Reference (24h)
└─ Yes
   └─ Is it time-critical for safety/accuracy?
      ├─ Yes (disruptions, delays) → Real-Time (30s)
      └─ No
         └─ How often does it change?
            ├─ Multiple times per hour → Dynamic (5min)
            └─ Once per day or less → Semi-Static (1h)
```

### [ADR-041] Related ADRs

- [ADR-002](#adr-002-use-redis-for-state-management) - Redis as cache backend
- [ADR-004](#adr-004-no-in-memory-caches) - No in-memory caching policy
- [ADR-046](#adr-046-performance-slas-and-monitoring) - Performance monitoring

## ADR-043: API Versioning and Backward Compatibility

**Status**: 🔄 Proposed **Date**: 2026-01-28 **Deciders**: Architecture Team
**Context**: API Design

### [ADR-043] Context

MCP tools evolve over time as requirements change and features are added.
Without a clear versioning strategy:

- Breaking changes disrupt deployed LLM integrations
- No migration path for clients using old tool versions
- Unclear which changes are safe to make
- Difficult to deprecate outdated tools

Current situation:

- 16 tools in production (v2.0.1)
- No formal versioning policy for individual tools
- All changes assumed to be backward compatible
- No deprecation mechanism

### [ADR-043] Decision

Implement **additive-only changes with graceful degradation** and formal
deprecation process.

**Allowed Changes (Non-Breaking)**:

```yaml
✅ Adding new optional parameters:
   - Must have sensible defaults
   - Tool works without the parameter
   - Example: Add "maxTransfers" with default=3

✅ Adding new response fields:
   - Existing fields unchanged
   - New fields optional
   - Example: Add "carbonFootprint" to trip response

✅ Adding new MCP tools:
   - New tools don't affect existing ones
   - Example: Add "journey__optimize_for_comfort"

✅ Improving error messages:
   - More detailed error descriptions
   - Better error codes
   - Example: "INVALID_STATION" → "INVALID_STATION_CODE"

✅ Performance optimizations:
   - Faster response times
   - Reduced memory usage
   - Internal implementation changes

✅ Expanding enum values:
   - Add new transport modes
   - Add new fare types
   - Example: Add "CABLE_CAR" to transport modes
```

**Prohibited Changes (Breaking)**:

```yaml
❌ Removing parameters:
  - Breaks clients expecting parameter
  - Example: Remove "departureTime" parameter

❌ Removing response fields:
  - Breaks clients parsing response
  - Example: Remove "duration" from trip

❌ Renaming tools:
  - Breaks tool discovery
  - Example: Rename "journey__find_trips" to "journey__search_trips"

❌ Changing parameter types:
  - Breaks type validation
  - Example: Change "maxResults" from number to string

❌ Changing response structure:
  - Breaks response parsing
  - Example: Flatten nested "leg" objects

❌ Changing semantics:
  - Same parameter, different meaning
  - Example: Change "maxResults" to mean "per page" instead of "total"

❌ Removing enum values:
  - Breaks clients using the value
  - Example: Remove "TRAIN" from transport modes
```

**Breaking Change Process**:

When breaking changes are absolutely necessary:

```yaml
Step 1: Create New Tool Version
  - Tool name: {original}_v2
  - Example: journey__find_trips_v2
  - Implement new behavior
  - Update JSON schema

Step 2: Mark Old Tool Deprecated
  - Add ⚠️ DEPRECATED prefix to description
  - Include migration guide in description
  - Example: "⚠️ DEPRECATED: Use journey__find_trips_v2 instead.
             This version will be removed after 2026-06-01."

Step 3: Monitor Usage
  - Track old tool invocation count
  - Alert when usage drops below 5%
  - Set sunset date (minimum 6 months)

Step 4: Remove Old Version
  - Only after usage < 5% for 30 days
  - Announce removal 30 days in advance
  - Return helpful error pointing to new version
```

**Tool Description Deprecation Example**:

```json
{
  "name": "journey__find_trips",
  "description": "⚠️ DEPRECATED: Use journey__find_trips_v2 instead (adds real-time pricing). This version will be removed after 2026-06-01.\n\nFind trips between origin and destination with time windows..."
}
```

### [ADR-043] Rationale

- ✅ **Zero-Downtime Migrations**: Clients have time to upgrade
- ✅ **Clear Migration Path**: Versioned tools provide upgrade path
- ✅ **Usage-Driven Sunset**: Remove tools only when safe
- ✅ **Prevents Breaking Changes**: Clear rules for what's allowed
- ✅ **Protects Deployed Integrations**: No surprise breakage

### [ADR-043] Alternatives Considered

1. **Semantic versioning for entire server** - Rejected, too coarse-grained (one
   tool change forces major version)
2. **Breaking changes without versioning** - Rejected, breaks deployed clients
3. **API gateway with version routing** - Rejected, adds infrastructure
   complexity
4. **Separate versioned endpoints** - Rejected, MCP protocol doesn't support
   this
5. **Never allow breaking changes** - Rejected, too restrictive for long-term
   evolution

### [ADR-043] Consequences

- **Positive**:
  - Stable API for LLM clients
  - Clear upgrade path for breaking changes
  - No surprise breakage
  - Supports long-term evolution
- **Negative**:
  - Multiple tool versions to maintain
  - Need to track usage metrics
  - Increased complexity
- **Mitigation**:
  - Automated usage tracking
  - Clear deprecation timeline
  - Documentation for migration

### [ADR-043] Implementation Notes

**Usage Tracking**:

```java
@Component
public class ToolInvocationMetrics {
    private final MeterRegistry meterRegistry;

    public void recordInvocation(String toolName) {
        meterRegistry.counter("mcp.tool.invocations",
            "tool", toolName).increment();
    }
}

// Prometheus query for deprecation decision
sum(rate(mcp_tool_invocations{tool="journey__find_trips"}[7d]))
  /
sum(rate(mcp_tool_invocations[7d]))
< 0.05  // Alert if usage < 5%
```

**Deprecation Response**:

```java
@Override
public Mono<McpResult<JsonNode>> invoke(Map<String, Object> arguments) {
    if (isDeprecated()) {
        log.warn("Deprecated tool invoked: {} - Migrate to {}",
            name(), getReplacementTool());

        // Still process request, but log warning
        return processRequest(arguments)
            .map(result -> McpResult.successWithWarning(result,
                "⚠️ This tool is deprecated. Use " + getReplacementTool()));
    }
    return processRequest(arguments).map(McpResult::success);
}
```

**Migration Helper**:

```java
// After sunset date, tool returns error with migration guide
@Override
public Mono<McpResult<JsonNode>> invoke(Map<String, Object> arguments) {
    if (isSunset()) {
        return Mono.just(McpResult.failure(
            "TOOL_REMOVED",
            "This tool was removed on " + getSunsetDate() + ". " +
            "Please use " + getReplacementTool() + " instead. " +
            "Migration guide: " + getMigrationGuideUrl()
        ));
    }
    // Normal processing...
}
```

### [ADR-043] Example Scenario

**Scenario**: Add real-time pricing to `journey__find_trips`

**Breaking Change**: Response structure changes (adds nested pricing object)

**Implementation**:

```java
// Step 1: Create new version
@Component
public class FindTripsToolV2 implements McpTool<McpResult<JsonNode>> {
    @Override
    public String name() {
        return "journey__find_trips_v2";
    }

    @Override
    public String description() {
        return """
            Find trips with real-time pricing.
            Replaces journey__find_trips with enhanced pricing data.
            """;
    }

    @Override
    public String inputSchema() {
        // Same as v1, but adds optional "includePricing" parameter
    }
    // New implementation with pricing...
}

// Step 2: Mark old version deprecated
@Component
public class FindTripsTool implements McpTool<McpResult<JsonNode>> {
    @Override
    public String description() {
        return """
            ⚠️ DEPRECATED: Use journey__find_trips_v2 instead (adds real-time pricing).
            This version will be removed after 2026-07-28.

            Find trips between origin and destination...
            """;
    }

    // Keep old implementation for backward compatibility
}

// Step 3: Monitor usage for 6 months
// Step 4: Remove old version when usage < 5%
```

### [ADR-043] Related ADRs

- [ADR-008](#adr-008-use-semantic-versioning-semver) - Project-level versioning
- [ADR-019](#adr-019-use-mcp-protocol-2025-03-26-with-streamable-http-transport) -
  MCP protocol constraints
- [ADR-040](#adr-040-mcp-tool-naming-convention) - Tool naming conventions

---

## ADR-044: Resilience4j Configuration Strategy

**Status**: 🔄 Proposed **Date**: 2026-01-28 **Deciders**: Architecture Team
**Context**: Operations & Reliability

### [ADR-044] Context

Current Resilience4j configuration (50% failure threshold, 30s wait, 3 retries)
lacks formal documentation on how these values were chosen. Without
evidence-based rationale:

- Difficult to tune settings for different scenarios
- No clear understanding of why specific values were selected
- Hard to adapt configuration as SBB API behavior changes
- New developers don't understand trade-offs

### [ADR-044] Decision

Document **evidence-based resilience configuration** with clear rationale for
each setting.

**Circuit Breaker Settings**:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      sbbApi:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        sliding-window-size: 100
        sliding-window-type: COUNT_BASED
        minimum-number-of-calls: 10
        permitted-number-of-calls-in-half-open-state: 5
```

**Rationale**:

- `failure-rate-threshold: 50%` - SBB API has 99.5% SLA; 50% indicates severe
  outage, not transient errors
- `wait-duration-in-open-state: 30s` - Typical SBB incident response time;
  prevents overwhelming recovering service
- `sliding-window-size: 100` - Statistical significance for failure rate
  calculation
- `minimum-number-of-calls: 10` - Prevent premature circuit opening from small
  samples
- `permitted-calls-in-half-open: 5` - Test recovery without flooding

**Rate Limiter Settings**:

```yaml
resilience4j:
  ratelimiter:
    instances:
      sbbApi:
        limit-for-period: 100
        limit-refresh-period: 1s
        timeout-duration: 0ms
```

**Rationale**:

- `limit-for-period: 100` - SBB API allows 100 requests/second per client
- `limit-refresh-period: 1s` - Matches SBB API rate limit window
- `timeout-duration: 0ms` - Fail immediately if limit exceeded (no queueing)

**Retry Settings**:

```yaml
resilience4j:
  retry:
    instances:
      sbbApi:
        max-attempts: 3
        wait-duration: 500ms
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - org.springframework.web.reactive.function.client.WebClientRequestException
          - java.net.ConnectException
        ignore-exceptions:
          - ch.sbb.ki.journeyservicemcp.exception.ValidationException
```

**Rationale**:

- `max-attempts: 3` - Most transient failures resolve within 2-3 seconds
- `wait-duration: 500ms` - Initial wait before first retry
- `exponential-backoff: 2` - Doubles wait time: 500ms → 1s → 2s (total 3.5s)
- `retry-exceptions` - Only retry network errors, not business logic errors

### [ADR-044] Rationale

- ✅ **Evidence-Based**: Configuration tied to SBB API behavior and SLA
- ✅ **Documented Trade-offs**: Clear understanding of why each value chosen
- ✅ **Tunable**: Can adjust based on observed behavior
- ✅ **Prevents Cascading Failures**: Circuit breaker isolates failures
- ✅ **Respects Rate Limits**: Prevents quota exhaustion

### [ADR-044] Alternatives Considered

1. **Default Resilience4j settings** - Rejected due to generic values not
   optimized for SBB API
2. **More aggressive retries** (5+ attempts) - Rejected due to increased latency
   and API load
3. **No circuit breaker** - Rejected due to cascading failure risk
4. **Shorter wait duration** (10s) - Rejected, insufficient time for incident
   response

### [ADR-044] Consequences

- **Positive**:
  - Optimized for SBB API characteristics
  - Clear rationale aids future tuning
  - Prevents API quota exhaustion
  - Graceful degradation during outages
- **Negative**:
  - Configuration complexity
  - Need to monitor effectiveness
- **Mitigation**:
  - Prometheus metrics for circuit breaker state
  - Alerts when circuit opens frequently
  - Regular review of settings vs observed behavior

### [ADR-044] Monitoring

**Metrics to Track**:

```yaml
# Circuit breaker state changes
resilience4j_circuitbreaker_state{name="sbbApi", state="closed|open|half_open"}

# Retry attempts
resilience4j_retry_calls{name="sbbApi", kind="successful_with_retry|failed_with_retry"}

# Rate limiter rejections
resilience4j_ratelimiter_available_permissions{name="sbbApi"}
```

**Alerts**:

```yaml
# Alert if circuit opens frequently
alert: SbbApiCircuitBreakerOpenFrequently
  expr: rate(resilience4j_circuitbreaker_state{state="open"}[5m]) > 0.1
  for: 5m
  annotations:
    summary: "SBB API circuit breaker opening frequently"

# Alert if many retries
alert: SbbApiHighRetryRate
  expr: rate(resilience4j_retry_calls{kind="successful_with_retry"}[5m]) > 10
  for: 10m
  annotations:
    summary: "High retry rate for SBB API calls"
```

### [ADR-044] Related ADRs

- [ADR-012B](#adr-012b-use-error-handling--circuit-breaking-for-external-apis) -
  Circuit breaker pattern
- [ADR-020](#adr-020-use-api-first-strategy-with-fallback-for-sbb-integration) -
  SBB API integration
- [ADR-046](#adr-046-performance-slas-and-monitoring) - Performance monitoring

---

## ADR-045: JSON Schema Validation for MCP Tool Inputs

**Status**: 🔄 Proposed **Date**: 2026-01-28 **Deciders**: Architecture Team
**Context**: Development Standards

### [ADR-045] Context

MCP tools receive arguments as `Map<String, Object>` with JSON Schema
definitions for structure validation. Without clear validation strategy:

- Runtime errors from invalid arguments
- Inconsistent validation across tools
- Poor error messages for LLMs
- Difficult to debug argument issues

### [ADR-045] Decision

Implement **two-tier validation** strategy.

**Tier 1: JSON Schema Validation** (MCP Protocol Layer)

Handled automatically by MCP framework before tool invocation:

```json
{
  "type": "object",
  "properties": {
    "origin": { "type": "string", "minLength": 1 },
    "destination": { "type": "string", "minLength": 1 },
    "departureTime": { "type": "string", "format": "date-time" }
  },
  "required": ["origin", "destination"]
}
```

Validates:

- Correct types (string, number, boolean, array, object)
- Required fields present
- Basic format constraints (date-time, email, etc.)
- Min/max length, min/max value

Returns `400 Bad Request` with detailed error on schema violation.

**Tier 2: Business Logic Validation** (Tool Layer)

```java
@Override
public Mono<McpResult<JsonNode>> invoke(Map<String, Object> arguments) {
    return Mono.fromCallable(() -> validateArguments(arguments))
        .flatMap(request -> journeyService.findTrips(request))
        .map(McpResult::success)
        .onErrorResume(ValidationException.class, e ->
            Mono.just(McpResult.failure("INVALID_PARAMETERS", e.getMessage())));
}

private TripRequest validateArguments(Map<String, Object> args) {
    String origin = (String) args.get("origin");
    String destination = (String) args.get("destination");

    // Business rule validation
    if (origin.equals(destination)) {
        throw new ValidationException(
            "origin and destination must be different");
    }

    if (args.containsKey("departureTime")) {
        String timeStr = (String) args.get("departureTime");
        LocalDateTime departureTime = LocalDateTime.parse(timeStr);

        if (departureTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException(
                "departureTime must be in the future");
        }
    }

    return new TripRequest(origin, destination, ...);
}
```

Validates:

- Swiss station codes (format, existence)
- Date ranges (future dates, reasonable booking window)
- Business constraints (origin ≠ destination)
- Cross-field dependencies
- Reasonable limits (maxResults < 100)

### [ADR-045] Validation Rules by Category

**Journey Tools**:

```java
- origin/destination: Non-empty, different from each other
- departureTime/arrivalTime: Future time, within 1 year
- maxTransfers: 0-10 (reasonable limit)
- maxResults: 1-50 (prevent abuse)
```

**Place Tools**:

```java
- radius: 1-50000 meters (prevent excessive area searches)
- limit: 1-100 results
- coordinates: Valid lat/long (-90 to 90, -180 to 180)
```

**Formation Tools**:

```java
- trainNumber: 4-6 digits
- date: Within ±30 days (formation data availability)
```

### [ADR-045] Rationale

- ✅ **Fail Fast**: Invalid arguments caught before processing
- ✅ **Clear Error Messages**: LLMs learn from validation errors
- ✅ **Prevents Downstream Failures**: Don't call SBB API with invalid data
- ✅ **Consistent Validation**: All tools follow same pattern
- ✅ **Type Safety**: Early conversion to domain objects

### [ADR-045] Alternatives Considered

1. **Schema validation only** - Rejected, can't express business rules in JSON
   Schema
2. **Manual validation in each method** - Rejected, inconsistent and error-prone
3. **Bean Validation (JSR-380)** - Rejected, doesn't work well with reactive
   Map<String, Object>
4. **No validation** - Rejected, leads to poor error messages and downstream
   failures

### [ADR-045] Consequences

- **Positive**:
  - Better error messages
  - Prevents invalid API calls
  - Easier debugging
  - LLMs learn correct parameter usage
- **Negative**:
  - Additional validation code
  - Must maintain validation logic
- **Mitigation**:
  - Shared validation utilities
  - Unit tests for validation logic
  - Clear error message templates

### [ADR-045] Implementation Pattern

**Validation Utility** (`ValidationUtils.java`):

```java
public class ValidationUtils {
    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                fieldName + " must not be blank");
        }
    }

    public static void requireFutureTime(LocalDateTime time, String fieldName) {
        if (time.isBefore(LocalDateTime.now())) {
            throw new ValidationException(
                fieldName + " must be in the future");
        }
    }

    public static void requireRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(
                fieldName + " must be between " + min + " and " + max);
        }
    }

    public static void requireDifferent(String value1, String value2,
                                       String field1, String field2) {
        if (value1.equals(value2)) {
            throw new ValidationException(
                field1 + " and " + field2 + " must be different");
        }
    }
}
```

**Usage in Tool**:

```java
private TripRequest validateArguments(Map<String, Object> args) {
    String origin = (String) args.get("origin");
    String destination = (String) args.get("destination");

    ValidationUtils.requireNonBlank(origin, "origin");
    ValidationUtils.requireNonBlank(destination, "destination");
    ValidationUtils.requireDifferent(origin, destination, "origin", "destination");

    if (args.containsKey("maxResults")) {
        int maxResults = ((Number) args.get("maxResults")).intValue();
        ValidationUtils.requireRange(maxResults, 1, 50, "maxResults");
    }

    return new TripRequest(...);
}
```

### [ADR-045] Error Message Format

```json
{
  "isSuccess": false,
  "errorCode": "INVALID_PARAMETERS",
  "errorMessage": "origin and destination must be different",
  "details": {
    "field": "destination",
    "value": "Zürich HB",
    "constraint": "must be different from origin"
  }
}
```

### [ADR-045] Testing

```java
@Test
void shouldRejectSameOriginAndDestination() {
    Map<String, Object> args = Map.of(
        "origin", "Zürich HB",
        "destination", "Zürich HB"
    );

    StepVerifier.create(tool.invoke(args))
        .expectNextMatches(result ->
            !result.isSuccess() &&
            result.getErrorCode().equals("INVALID_PARAMETERS") &&
            result.getErrorMessage().contains("must be different"))
        .verifyComplete();
}

@Test
void shouldRejectPastDepartureTime() {
    Map<String, Object> args = Map.of(
        "origin", "Zürich HB",
        "destination", "Bern",
        "departureTime", "2020-01-01T10:00:00"
    );

    StepVerifier.create(tool.invoke(args))
        .expectNextMatches(result ->
            !result.isSuccess() &&
            result.getErrorMessage().contains("must be in the future"))
        .verifyComplete();
}
```

### [ADR-045] Related ADRs

- [ADR-031](#adr-031-use-rfc-7807-structured-error-propagation) - Error response
  format
- [ADR-019](#adr-019-use-mcp-protocol-2025-03-26-with-streamable-http-transport) -
  MCP protocol JSON Schema
- [ADR-042](#adr-042-reactive-error-propagation-patterns) - Error handling
  patterns

---

## ADR-046: Performance SLAs and Monitoring

**Status**: 🔄 Proposed **Date**: 2026-01-28 **Deciders**: Architecture Team
**Context**: Observability & Performance

### [ADR-046] Context

CLAUDE.md mentions performance targets (P50: 0-2ms, P95: 2-3ms) but these are
not formally documented as SLAs. Without formal SLAs:

- No accountability for performance degradation
- Unclear when to investigate performance issues
- No baseline for capacity planning
- Difficult to justify infrastructure investments

### [ADR-046] Decision

Establish **formal performance SLAs** with automated monitoring and alerting.

**Response Time SLAs** (99th percentile):

```yaml
Tier 1: In-Memory Operations (Redis cache hit)
  P50: < 2ms
  P95: < 5ms
  P99: < 10ms
  Target Availability: 99.95%
  Examples: Station metadata, operator info

Tier 2: SBB API Calls (cache miss)
  P50: < 200ms
  P95: < 500ms
  P99: < 1000ms
  Target Availability: 99.5% (SBB SLA)
  Examples: Single journey query

Tier 3: Complex Operations (multi-call workflows)
  P50: < 300ms
  P95: < 800ms
  P99: < 1500ms
  Target Availability: 99.5%
  Examples: Journey planning with pricing, eco-comparison

Tier 4: Real-Time Operations (streaming)
  P50: < 100ms
  P95: < 300ms
  P99: < 500ms
  Target Availability: 99.9%
  Examples: Service alerts, disruption notifications
```

**System-Wide SLAs**:

```yaml
Availability:
  Target: 99.9% uptime (excluding planned maintenance)
  Allowed Downtime: 43 minutes/month
  Measurement: Health check endpoint

Error Rate:
  Target: < 0.5% of all requests
  Excludes: 4xx client errors (user mistakes)
  Includes: 5xx server errors, timeouts

Cache Performance:
  Static Data Hit Rate: > 90%
  Dynamic Data Hit Rate: > 70%
  Cache Latency P95: < 5ms
```

### [ADR-046] Monitoring Implementation

**Prometheus Metrics**:

```yaml
# Response time by tool and tier
http_server_requests_seconds{
  tool="journey__find_trips",
  tier="sbb-api",
  quantile="0.5|0.95|0.99"
}

# Error rate
http_server_requests_total{
  tool="journey__find_trips",
  status="200|400|500"
}

# Cache hit rate
cache_requests_total{
  tier="static-reference",
  result="hit|miss"
}

# SLA compliance
sla_compliance{
  tier="in-memory",
  metric="p99_latency",
  status="compliant|violated"
}
```

**Grafana Dashboards**:

```yaml
Dashboard 1:
  SLA Overview - SLA compliance by tier (gauge) - Response time trends (line
  chart) - Error rate (gauge) - Availability (uptime percentage)

Dashboard 2:
  Tool Performance - Response time by tool (heatmap) - Request volume by tool
  (bar chart) - Error rate by tool (table) - Cache hit rate by tool (line chart)

Dashboard 3:
  Infrastructure Health - Redis latency (line chart) - SBB API latency (line
  chart) - Circuit breaker state (state diagram) - Thread pool utilization
  (gauge)
```

**Alerting Rules**:

```yaml
alert: SLAViolationP99Latency
  expr: histogram_quantile(0.99, http_server_requests_seconds) > tier_p99_sla
  for: 5m
  severity: warning
  annotations:
    summary: "P99 latency exceeds SLA for {{ $labels.tier }}"
    description: "P99 latency is {{ $value }}ms, SLA is {{ $labels.sla }}ms"

alert: HighErrorRate
  expr: rate(http_server_requests_total{status=~"5.."}[5m]) > 0.005
  for: 5m
  severity: critical
  annotations:
    summary: "Error rate exceeds 0.5% threshold"

alert: LowCacheHitRate
  expr: (
    rate(cache_requests_total{result="hit"}[10m]) /
    rate(cache_requests_total[10m])
  ) < tier_target_hit_rate
  for: 15m
  severity: warning
  annotations:
    summary: "Cache hit rate below target for {{ $labels.tier }}"

alert: ServiceUnavailable
  expr: up{job="journey-service-mcp"} == 0
  for: 1m
  severity: critical
  annotations:
    summary: "Journey Service MCP is down"
```

### [ADR-046] SLA Reporting

**Weekly Report** (automated):

```yaml
Subject: Journey Service MCP - Weekly SLA Report

SLA Compliance: 99.87% ✅
- Tier 1 (In-Memory): 99.98% ✅
- Tier 2 (SBB API): 99.76% ✅
- Tier 3 (Complex): 99.65% ✅

Performance Summary:
- Average Response Time: 245ms
- P99 Response Time: 892ms (target: <1500ms) ✅
- Total Requests: 1,234,567
- Error Rate: 0.42% ✅

Top 5 Slowest Tools:
1. journey__compare_routes - 487ms P95
2. eco__compare_journeys - 423ms P95
3. journey__find_trips - 412ms P95
4. journey__get_trip_details - 387ms P95
5. formation__get_train_formation - 342ms P95

Cache Performance:
- Static Data Hit Rate: 94.2% ✅
- Dynamic Data Hit Rate: 72.8% ✅

SLA Violations:
- 2026-01-22 14:30-14:45: SBB API outage (99.2% availability)
```

### [ADR-046] Rationale

- ✅ **Clear Expectations**: Users and stakeholders know what to expect
- ✅ **Proactive Issue Detection**: Alerts before users complain
- ✅ **Data-Driven Optimization**: Know where to focus improvement efforts
- ✅ **Capacity Planning**: Historical trends guide infrastructure decisions
- ✅ **Accountability**: Measurable performance commitments

### [ADR-046] Alternatives Considered

1. **No formal SLAs** - Rejected, no accountability or clear expectations
2. **Single SLA for all operations** - Rejected, different operations have
   different characteristics
3. **P50 only** - Rejected, doesn't capture tail latency issues
4. **Manual reporting** - Rejected, time-consuming and error-prone

### [ADR-046] Consequences

- **Positive**:
  - Clear performance expectations
  - Early warning of degradation
  - Data-driven decisions
  - Improved user experience
- **Negative**:
  - Monitoring infrastructure investment
  - Alert fatigue if thresholds wrong
  - Pressure to meet SLAs
- **Mitigation**:
  - Use Cloud Run built-in metrics where possible
  - Tune alert thresholds based on actual behavior
  - SLAs based on realistic targets, not aspirational

### [ADR-046] Related ADRs

- [ADR-009](#adr-009-use-micrometer-for-observability) - Metrics infrastructure
- [ADR-024](#adr-024-use-loki--promtail--grafana-for-observability) -
  Observability stack
- [ADR-041](#adr-041-cache-ttl-strategy-by-data-volatility) - Cache performance
- [ADR-044](#adr-044-resilience4j-configuration-strategy) - Resilience
  monitoring

---

## ADR-047: Graceful Shutdown and In-Flight Request Handling

**Status**: 🔄 Proposed **Date**: 2026-01-28 **Deciders**: Architecture Team
**Context**: Operations & Reliability

### [ADR-047] Context

Cloud Run deployments can terminate instances during:

- New releases (rolling deployments)
- Autoscaling down (reduced traffic)
- Instance replacement (maintenance)

Without graceful shutdown:

- In-flight requests aborted mid-processing
- Users see "Connection reset" errors
- Incomplete SBB API calls may cause inconsistent state
- Poor user experience during deployments

### [ADR-047] Decision

Implement **graceful shutdown** with configurable grace period and proper
cleanup.

**Configuration** (`application.yml`):

```yaml
server:
  shutdown: graceful # Enable graceful shutdown

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s # Max wait for in-flight requests
```

**Shutdown Workflow**:

```yaml
Phase 1:
  SIGTERM Signal (t=0s) - Cloud Run sends SIGTERM to instance - Spring Boot
  initiates graceful shutdown - Server stops accepting new requests - Returns
  503 Service Unavailable for new requests

Phase 2:
  In-Flight Processing (t=0-30s) - Existing requests continue processing -
  Circuit breakers open immediately (prevent new SBB API calls) - Health check
  returns DOWN (remove from load balancer) - Background tasks given time to
  complete

Phase 3:
  Forced Shutdown (t=30s) - If requests still in-flight, force shutdown - Cloud
  Run sends SIGKILL (10s after SIGTERM) - Clean up resources (close Redis
  connections, etc.)

Phase 4:
  Termination (t=40s max) - Instance fully terminated - Cloud Run removes from
  service
```

**Health Check Behavior**:

```java
@Component
public class ShutdownAwareHealthIndicator implements HealthIndicator {
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    @EventListener
    public void onShutdownEvent(ContextClosedEvent event) {
        shuttingDown.set(true);
    }

    @Override
    public Health health() {
        if (shuttingDown.get()) {
            return Health.down()
                .withDetail("reason", "Instance shutting down")
                .build();
        }
        return Health.up().build();
    }
}
```

**Circuit Breaker Shutdown Behavior**:

```java
@Component
public class ShutdownCircuitBreakerListener {
    @EventListener
    public void onShutdown(ContextClosedEvent event) {
        // Open all circuit breakers immediately
        circuitBreakerRegistry.getAllCircuitBreakers()
            .forEach(cb -> cb.transitionToForcedOpenState());

        log.info("All circuit breakers opened for graceful shutdown");
    }
}
```

**Request Rejection Filter**:

```java
@Component
public class GracefulShutdownFilter implements WebFilter {
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    @EventListener
    public void onShutdown(ContextClosedEvent event) {
        shuttingDown.set(true);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (shuttingDown.get()) {
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            exchange.getResponse().getHeaders().add("Retry-After", "10");
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }
}
```

### [ADR-047] Monitoring

```yaml
# Track shutdown duration
spring_application_shutdown_duration_seconds

# Track rejected requests during shutdown
http_server_requests_total{status="503", reason="shutdown"}

# Alert if shutdown takes too long
alert: GracefulShutdownTimeout
  expr: spring_application_shutdown_duration_seconds > 30
  for: 1m
  annotations:
    summary: "Graceful shutdown exceeded 30s timeout"
```

### [ADR-047] Related ADRs

- [ADR-012B](#adr-012b-use-error-handling--circuit-breaking-for-external-apis) -
  Circuit breaker integration
- [ADR-050](#adr-050-health-check-strategy) - Health check implementation

---

## ADR-048: Data Retention and Privacy Policy

**Status**: 🔄 Proposed **Date**: 2026-01-28 **Deciders**: Architecture Team
**Context**: Security & Compliance

### [ADR-048] Context

System handles user journey queries that may contain PII (locations, travel
patterns). Need clear data retention and privacy policy for GDPR compliance.
Without formal policy:

- Legal risk from improper data handling
- Unclear what data is stored and for how long
- No documented compliance with GDPR
- Users don't know how their data is used

### [ADR-048] Decision

Implement **privacy-first data retention** with minimal data collection.

**PII Handling Principles**:

```yaml
Data Minimization:
  - ✅ Process journey queries in-memory only
  - ✅ Pass queries to SBB API without persistence
  - ✅ No database of user search history
  - ❌ Never store user locations
  - ❌ Never track travel patterns

Transparency:
  - ✅ Privacy policy exposed at /privacy endpoint
  - ✅ Clear data retention periods
  - ✅ Documented data processing purpose

Purpose Limitation:
  - ✅ Data used only for journey planning
  - ❌ No analytics on user behavior
  - ❌ No marketing or profiling

Security:
  - ✅ PII masked in logs (ADR-013)
  - ✅ TLS for all communications
  - ✅ No PII in error messages
```

**Data Retention Policy**:

```yaml
User Sessions (Redis):
  Retention: 24 hours (TTL-based auto-deletion)
  Contents: Session ID, last activity timestamp
  No PII: ✅ (session ID is random UUID)

API Request Logs (Cloud Logging):
  Retention: 30 days
  Contents: Request path, response status, latency
  PII Masked: ✅ (origin/destination masked as <LOCATION>)
  Purpose: Debugging and performance monitoring

Error Logs (Cloud Logging):
  Retention: 90 days
  Contents: Stack traces, error messages
  PII Masked: ✅ (all user data redacted)
  Purpose: Bug fixes and error analysis

Metrics (Prometheus/Cloud Monitoring):
  Retention: 1 year
  Contents: Aggregated counts, latencies, error rates
  No PII: ✅ (only aggregated statistics)
  Purpose: Performance analysis and capacity planning

Audit Trail (Event Sourcing):
  Retention: 7 years (compliance requirement)
  Contents: MCP tool invocations, timestamps
  PII Masked: ✅ (tool name + timestamp only)
  Purpose: Security auditing and compliance
```

**GDPR Compliance**:

```yaml
Right to be Forgotten:
  Implementation: Auto-deletion via TTL
  Manual Process: Not needed (no persistent user data)
  Response Time: Immediate (data not stored)

Data Portability:
  Implementation: Not applicable (no user data stored)

Consent:
  Required: No (legitimate interest: service provision)
  Opt-out: Users can choose not to use service

Data Protection Impact Assessment (DPIA):
  Status: Completed
  Risk Level: Low (minimal PII processing)
  Safeguards: TTL-based deletion, log masking, no profiling
```

**Log Masking Implementation**:

```java
@Component
public class PiiMaskingFilter implements WebFilter {
    private static final Set<String> PII_FIELDS = Set.of(
        "origin", "destination", "location", "address"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
            .contextWrite(ctx -> ctx.put("maskPii", true))
            .doOnSuccess(v -> {
                if (log.isDebugEnabled()) {
                    Map<String, Object> maskedArgs = maskPiiFields(
                        exchange.getRequest().getQueryParams());
                    log.debug("Request: {}", maskedArgs);
                }
            });
    }

    private Map<String, Object> maskPiiFields(Map<String, ?> data) {
        return data.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> PII_FIELDS.contains(e.getKey()) ? "<REDACTED>" : e.getValue()
            ));
    }
}
```

**Privacy Policy Endpoint**:

```java
@RestController
public class PrivacyController {
    @GetMapping("/privacy")
    public Mono<PrivacyPolicy> getPrivacyPolicy() {
        return Mono.just(new PrivacyPolicy(
            "Journey Service MCP Privacy Policy",
            "We process journey planning queries in-memory only. " +
            "No personal data is stored beyond 24-hour session cache. " +
            "Logs are masked and retained for 30-90 days for debugging. " +
            "See full policy at: https://journey-service-mcp.docs/privacy",
            Map.of(
                "sessions", "24 hours",
                "request_logs", "30 days (PII masked)",
                "error_logs", "90 days (PII masked)",
                "metrics", "1 year (aggregated only)"
            )
        ));
    }
}
```

### [ADR-048] Rationale

- ✅ **GDPR Compliant**: Minimal data collection, auto-deletion, transparency
- ✅ **User Trust**: Clear privacy policy builds confidence
- ✅ **Legal Protection**: Documented compliance with privacy regulations
- ✅ **Minimal Risk**: No PII storage means minimal breach risk
- ✅ **Operational Simplicity**: TTL-based deletion, no manual processes

### [ADR-048] Alternatives Considered

1. **Store user history for personalization** - Rejected, privacy risk and not
   needed
2. **Longer log retention** (1 year) - Rejected, unnecessary for debugging
3. **No privacy policy** - Rejected, legal risk and lack of transparency
4. **Encryption only** - Rejected, doesn't address data retention

### [ADR-048] Consequences

- **Positive**:
  - GDPR compliant out of the box
  - Minimal data breach risk
  - Builds user trust
  - Simple compliance processes
- **Negative**:
  - Can't offer personalized recommendations
  - Limited historical analytics
- **Mitigation**:
  - Personalization not needed for journey planning
  - Aggregated metrics sufficient for analytics

### [ADR-048] Compliance Checklist

```yaml
✅ Privacy policy published at /privacy ✅ TTL-based auto-deletion implemented
✅ PII masking in logs verified ✅ No user profiling or tracking ✅ TLS enforced
for all connections ✅ Security audit completed ✅ DPIA documented ✅ Retention
periods documented ✅ Data minimization principle followed
```

### [ADR-048] Related ADRs

- [ADR-007](#adr-007-use-event-sourcing-for-audit-trail) - Audit trail
  implementation
- [ADR-013](#adr-013-use-structured-json-logging-with-pii-masking) - Log masking
- [ADR-041](#adr-041-cache-ttl-strategy-by-data-volatility) - TTL-based
  retention

---

### [ADR-049] Monitoring and Tuning

**Metrics to Track**:

```yaml
# Thread pool utilization
reactor_scheduler_threads_active{scheduler="boundedElastic"}
reactor_scheduler_threads_total{scheduler="boundedElastic"}

# Queue depth
reactor_scheduler_queued_tasks{scheduler="boundedElastic"}

# Task wait time
reactor_scheduler_task_wait_time_seconds

# CPU utilization
system_cpu_usage
process_cpu_usage
```

**Tuning Indicators**:

```yaml
Increase Thread Pool If:
  - Thread utilization consistently >80%
  - Task wait time >100ms
  - Request latency increasing
  - CPU utilization <50% (underutilized)

Decrease Thread Pool If:
  - Thread utilization consistently <20%
  - High memory usage
  - Thread context switching overhead high

Keep Current If:
  - Thread utilization 20-80%
  - Stable latency
  - No queued tasks
```

### [ADR-049] Rationale

- ✅ **Right-Sized**: Based on actual workload characteristics
- ✅ **Documented**: Clear reasoning for future tuning
- ✅ **Conservative**: Headroom for traffic spikes
- ✅ **Cost-Effective**: No over-provisioning
- ✅ **Explainable**: Team understands trade-offs

### [ADR-049] Alternatives Considered

1. **Fixed thread pool** (e.g., always 16) - Rejected, doesn't scale with CPU
   cores
2. **Unlimited thread pool** - Rejected, memory exhaustion risk
3. **CPU cores × 2** - Rejected, too small for I/O-bound workload
4. **CPU cores × 100** - Rejected, unnecessary for reactive workload

### [ADR-049] Consequences

- **Positive**:
  - Optimal resource utilization
  - Clear capacity planning
  - Documented tuning process
- **Negative**:
  - May need adjustment as workload changes
- **Mitigation**:
  - Monitor thread utilization
  - Review quarterly
  - Adjust based on metrics

### [ADR-049] Testing

**Load Test Scenario**:

```bash
# Test with different thread pool sizes
for threads in 4 8 16 32; do
  echo "Testing with $threads threads"

  # Set thread pool size
  export REACTOR_BOUNDED_ELASTIC_MAX_THREADS=$threads

  # Run load test
  ab -n 10000 -c 100 https://journey-service-mcp.run.app/mcp/tools

  # Measure latency, throughput, CPU usage
done

# Expected Result: 8-16 threads optimal for throughput/latency balance
```

### [ADR-049] Related ADRs

- [ADR-046](#adr-046-performance-slas-and-monitoring) - Performance SLAs

---

## ADR-050: Health Check Strategy

**Status**: 🔄 Proposed **Date**: 2026-01-28 **Deciders**: Architecture Team
**Context**: Operations & Monitoring

### [ADR-050] Context

Cloud Run uses `/actuator/health` for readiness/liveness checks, but current
implementation:

- Doesn't validate critical dependencies (Redis, SBB API)
- Same endpoint for liveness and readiness
- May cause restart loops if dependencies fail
- No clear separation of concerns

### [ADR-050] Decision

Implement **tiered health checks** with separate liveness and readiness
endpoints.

**Liveness Check** (`/actuator/health/liveness`):

```yaml
Purpose: Is the application running?
Checks:
  - ✅ JVM is running
  - ✅ Application context started
  - ✅ No deadlocks
Excludes:
  - ❌ External dependencies (Redis, SBB API)
  - ❌ Circuit breaker state
Response:
  UP: Application is alive, don't restart
  DOWN: Application is dead, restart immediately
```

**Rationale**: Never check external dependencies in liveness. If Redis is down,
restarting the app won't help.

**Readiness Check** (`/actuator/health/readiness`):

```yaml
Purpose: Can the application handle requests?
Checks:
  - ✅ Redis connection (ping command)
  - ✅ SBB API reachable (lightweight health endpoint)
  - ✅ Circuit breakers not in FORCED_OPEN state
  - ✅ Thread pool not exhausted
Response:
  UP: Ready to serve traffic
  DOWN: Remove from load balancer (don't send requests)
```

**Rationale**: If dependencies unavailable, remove instance from load balancer
but don't restart (may recover).

**Implementation**:

**Liveness Indicator** (always healthy unless app crashed):

```java
// Spring Boot default - no custom indicator needed
// Checks JVM health only
```

**Redis Health Indicator**:

```java
@Component
public class RedisHealthIndicator implements HealthIndicator {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public Health health() {
        try {
            // Lightweight ping command (< 5ms)
            redisTemplate.execute(RedisCommand.PING)
                .block(Duration.ofSeconds(2));

            return Health.up()
                .withDetail("redis", "connected")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("redis", "unreachable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

**SBB API Health Indicator**:

```java
@Component
public class SbbApiHealthIndicator implements HealthIndicator {
    private final WebClient sbbClient;

    @Override
    public Health health() {
        try {
            // Lightweight health check endpoint
            // (not full journey query)
            sbbClient.get()
                .uri("/health")
                .retrieve()
                .toBodilessEntity()
                .block(Duration.ofSeconds(2));

            return Health.up()
                .withDetail("sbbApi", "reachable")
                .build();
        } catch (Exception e) {
            log.warn("SBB API health check failed", e);
            return Health.down()
                .withDetail("sbbApi", "unreachable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

**Circuit Breaker Health Indicator**:

```java
@Component
public class CircuitBreakerHealthIndicator implements HealthIndicator {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Health health() {
        List<String> openCircuits = circuitBreakerRegistry.getAllCircuitBreakers()
            .stream()
            .filter(cb -> cb.getState() == CircuitBreaker.State.FORCED_OPEN)
            .map(CircuitBreaker::getName)
            .toList();

        if (openCircuits.isEmpty()) {
            return Health.up()
                .withDetail("circuitBreakers", "all closed")
                .build();
        }

        return Health.down()
            .withDetail("circuitBreakers", "forced open")
            .withDetail("openCircuits", openCircuits)
            .build();
    }
}
```

**Configuration**:

```yaml
# application.yml
management:
  endpoint:
    health:
      probes:
        enabled: true # Enable liveness/readiness endpoints
      show-details: when-authorized
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
    redis:
      enabled: true
    circuitBreaker:
      enabled: true
```

**Cloud Run Configuration**:

```yaml
# cloud-run.yaml
apiVersion: serving.knative.dev/v1
kind: Service
spec:
  template:
    spec:
      containers:
        - image: gcr.io/project/journey-service-mcp
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 3 # Restart after 3 failures

          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 10
            timeoutSeconds: 5
            failureThreshold: 3 # Remove from LB after 3 failures
```

### [ADR-050] Health Check Response Examples

**Healthy Instance**:

```json
GET /actuator/health/readiness

{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {"redis": "connected"}
    },
    "sbbApi": {
      "status": "UP",
      "details": {"sbbApi": "reachable"}
    },
    "circuitBreakers": {
      "status": "UP",
      "details": {"circuitBreakers": "all closed"}
    }
  }
}
```

**Unhealthy Instance** (Redis down):

```json
GET /actuator/health/readiness

{
  "status": "DOWN",
  "components": {
    "redis": {
      "status": "DOWN",
      "details": {
        "redis": "unreachable",
        "error": "Connection refused"
      }
    },
    "sbbApi": {
      "status": "UP"
    },
    "circuitBreakers": {
      "status": "UP"
    }
  }
}
```

### [ADR-050] Rationale

- ✅ **Prevents Restart Loops**: Liveness doesn't check dependencies
- ✅ **Graceful Degradation**: Remove from LB but don't restart
- ✅ **Fast Health Checks**: < 100ms (lightweight ping/health endpoints)
- ✅ **Clear Separation**: Liveness vs readiness have different purposes
- ✅ **Observable**: Detailed health status for debugging

### [ADR-050] Alternatives Considered

1. **Single health endpoint** - Rejected, causes restart loops when dependencies
   fail
2. **No dependency checks** - Rejected, may route traffic to broken instances
3. **Deep health checks** (full journey query) - Rejected, too slow and
   expensive
4. **Health checks disabled** - Rejected, Cloud Run requires health checks

### [ADR-050] Consequences

- **Positive**:
  - No restart loops
  - Traffic routed only to healthy instances
  - Fast health check responses
  - Clear observability
- **Negative**:
  - More complex health check logic
  - Must maintain health indicators
- **Mitigation**:
  - Shared health indicator base classes
  - Comprehensive testing
  - Monitoring health check latency

### [ADR-050] Monitoring

```yaml
# Health check latency
http_server_requests_seconds{uri="/actuator/health/readiness"}

# Health check failures
health_check_failures_total{component="redis|sbbApi|circuitBreakers"}

# Alert if health checks consistently failing
alert: HealthCheckFailureRate
  expr: rate(health_check_failures_total[5m]) > 0.1
  for: 5m
  annotations:
    summary: "Health checks failing for {{ $labels.component }}"
```

### [ADR-050] Testing

```java
@Test
void livenessCheckShouldNotDependOnRedis() {
    // Stop Redis
    redisContainer.stop();

    // Liveness should still be UP
    webTestClient.get()
        .uri("/actuator/health/liveness")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.status").isEqualTo("UP");
}

@Test
void readinessCheckShouldFailWhenRedisDown() {
    // Stop Redis
    redisContainer.stop();

    // Readiness should be DOWN
    webTestClient.get()
        .uri("/actuator/health/readiness")
        .exchange()
        .expectStatus().is5xxServerError()
        .expectBody()
        .jsonPath("$.status").isEqualTo("DOWN")
        .jsonPath("$.components.redis.status").isEqualTo("DOWN");
}

@Test
void readinessCheckShouldFailWhenCircuitBreakerOpen() {
    // Force circuit breaker open
    circuitBreaker.transitionToForcedOpenState();

    // Readiness should be DOWN
    webTestClient.get()
        .uri("/actuator/health/readiness")
        .exchange()
        .expectStatus().is5xxServerError()
        .expectBody()
        .jsonPath("$.status").isEqualTo("DOWN")
        .jsonPath("$.components.circuitBreakers.status").isEqualTo("DOWN");
}
```

### [ADR-050] Related ADRs

- [ADR-012B](#adr-012b-use-error-handling--circuit-breaking-for-external-apis) -
  Circuit breaker integration
- [ADR-047](#adr-047-graceful-shutdown-and-in-flight-request-handling) -
  Shutdown health check behavior

---

## ADR-051: Use Spring AI 2.0 for AI Integration

**Status**: 🔄 Proposed **Date**: 2026-01-30 **Deciders**: Architecture Team
**Context**: Architecture & Design

### [ADR-051] Context

The Journey Service MCP needs AI integration capabilities for:

- **LLM-powered features**: Natural language processing for journey queries, intelligent suggestions, and conversational interfaces
- **Multi-model support**: Ability to integrate with different LLM providers (OpenAI, Anthropic Claude, Azure OpenAI, local models)
- **Structured outputs**: Type-safe responses with Java records and JSON serialization
- **Async integration**: Non-blocking AI operations with CompletableFuture for concurrent processing
- **MCP protocol enhancement**: Potential future use of AI models within MCP sampling and prompt responses
- **Observability**: Consistent metrics, logging, and tracing for AI operations

Current challenges with direct API integration:

- Each LLM provider has different API contracts and authentication mechanisms
- No standardized abstraction for prompt templates, function calling, or embeddings
- Manual handling of streaming responses, retries, and rate limiting
- Difficult to switch between providers or use multiple models
- No built-in Spring Boot integration for configuration and observability

### [ADR-051] Decision

Adopt **Spring AI 2.0** as the standard framework for all AI/LLM integration in the Journey Service MCP.

**Core Components**:

```java
// Spring AI 2.0 dependency
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter</artifactId>
    <version>2.0.0</version>
</dependency>

// Model-specific starters (choose based on provider)
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-anthropic-spring-boot-starter</artifactId>
</dependency>
```

**Key Features Used**:

1. **Unified Chat API**: Consistent interface across all LLM providers

   ```java
   @Service
   public class AiService {
       private final ChatClient chatClient;

       public AiService(ChatClient.Builder chatClientBuilder) {
           this.chatClient = chatClientBuilder.build();
       }

       public String generateResponse(String prompt) {
           return chatClient.prompt()
               .user(prompt)
               .call()
               .content();
       }

       public Stream<String> generateStreamingResponse(String prompt) {
           return chatClient.prompt()
               .user(prompt)
               .stream()
               .content();
       }
   }
   ```

2. **Structured Outputs**: Type-safe responses with Java records

   ```java
   public record JourneySuggestion(
       String destination,
       LocalDateTime departureTime,
       int estimatedDuration,
       String reasoning
   ) {}

   public JourneySuggestion suggestJourney(String query) {
       return chatClient.prompt()
           .user(query)
           .call()
           .entity(JourneySuggestion.class);
   }

   // Async version with CompletableFuture
   public CompletableFuture<JourneySuggestion> suggestJourneyAsync(String query) {
       return CompletableFuture.supplyAsync(() ->
           chatClient.prompt()
               .user(query)
               .call()
               .entity(JourneySuggestion.class)
       );
   }
   ```

3. **Prompt Templates**: Reusable, parameterized prompts
   ```java
   @Component
   public class JourneyPrompts {
       private final PromptTemplate journeyTemplate = new PromptTemplate("""
           Based on the following journey request:
           {request}

           Current weather conditions: {weather}
           Available connections: {connections}

           Provide a journey suggestion optimized for:
           - Travel time
           - Weather conditions
           - Connection reliability
           """);

       public Prompt createJourneyPrompt(String request, String weather, String connections) {
           return journeyTemplate.create(Map.of(
               "request", request,
               "weather", weather,
               "connections", connections
           ));
       }
   }
   ```

4. **Function Calling**: Native tool/function calling support
   ```java
   @Component
   public class JourneyFunctions {
       @FunctionDefinition("Search for train connections between two locations")
       public List<Connection> searchConnections(
           @FunctionParameter("origin station") String origin,
           @FunctionParameter("destination station") String destination,
           @FunctionParameter("departure time in ISO format") String departureTime
       ) {
           // Implementation
           return connectionService.search(origin, destination, departureTime);
       }
   }

   // Use in chat client
   chatClient.prompt()
       .user("Find me trains from Zurich to Bern tomorrow at 9 AM")
       .functions("searchConnections")
       .call()
       .content();
   ```

5. **Streaming Responses**: Stream AI responses as they are generated

   ```java
   public Stream<ChatResponse> streamResponse(String prompt) {
       return chatClient.prompt()
           .user(prompt)
           .stream()
           .chatResponse();
   }

   // Process streaming responses
   public void processStreamingResponse(String prompt) {
       streamResponse(prompt).forEach(response -> {
           log.info("Received chunk: {}", response);
           // Process each chunk as it arrives
       });
   }
   ```

6. **Multi-Modal Support**: Handle images, documents, and structured data

   ```java
   public String analyzeWeatherMap(byte[] weatherMapImage, String query) {
       return chatClient.prompt()
           .user(userSpec -> userSpec
               .text(query)
               .media(MimeTypeUtils.IMAGE_PNG, weatherMapImage)
           )
           .call()
           .content();
   }

   // Async version
   public CompletableFuture<String> analyzeWeatherMapAsync(byte[] weatherMapImage, String query) {
       return CompletableFuture.supplyAsync(() ->
           analyzeWeatherMap(weatherMapImage, query)
       );
   }
   ```

7. **Provider Abstraction**: Easy switching between models
   ```yaml
   # application.yml
   spring:
     ai:
       openai:
         api-key: ${OPENAI_API_KEY}
         chat:
           model: gpt-4o
           temperature: 0.7
       anthropic:
         api-key: ${ANTHROPIC_API_KEY}
         chat:
           model: claude-sonnet-4-5-20250929
           temperature: 0.7
   ```

8. **Observability Integration**: Built-in metrics and tracing
   ```java
   // Automatic metrics exposed
   ai.chat.requests.total
   ai.chat.requests.duration
   ai.chat.tokens.input
   ai.chat.tokens.output
   ai.chat.errors.total
   ```

**Configuration**:

```yaml
# application.yml
spring:
  ai:
    # Model selection strategy
    chat:
      default-model: anthropic  # or openai, azure

    # Retry configuration
    retry:
      max-attempts: 3
      backoff:
        initial-interval: 1s
        multiplier: 2
        max-interval: 10s

    # Rate limiting
    rate-limiter:
      enabled: true
      requests-per-minute: 60

    # Observability
    observability:
      include-prompt: false  # Don't log full prompts (PII concerns)
      include-completion: false
      metrics-enabled: true
      tracing-enabled: true
```

**Architecture Integration**:

```java
@Configuration
public class SpringAiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultOptions(ChatOptions.builder()
                .temperature(0.7)
                .maxTokens(2000)
                .build())
            .defaultAdvisors(
                new SimpleLoggerAdvisor(),  // Log requests/responses
                new MessageChatMemoryAdvisor(),  // Conversation memory
                new QuestionAnswerAdvisor()  // RAG support
            )
            .build();
    }

    @Bean
    public VectorStore vectorStore(EmbeddingClient embeddingClient) {
        // For RAG use cases (journey documentation, station info)
        return new SimpleVectorStore(embeddingClient);
    }
}
```

### [ADR-051] Rationale

**Why Spring AI 2.0**:

- ✅ **Vendor Neutrality**: Abstract away provider-specific APIs, easily switch between OpenAI, Anthropic, Azure, and local models
- ✅ **Spring Ecosystem Integration**: Native Spring Boot auto-configuration, properties, and dependency injection
- ✅ **Async Support**: Non-blocking AI operations with CompletableFuture and streaming responses
- ✅ **Type Safety**: Structured outputs with Java records eliminate JSON parsing boilerplate
- ✅ **Function Calling**: Native support for tool use/function calling (critical for MCP integration)
- ✅ **Production Ready**: Built-in retry logic, rate limiting, circuit breakers, and observability
- ✅ **Active Development**: Backed by Spring team, regular updates, strong community
- ✅ **Multi-Modal**: Support for images, documents, audio (future use cases)
- ✅ **RAG Support**: Built-in vector store abstractions and document retrieval
- ✅ **Template System**: Reusable, parameterized prompts reduce duplication

**Spring AI 2.0 Improvements over 1.x**:

- Simplified Chat API with fluent builder pattern
- Better streaming support with Java Stream API integration
- Enhanced function calling with automatic JSON schema generation
- Improved structured output parsing
- Better error handling and retry mechanisms
- Native support for conversational memory
- Enhanced observability with detailed metrics

### [ADR-051] Alternatives Considered

1. **Direct LLM Provider APIs** (OpenAI SDK, Anthropic SDK)
   - ❌ **Rejected**: Tight coupling to specific providers, difficult to switch
   - ❌ No unified abstraction, duplicated code for each provider
   - ❌ Manual retry logic, rate limiting, and observability
   - ✅ Lower abstraction overhead, direct access to all features

2. **LangChain4j** (Java port of LangChain)
   - ❌ **Rejected**: Less mature Java implementation compared to Python version
   - ❌ Not Spring-native, requires additional integration work
   - ❌ Different programming model (chains, agents) vs Spring's imperative style
   - ✅ More opinionated workflows, good for complex agent systems
   - ✅ Larger ecosystem of integrations

3. **Custom AI Abstraction Layer**
   - ❌ **Rejected**: Significant development and maintenance overhead
   - ❌ Reinventing the wheel when Spring AI exists
   - ❌ Won't benefit from community contributions and updates
   - ✅ Complete control over API design
   - ✅ Minimal dependencies

4. **Azure AI SDK** (if using Azure-hosted models)
   - ❌ **Rejected**: Locks into Azure ecosystem
   - ❌ Not Spring-native integration
   - ✅ Optimized for Azure services
   - ✅ Strong enterprise support

### [ADR-051] Consequences

**Positive**:
- Provider flexibility: easily switch between OpenAI, Anthropic, local models
- Reduced boilerplate: no manual JSON parsing, retry logic, or API client management
- Spring ecosystem: leverage existing Spring Boot features (config, actuator, security)
- Async support: seamless integration with CompletableFuture and streaming APIs
- Type safety: structured outputs eliminate runtime JSON errors
- Observability: automatic metrics and tracing for AI operations
- Future-proof: Spring AI evolves with ecosystem, new providers added regularly
- Testing: easy to mock/stub ChatClient for unit tests
- Function calling: native support for MCP tool integration in AI responses

**Negative**:
- New dependency: adds Spring AI (~5MB) and provider-specific starters
- Learning curve: team must learn Spring AI abstractions and patterns
- Abstraction overhead: may not expose all provider-specific features immediately
- Version coupling: tied to Spring AI release cycle for updates
- Limited customization: some advanced provider features may require workarounds

**Mitigation**:
- Comprehensive team training on Spring AI 2.0 patterns and best practices
- Document common patterns and example code in project wiki
- Create reusable service templates for AI integration
- Monitor Spring AI changelog for new features and updates
- Use provider-specific clients directly only when absolutely necessary (document why)
- Implement thorough testing with AI response mocking

### [ADR-051] Implementation Guidelines

**Service Layer Pattern**:

```java
@Service
@Slf4j
public class AiJourneyService {
    private final ChatClient chatClient;
    private final JourneyPrompts prompts;
    private final ExecutorService executorService;

    public AiJourneyService(
        ChatClient.Builder chatClientBuilder,
        JourneyPrompts prompts
    ) {
        this.chatClient = chatClientBuilder
            .defaultOptions(ChatOptions.builder()
                .model("claude-sonnet-4-5-20250929")
                .temperature(0.7)
                .build())
            .build();
        this.prompts = prompts;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    // Blocking version
    public JourneySuggestion suggestJourney(
        String request,
        WeatherConditions weather,
        List<Connection> connections
    ) {
        Prompt prompt = prompts.createJourneyPrompt(
            request,
            weather.toJson(),
            connectionsToJson(connections)
        );

        return chatClient.prompt(prompt)
            .call()
            .entity(JourneySuggestion.class);
    }

    // Async version with CompletableFuture
    public CompletableFuture<JourneySuggestion> suggestJourneyAsync(
        String request,
        WeatherConditions weather,
        List<Connection> connections
    ) {
        return CompletableFuture.supplyAsync(
            () -> suggestJourney(request, weather, connections),
            executorService
        );
    }

    // Streaming version
    public Stream<String> streamExplanation(String query) {
        log.info("Starting AI stream for: {}", query);
        return chatClient.prompt()
            .user(query)
            .stream()
            .content()
            .onClose(() -> log.info("AI stream completed"));
    }
}
```

**Testing Pattern**:

```java
@SpringBootTest
class AiJourneyServiceTest {

    @Autowired
    private AiJourneyService aiService;

    @MockBean
    private ChatClient chatClient;

    @Test
    void shouldSuggestJourney() {
        // Mock AI response
        JourneySuggestion expected = new JourneySuggestion(
            "Bern",
            LocalDateTime.now(),
            90,
            "Optimal route with good weather"
        );

        CallResponseSpec mockResponse = mock(CallResponseSpec.class);
        when(mockResponse.entity(JourneySuggestion.class)).thenReturn(expected);

        PromptRequestSpec promptSpec = mock(PromptRequestSpec.class);
        when(chatClient.prompt(any(Prompt.class))).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(mockResponse);

        // Test blocking version
        JourneySuggestion result = aiService.suggestJourney(
            "Zurich to Bern",
            weather,
            connections
        );

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldSuggestJourneyAsync() throws Exception {
        // Mock AI response
        JourneySuggestion expected = new JourneySuggestion(
            "Bern",
            LocalDateTime.now(),
            90,
            "Optimal route with good weather"
        );

        // Setup mocks (same as above)

        // Test async version
        CompletableFuture<JourneySuggestion> future =
            aiService.suggestJourneyAsync("Zurich to Bern", weather, connections);

        JourneySuggestion result = future.get(5, TimeUnit.SECONDS);
        assertThat(result).isEqualTo(expected);
    }
}
```

**Cost Monitoring**:

```java
@Component
public class AiCostTracker {
    private final MeterRegistry meterRegistry;

    @EventListener
    public void onChatResponse(ChatResponseEvent event) {
        Usage usage = event.getResponse().getMetadata().getUsage();

        meterRegistry.counter("ai.tokens.input",
            "model", event.getModel(),
            "provider", event.getProvider()
        ).increment(usage.getInputTokens());

        meterRegistry.counter("ai.tokens.output",
            "model", event.getModel(),
            "provider", event.getProvider()
        ).increment(usage.getOutputTokens());

        // Calculate estimated cost (provider-specific)
        double cost = calculateCost(
            event.getProvider(),
            usage.getInputTokens(),
            usage.getOutputTokens()
        );

        meterRegistry.counter("ai.cost.total.usd",
            "model", event.getModel()
        ).increment(cost);
    }
}
```

### [ADR-051] Migration Plan

**Phase 1: Foundation** (Week 1)
- Add Spring AI dependencies to `pom.xml`
- Configure primary model provider (Anthropic Claude)
- Create base configuration and service templates
- Set up observability and metrics

**Phase 2: Core Use Cases** (Week 2-3)
- Implement journey suggestion service
- Add natural language query parsing
- Create prompt templates library
- Implement function calling for MCP tools

**Phase 3: Advanced Features** (Week 4)
- Add conversational memory for multi-turn dialogs
- Implement RAG for journey documentation
- Add multi-provider fallback strategy
- Enhance cost tracking and alerting

**Phase 4: Production Hardening** (Week 5)
- Comprehensive testing with real models
- Performance tuning and caching
- Security review (API keys, prompt injection)
- Documentation and team training

### [ADR-051] Security Considerations

**API Key Management**:
```yaml
# Never commit API keys!
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}  # From environment variable
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
```

**Prompt Injection Protection**:
```java
public class PromptSanitizer {
    private static final Pattern INJECTION_PATTERN =
        Pattern.compile("ignore previous|disregard instructions",
                       Pattern.CASE_INSENSITIVE);

    public String sanitize(String userInput) {
        if (INJECTION_PATTERN.matcher(userInput).find()) {
            log.warn("Potential prompt injection detected: {}", userInput);
            throw new SecurityException("Invalid input");
        }
        return userInput;
    }
}
```

**PII Handling**:

```java
// Never send PII to AI models without user consent
public String processQuery(String query, UserContext user) {
    String sanitizedQuery = piiMasker.mask(query);  // Remove names, addresses, etc.

    return chatClient.prompt()
        .user(sanitizedQuery)
        .call()
        .content();
}

// Async version
public CompletableFuture<String> processQueryAsync(String query, UserContext user) {
    return CompletableFuture.supplyAsync(() -> processQuery(query, user));
}
```

### [ADR-051] Monitoring and Alerting

**Key Metrics**:
```yaml
# Prometheus metrics
ai.chat.requests.total{model, provider, status}
ai.chat.requests.duration{model, provider}
ai.tokens.input{model, provider}
ai.tokens.output{model, provider}
ai.cost.total.usd{model}
ai.errors.total{model, provider, error_type}
```

**Alerts**:
```yaml
# High error rate
alert: AiHighErrorRate
  expr: rate(ai.errors.total[5m]) > 0.1
  for: 5m
  annotations:
    summary: "AI requests failing at {{ $value }} req/s"

# High cost burn rate
alert: AiHighCostBurn
  expr: rate(ai.cost.total.usd[1h]) > 10
  for: 10m
  annotations:
    summary: "AI costs burning at ${{ $value }}/hour"

# Slow response times
alert: AiSlowResponses
  expr: histogram_quantile(0.95, ai.chat.requests.duration) > 30
  for: 5m
  annotations:
    summary: "95th percentile AI response time: {{ $value }}s"
```

### [ADR-051] Related ADRs

- [ADR-030](#adr-030-mcp-sampling-for-agentic-data-synthesis) - MCP sampling integration with AI
- [ADR-019](#adr-019-use-mcp-protocol-2025-03-26-with-streamable-http-transport) - MCP protocol enhancement with AI capabilities
- [ADR-009](#adr-009-use-micrometer-for-observability) - Observability for AI operations
- [ADR-013](#adr-013-use-structured-json-logging-with-pii-masking) - PII masking in AI prompts/responses
- [ADR-027](#adr-027-use-java-records-for-all-dtos-and-domain-models) - Java Records for structured AI outputs
