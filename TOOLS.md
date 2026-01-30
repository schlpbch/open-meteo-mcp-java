# MCP Tools Implementation

This document describes the 4 MCP tools implemented for the Open Meteo MCP Server (Java).

## Overview

The tools are exposed through two interfaces:

1. **MCP Protocol** - Via `McpToolsHandler` component (native MCP integration)
2. **REST API** - Via `McpToolsController` endpoints (HTTP/JSON access)

## Architecture

- **Handler**: `src/main/java/com/openmeteo/mcp/tool/McpToolsHandler.java`
- **Controller**: `src/main/java/com/openmeteo/mcp/tool/McpToolsController.java`

Both components delegate to the service layer:
- `LocationService` - Geocoding operations
- `WeatherService` - Weather forecasts with interpretation
- `SnowConditionsService` - Snow data with ski condition assessment
- `AirQualityService` - Air quality with AQI interpretation

---

## Tools

### 1. search_location

Search for locations by name using geocoding.

**Handler Method:**
```java
CompletableFuture<GeocodingResponse> searchLocation(
    String name,       // Location name (required)
    int count,         // Max results (1-100, default: 10)
    String language,   // Language code (default: "en")
    String country     // ISO 3166-1 alpha-2 country code (optional)
)
```

**REST Endpoint:**
```
GET /api/tools/search-location?name=London&count=10&language=en&country=
```

**Example Requests:**

```bash
# Search for "London" with 10 results
curl "http://localhost:8080/api/tools/search-location?name=London&count=10&language=en"

# Search for "Zurich" in Switzerland
curl "http://localhost:8080/api/tools/search-location?name=Zurich&count=5&language=en&country=CH"

# Search in German language
curl "http://localhost:8080/api/tools/search-location?name=Berlin&count=5&language=de"
```

**Response:**
```json
{
  "results": [
    {
      "name": "London",
      "latitude": 51.5074,
      "longitude": -0.1278,
      "elevation": 11.0,
      "country": "United Kingdom",
      "country_code": "GB",
      "admin1": "England"
    },
    ...
  ]
}
```

---

### 2. get_weather

Get weather forecast with temperature, precipitation, wind, and current conditions.
Includes enriched data with WMO code interpretation and travel impact assessment.

**Handler Method:**
```java
CompletableFuture<Map<String, Object>> getWeather(
    double latitude,      // Latitude (-90 to 90, required)
    double longitude,     // Longitude (-180 to 180, required)
    int forecastDays,     // Forecast days (1-16, default: 7)
    String timezone       // Timezone ID (default: "UTC")
)
```

**REST Endpoint:**
```
GET /api/tools/weather?latitude=51.5074&longitude=-0.1278&forecastDays=7&timezone=Europe/London
```

**Example Requests:**

```bash
# London - 7 days
curl "http://localhost:8080/api/tools/weather?latitude=51.5074&longitude=-0.1278&forecastDays=7&timezone=Europe/London"

# Paris - 3 days
curl "http://localhost:8080/api/tools/weather?latitude=48.8566&longitude=2.3522&forecastDays=3&timezone=Europe/Paris"

# Tokyo - 10 days
curl "http://localhost:8080/api/tools/weather?latitude=35.6762&longitude=139.6503&forecastDays=10&timezone=Asia/Tokyo"
```

**Response:**
```json
{
  "forecast": {
    "latitude": 51.5074,
    "longitude": -0.1278,
    "timezone": "Europe/London",
    "current_weather": {
      "temperature": 8.5,
      "windspeed": 12.3,
      "winddirection": 240,
      "weathercode": 61
    },
    "daily": {
      "time": ["2026-01-30", "2026-01-31", ...],
      "temperature_2m_max": [10.5, 9.2, ...],
      "temperature_2m_min": [5.1, 4.3, ...],
      "precipitation_sum": [2.5, 0.0, ...],
      "weather_code": [61, 1, ...]
    },
    "hourly": {
      "time": [...],
      "temperature_2m": [...],
      "precipitation": [...],
      ...
    }
  },
  "interpretation": {
    "description": "Moderate or heavy rain",
    "category": "Rain",
    "severity": "Moderate",
    "travel_impact": "Slippery roads, reduced visibility",
    "formatted_temperature": "8.5°C",
    "formatted_wind": "12.3 km/h WSW"
  }
}
```

---

### 3. get_snow_conditions

Get snow conditions for ski trip planning with depth, recent snowfall, and assessments.
Includes enriched data with ski condition assessment based on snow depth, recent snowfall,
temperature, and current weather conditions.

**Handler Method:**
```java
CompletableFuture<Map<String, Object>> getSnowConditions(
    double latitude,      // Latitude (-90 to 90, required)
    double longitude,     // Longitude (-180 to 180, required)
    int forecastDays,     // Forecast days (1-16, default: 7)
    String timezone       // Timezone ID (default: "UTC")
)
```

**REST Endpoint:**
```
GET /api/tools/snow-conditions?latitude=46.4917&longitude=10.2619&forecastDays=5&timezone=Europe/Zurich
```

**Example Requests:**

```bash
# Swiss Alps - 5 days
curl "http://localhost:8080/api/tools/snow-conditions?latitude=46.4917&longitude=10.2619&forecastDays=5&timezone=Europe/Zurich"

# Dolomites (Italy) - 3 days
curl "http://localhost:8080/api/tools/snow-conditions?latitude=45.4872&longitude=11.8753&forecastDays=3&timezone=Europe/Venice"

# Chamonix (France) - 7 days
curl "http://localhost:8080/api/tools/snow-conditions?latitude=45.9237&longitude=6.8694&forecastDays=7&timezone=Europe/Paris"
```

**Response:**
```json
{
  "conditions": {
    "latitude": 46.4917,
    "longitude": 10.2619,
    "timezone": "Europe/Zurich",
    "daily": {
      "time": ["2026-01-30", "2026-01-31", ...],
      "weather_code": [80, 81, ...],
      "temperature_2m_max": [-2.5, -1.0, ...],
      "temperature_2m_min": [-5.3, -4.2, ...],
      "snowfall_sum": [15.0, 8.5, ...]
    },
    "hourly": {
      "time": [...],
      "temperature_2m": [...],
      "snowfall": [...],
      "snow_depth": [0.45, 0.52, ...],
      ...
    }
  },
  "ski_assessment": {
    "assessment": "Good - Fresh snow with good base depth",
    "snow_depth_m": 0.45,
    "recent_snowfall_cm": 15.0,
    "temperature_c": -3.2
  }
}
```

**Ski Condition Assessment Values:**
- `"Excellent"` - Deep powder, fresh snow, ideal conditions
- `"Good"` - Fresh snow with adequate base
- `"Fair"` - Some snow coverage, variable conditions
- `"Poor"` - Thin snow coverage or bare spots

---

### 4. get_air_quality

Get air quality forecast with AQI, pollutants, UV index, and pollen data.
Returns enriched data with AQI level interpretation and UV index guidance.

**Handler Method:**
```java
CompletableFuture<Map<String, Object>> getAirQuality(
    double latitude,      // Latitude (-90 to 90, required)
    double longitude,     // Longitude (-180 to 180, required)
    int forecastDays,     // Forecast days (1-5, default: 3)
    boolean includePollen,// Include pollen data (default: true)
    String timezone       // Timezone ID (default: "UTC")
)
```

**REST Endpoint:**
```
GET /api/tools/air-quality?latitude=52.52&longitude=13.405&forecastDays=3&includePollen=true&timezone=Europe/Berlin
```

**Example Requests:**

```bash
# Berlin - 3 days with pollen
curl "http://localhost:8080/api/tools/air-quality?latitude=52.52&longitude=13.405&forecastDays=3&includePollen=true&timezone=Europe/Berlin"

# New York - 2 days without pollen
curl "http://localhost:8080/api/tools/air-quality?latitude=40.7128&longitude=-74.0060&forecastDays=2&includePollen=false&timezone=America/New_York"

# Tokyo - 5 days (pollen data not available, ignored)
curl "http://localhost:8080/api/tools/air-quality?latitude=35.6762&longitude=139.6503&forecastDays=5&includePollen=true&timezone=Asia/Tokyo"
```

**Response:**
```json
{
  "forecast": {
    "latitude": 52.52,
    "longitude": 13.405,
    "timezone": "Europe/Berlin",
    "current": {
      "european_aqi": 35,
      "us_aqi": 42,
      "pm10": 15.2,
      "pm2_5": 8.5,
      "o3": 45.2,
      "no2": 28.5,
      "so2": 2.1,
      "co": 450.5,
      "uv_index": 0.8,
      "pollen_alder": 45,
      "pollen_birch": 120,
      "pollen_grass": 5,
      "pollen_mugwort": 0,
      "pollen_olive": 0,
      "pollen_ragweed": 0
    },
    "daily": {
      "time": ["2026-01-30", "2026-01-31", "2026-02-01"],
      "european_aqi_max": [35, 42, 38],
      "pm10_max": [15.2, 22.1, 18.5],
      "pm2_5_max": [8.5, 12.3, 10.2],
      ...
    },
    "hourly": {
      "time": [...],
      "european_aqi": [...],
      "pm10": [...],
      "pm2_5": [...],
      ...
    }
  },
  "interpretation": {
    "european_aqi_level": "Good",
    "us_aqi_level": "Good",
    "uv_index_level": "Low"
  }
}
```

**AQI Level Interpretation:**
- `"Good"` (0-50 EU / 0-50 US) - Air quality is satisfactory
- `"Fair"` (51-100 EU / 51-100 US) - Acceptable air quality
- `"Moderate"` (101-150 EU / 101-150 US) - Sensitive individuals may experience effects
- `"Poor"` (151-200 EU / 151-200 US) - Air quality is poor
- `"Very Poor"` (201-300 EU / 201-300 US) - Health warnings
- `"Extremely Poor"` (300+ EU / 301+ US) - Serious health concerns

**UV Index Level Interpretation:**
- `"Low"` (0-2) - Minimal UV exposure risk
- `"Moderate"` (3-5) - Moderate UV exposure risk
- `"High"` (6-7) - High UV exposure risk
- `"Very High"` (8-10) - Very high UV exposure risk
- `"Extreme"` (11+) - Extreme UV exposure risk

---

## Health Check

**REST Endpoint:**
```
GET /api/tools/health
```

**Response:**
```json
{
  "status": "OK",
  "service": "Open-Meteo MCP Tools"
}
```

---

## Running the Application

### Start the server:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Test an endpoint:
```bash
# Test weather tool
curl "http://localhost:8080/api/tools/weather?latitude=51.5074&longitude=-0.1278&forecastDays=3&timezone=Europe/London"

# Test health endpoint
curl "http://localhost:8080/api/tools/health"
```

---

## Error Handling

All endpoints handle errors gracefully:

**Example Error Response:**
```http
HTTP/1.1 500 Internal Server Error

{
  "error": "Failed to fetch weather data",
  "timestamp": "2026-01-30T16:30:00Z"
}
```

---

## Performance Notes

- All operations are **async** using `CompletableFuture`
- Requests are non-blocking and leverage Spring WebFlux
- API calls to Open-Meteo include gzip compression (enabled by default)
- Caching is not implemented (each request fetches fresh data)

---

## Integration with MCP

The `McpToolsHandler` component is available for MCP protocol integration:

```java
@Autowired
private McpToolsHandler toolsHandler;

// Call any of the 4 tools
CompletableFuture<GeocodingResponse> results =
    toolsHandler.searchLocation("London", 10, "en", "");
```

---

## Testing

Run tests with:
```bash
mvn test
```

Current test coverage: **279 tests** passing

Test files:
- `src/test/java/com/openmeteo/mcp/service/` - Service layer tests
- `src/test/java/com/openmeteo/mcp/service/util/` - Utility function tests
- `src/test/java/com/openmeteo/mcp/` - Integration and model tests
