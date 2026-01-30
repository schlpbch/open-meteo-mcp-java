# Open Meteo MCP Java - API Reference

## Overview

This document provides complete API documentation for the Open Meteo MCP Java server. The server exposes weather, snow conditions, and air quality data through Model Context Protocol (MCP) tools, resources, and prompts.

## MCP Tools

### 1. search_location

**Purpose**: Geocoding - search for locations by name

**Input Parameters**:
- `name` (String, required): Location name to search for
- `count` (Integer, optional): Maximum number of results (1-100, default: 10)
- `language` (String, optional): Language code for results (default: "en")
- `country` (String, optional): ISO 3166-1 alpha-2 country code filter

**Returns**: `LocationSearchResponse` with list of `GeocodingResult` objects

**Example**:
```json
{
  "name": "Zurich",
  "count": 5,
  "language": "en",
  "country": "CH"
}
```

**Response**:
```json
{
  "results": [
    {
      "id": 2657896,
      "name": "Zurich",
      "latitude": 47.3769,
      "longitude": 8.5417,
      "elevation": 408.0,
      "feature_code": "PPLC",
      "country_code": "CH",
      "country": "Switzerland",
      "admin1": "Zurich",
      "timezone": "Europe/Zurich",
      "population": 390475,
      "postcodes": ["8000", "8001", "8002"]
    }
  ]
}
```

### 2. get_weather

**Purpose**: Get weather forecast with temperature, precipitation, wind, UV index

**Input Parameters**:
- `latitude` (Double, required): Latitude in decimal degrees (-90 to 90)
- `longitude` (Double, required): Longitude in decimal degrees (-180 to 180)
- `forecast_days` (Integer, optional): Number of forecast days (1-16, default: 7)
- `include_hourly` (Boolean, optional): Include hourly forecast data (default: true)
- `timezone` (String, optional): Timezone identifier (default: "auto")

**Returns**: `WeatherResponse` with forecast data

**Example**:
```json
{
  "latitude": 47.3769,
  "longitude": 8.5417,
  "forecast_days": 7,
  "include_hourly": true,
  "timezone": "Europe/Zurich"
}
```

**Response**:
```json
{
  "latitude": 47.3769,
  "longitude": 8.5417,
  "timezone": "Europe/Zurich",
  "utc_offset_seconds": 3600,
  "current_weather": {
    "temperature": 12.5,
    "windspeed": 8.3,
    "winddirection": 185,
    "weathercode": 80,
    "time": "2024-01-30T15:00Z"
  },
  "daily": {
    "time": ["2024-01-30", "2024-01-31"],
    "temperature_2m_max": [15.2, 14.8],
    "temperature_2m_min": [5.3, 4.9],
    "precipitation_sum": [0.0, 2.5],
    "weathercode": [80, 61]
  },
  "hourly": {
    "time": ["2024-01-30T15:00Z", "2024-01-30T16:00Z"],
    "temperature_2m": [12.5, 11.8],
    "precipitation": [0.0, 0.2],
    "weathercode": [80, 61]
  }
}
```

### 3. get_snow_conditions

**Purpose**: Get snow depth, snowfall, and mountain weather

**Input Parameters**:
- `latitude` (Double, required): Latitude in decimal degrees (-90 to 90)
- `longitude` (Double, required): Longitude in decimal degrees (-180 to 180)
- `forecast_days` (Integer, optional): Number of forecast days (1-16, default: 7)
- `include_hourly` (Boolean, optional): Include hourly snow data (default: true)
- `timezone` (String, optional): Timezone identifier (default: "auto")

**Returns**: `SnowConditions` with snow forecast data

**Example**:
```json
{
  "latitude": 46.5472,
  "longitude": 7.9858,
  "forecast_days": 7,
  "include_hourly": true,
  "timezone": "Europe/Zurich"
}
```

**Response**:
```json
{
  "latitude": 46.5472,
  "longitude": 7.9858,
  "timezone": "Europe/Zurich",
  "daily": {
    "time": ["2024-01-30", "2024-01-31"],
    "snowfall_sum": [2.5, 5.0],
    "snow_depth": [50.0, 55.0],
    "temperature_2m_max": [-5.0, -6.0],
    "temperature_2m_min": [-10.0, -12.0]
  }
}
```

### 4. get_air_quality

**Purpose**: Get AQI, pollutants, UV index, and pollen data

**Input Parameters**:
- `latitude` (Double, required): Latitude in decimal degrees (-90 to 90)
- `longitude` (Double, required): Longitude in decimal degrees (-180 to 180)
- `forecast_days` (Integer, optional): Number of forecast days (1-5, default: 3)
- `include_pollen` (Boolean, optional): Include pollen data - Europe only (default: true)
- `timezone` (String, optional): Timezone identifier (default: "auto")

**Returns**: `AirQualityResponse` with air quality data

**Example**:
```json
{
  "latitude": 47.3769,
  "longitude": 8.5417,
  "forecast_days": 3,
  "include_pollen": true,
  "timezone": "Europe/Zurich"
}
```

**Response**:
```json
{
  "latitude": 47.3769,
  "longitude": 8.5417,
  "timezone": "Europe/Zurich",
  "daily": {
    "time": ["2024-01-30", "2024-01-31"],
    "european_aqi": [25, 35],
    "us_aqi": [30, 40],
    "pm10": [15.5, 25.3],
    "pm2_5": [8.2, 12.5],
    "o3": [65.0, 75.0],
    "no2": [12.5, 18.3],
    "so2": [2.5, 3.5],
    "uv_index_max": [1.0, 1.5]
  }
}
```

## MCP Resources

### weather://codes

WMO weather code reference with interpretations

```json
{
  "0": "Clear sky",
  "1": "Mainly clear",
  "61": "Slight rain",
  "80": "Slight rain showers",
  "85": "Heavy snow showers"
}
```

### weather://parameters

Available weather parameters for API queries

Lists all supported hourly and daily parameters for weather forecasts.

### weather://aqi-reference

AQI scales and health recommendations

Provides European AQI and US AQI scales with health guidance:
- Good (0-50): No health impact
- Moderate (51-100): Sensitive individuals should limit outdoor activity
- Poor (101+): Public health warning

### weather://swiss-locations

Swiss cities, mountains, and geographic landmarks with coordinates

Pre-defined locations for quick access:
- Major cities (Zurich, Geneva, Bern, etc.)
- Mountain peaks (Jungfraujoch, Säntis, etc.)
- Geographic points of interest

## MCP Prompts

### ski-trip-weather

Guide for checking snow conditions and weather for ski trips

**Arguments**:
- `resort`: Ski resort name (e.g., "Verbier", "Zermatt")
- `dates`: Date range (e.g., "2024-02-10 to 2024-02-12")

**Workflow**:
1. Search for resort location
2. Get snow conditions
3. Check weather forecast
4. Assess skiing conditions

### plan-outdoor-activity

Weather-aware activity planning

**Arguments**:
- `activity`: Type of activity (e.g., "hiking", "cycling", "camping")
- `location`: Location name
- `timeframe`: Time period (e.g., "this weekend", "next week")

**Workflow**:
1. Search location
2. Get weather forecast
3. Assess suitability
4. Recommend optimal timing

### weather-aware-travel

Travel planning with weather integration

**Arguments**:
- `destination`: Target destination
- `travel_dates`: Travel date range
- `trip_type`: Type of trip (e.g., "hiking", "sightseeing", "skiing")

**Workflow**:
1. Get destination weather
2. Check road conditions
3. Assess travel risks
4. Provide recommendations

## Error Handling

All tools return appropriate error responses for invalid inputs:

**Common Error Codes**:
- 400: Invalid input (latitude/longitude out of range, invalid count)
- 404: Location not found
- 429: Rate limited
- 500: Internal server error

**Error Response Format**:
```json
{
  "error": "error_code",
  "message": "Human-readable error message",
  "details": {}
}
```

## Performance Notes

- **Caching**: Responses are cached for 30 minutes by default
- **Compression**: All responses use gzip compression
- **Timeout**: Requests timeout after 10 seconds
- **Rate Limiting**: ~10,000 requests per day from Open-Meteo API

## Data Units

- **Temperature**: Celsius (°C)
- **Wind Speed**: km/h
- **Precipitation**: mm
- **Snow Depth**: meters
- **Pressure**: hPa
- **Visibility**: meters
- **Humidity**: %
- **UV Index**: 0-20 scale
- **Pollen**: grains/m³ (Europe only)

## Integration Examples

### With Spring Boot Application

```java
@Service
public class WeatherService {
    private final OpenMeteoClient client;

    public WeatherResponse getWeather(double lat, double lon) {
        WeatherRequest request = WeatherRequest.withDefaults(lat, lon);
        return client.getWeather(request).join();
    }
}
```

### With CompletableFuture

```java
// Non-blocking async call
client.getWeather(request)
    .thenAccept(response -> System.out.println(response))
    .exceptionally(error -> {
        logger.error("Failed to fetch weather", error);
        return null;
    });
```

## See Also

- [CLAUDE.md](../CLAUDE.md) - Development guide
- [CONSTITUTION.md](../spec/CONSTITUTION.md) - Project governance
- [Open-Meteo API Docs](https://open-meteo.com/en/docs)
- [MCP Protocol Spec](https://modelcontextprotocol.io/)
