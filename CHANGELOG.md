# Changelog

All notable changes to the Open Meteo MCP Java server will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-02-02

### Added
- **7 Advanced Tools** - Complete migration of all advanced tools from Python
  - `meteo__get_weather_alerts` - Weather alerts based on thresholds
  - `meteo__get_comfort_index` - Outdoor activity comfort score (0-100)
  - `meteo__get_astronomy` - Sunrise, sunset, golden hour, moon phase
  - `meteo__search_location_swiss` - Swiss-specific location search
  - `meteo__compare_locations` - Multi-location weather comparison
  - `meteo__get_historical_weather` - Historical weather data (1940-present)
  - `meteo__get_marine_conditions` - Wave/swell data for lakes and coasts

- **Helper Classes** - Specialized calculation utilities
  - `WeatherAlertGenerator` - Alert generation with severity levels
  - `ComfortIndexCalculator` - Multi-factor comfort scoring
  - `AstronomyCalculator` - Astronomical calculations

- **Services** - New API integration services
  - `HistoricalWeatherService` - Open-Meteo Archive API integration
  - `MarineConditionsService` - Open-Meteo Marine API integration

- **Comprehensive Test Coverage** - 19 unit tests
  - `WeatherAlertGeneratorTest` - 7 tests
  - `ComfortIndexCalculatorTest` - 4 tests
  - `AstronomyCalculatorTest` - 4 tests
  - `HistoricalWeatherServiceTest` - 2 tests
  - `MarineConditionsServiceTest` - 2 tests

### Changed
- **SBB MCP Ecosystem v2.0.0 Compliance** - Added `meteo__` namespace prefix
  - All 4 core tools now use `meteo__` prefix
  - All 3 prompts now use `meteo__` prefix
  - Federation-ready naming convention

### Technical Details
- Total tools: 11 (4 core + 7 advanced)
- Test coverage: 19 unit tests, 100% passing
- Migration status: 100% complete (Python → Java)
- Build status: ✅ SUCCESS
- Compilation: 41 source files

## [1.0.0] - 2026-01-XX

### Added
- Initial Java implementation with Spring AI MCP
- 4 core tools:
  - `search_location` - Location geocoding
  - `get_weather` - Weather forecast
  - `get_snow_conditions` - Snow/ski conditions
  - `get_air_quality` - Air quality data
- 3 prompts:
  - `ski-trip-weather`
  - `plan-outdoor-activity`
  - `weather-aware-travel`
- 4 resources:
  - `weather://codes`
  - `weather://parameters`
  - `weather://aqi-reference`
  - `weather://swiss-locations`

[1.1.0]: https://github.com/schlpbch/open-meteo-mcp-java/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/schlpbch/open-meteo-mcp-java/releases/tag/v1.0.0
