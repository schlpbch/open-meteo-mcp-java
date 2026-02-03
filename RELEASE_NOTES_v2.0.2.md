# Release Notes v2.0.2

**Release Date**: February 3, 2026

## Summary

Maintenance release with test improvements, documentation cleanup, and simplified MCP tool naming.

## Changes

### Refactoring

- **Simplified MCP tool names**: Removed `meteo__` server prefix from all tool and prompt names
  - Tools: `search_location`, `get_weather`, `get_snow_conditions`, `get_air_quality`, `get_weather_alerts`, `get_comfort_index`, `get_astronomy`, `search_location_swiss`, `compare_locations`, `get_historical_weather`, `get_marine_conditions`
  - Prompts: `ski-trip-weather`, `plan-outdoor-activity`, `weather-aware-travel`

### Testing

- Improved test coverage with new test classes:
  - `AdvancedToolsHandlerTest` (19 tests)
  - `ChatHandlerTest` (9 tests)
  - `RedisConversationMemoryServiceTest` (15 tests)
- Expanded existing test classes:
  - `HistoricalWeatherServiceTest`
  - `MarineConditionsServiceTest`
  - `McpToolsHandlerTest`
- Removed redundant annotation-only tests
- **426 tests passing** (100% pass rate)
- **72% code coverage**

### Documentation

- Optimized `CLAUDE.md`: Reduced from 1,592 to 207 lines (87% reduction)
- Removed duplicate sections across documentation
- Updated all documentation to reflect new tool names

## Compatibility

- Fully backward compatible with v2.0.x clients
- MCP clients should update tool name references (remove `meteo__` prefix)

## Upgrade Notes

If you have MCP client configurations referencing tool names, update them:

```diff
- meteo__get_weather
+ get_weather

- meteo__search_location
+ search_location
```

## Statistics

| Metric | Value |
|--------|-------|
| Tests | 426 |
| Coverage | 72% |
| MCP Tools | 11 |
| MCP Resources | 4 |
| MCP Prompts | 3 |
