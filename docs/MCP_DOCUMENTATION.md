# MCP Protocol Documentation - Open-Meteo MCP Java

## Overview

The Model Context Protocol (MCP) implementation provides AI assistants and
development tools with access to weather, air quality, and location services
through a standardized protocol.

MCP enables AI models to:

- **Call Tools**: Execute weather and location functions
- **Access Resources**: Reference weather codes, parameters, and location data
- **Use Prompts**: Follow structured workflows for weather-aware planning

> 📋 **Architecture**: For complete system architecture including the three API
> endpoints (REST, MCP, Chat), see [ARCHITECTURE.md](../ARCHITECTURE.md)

## Protocol Details

- **Protocol**: Model Context Protocol (MCP) v1.0
- **Transport**: Server-Sent Events (SSE) over HTTP
- **Encoding**: JSON messages
- **Authentication**: None (public weather data)

## MCP Tools

### 🔧 Available Tools (4 total)

#### 1. `meteo__search_location`

**Purpose**: Search for geographic locations by name (geocoding)

**Parameters**:

- `name` (string, required): Location name to search for
- `count` (integer, optional): Maximum results (default: 10, max: 100)
- `language` (string, optional): Language code (default: "en")
- `country` (string, optional): ISO country code filter (e.g., "CH")

**Returns**: `GeocodingResponse` with location results

**Example Usage**:

```json
{
  "name": "meteo__search_location",
  "arguments": {
    "name": "Zermatt",
    "count": 5,
    "country": "CH"
  }
}
```

**Response Structure**:

```json
{
  "results": [
    {
      "id": 2657896,
      "name": "Zermatt",
      "latitude": 46.0207,
      "longitude": 7.7491,
      "elevation": 1608,
      "country": "Switzerland",
      "country_code": "CH",
      "timezone": "Europe/Zurich",
      "population": 5800
    }
  ]
}
```

---

#### 2. `meteo__get_weather`

**Purpose**: Get weather forecast with current conditions and multi-day outlook

**Parameters**:

- `latitude` (double, required): Latitude in decimal degrees (-90 to 90)
- `longitude` (double, required): Longitude in decimal degrees (-180 to 180)
- `forecastDays` (integer, optional): Number of forecast days (1-16, default: 7)
- `timezone` (string, optional): Timezone identifier (default: "UTC")

**Returns**: Enhanced weather data with interpretations

**Example Usage**:

```json
{
  "name": "meteo__get_weather",
  "arguments": {
    "latitude": 47.3769,
    "longitude": 8.5417,
    "forecastDays": 7,
    "timezone": "Europe/Zurich"
  }
}
```

**Response Structure**:

```json
{
  "forecast": {
    "latitude": 47.3769,
    "longitude": 8.5417,
    "elevation": 408,
    "timezone": "Europe/Zurich",
    "current": {
      "time": "2026-02-02T14:00:00Z",
      "temperature_2m": 8.5,
      "weather_code": 1,
      "wind_speed_10m": 12.3,
      "relative_humidity_2m": 65
    },
    "daily": {
      "time": ["2026-02-02", "2026-02-03", "..."],
      "temperature_2m_max": [12.1, 9.8, "..."],
      "temperature_2m_min": [3.2, 1.5, "..."],
      "weather_code": [1, 3, "..."],
      "precipitation_sum": [0.0, 2.1, "..."]
    }
  },
  "current_interpretation": {
    "condition": "Mainly clear",
    "comfort_level": "comfortable",
    "travel_impact": "none"
  }
}
```

---

#### 3. `meteo__get_snow_conditions`

**Purpose**: Get snow depth, snowfall, and mountain weather conditions

**Parameters**:

- `latitude` (double, required): Latitude (preferably mountains >1000m)
- `longitude` (double, required): Longitude in decimal degrees
- `forecastDays` (integer, optional): Number of forecast days (1-16, default: 7)
- `timezone` (string, optional): Timezone identifier (default: "UTC")

**Returns**: Snow conditions with ski assessment

**Example Usage**:

```json
{
  "name": "meteo__get_snow_conditions",
  "arguments": {
    "latitude": 46.0207,
    "longitude": 7.7491,
    "forecastDays": 7,
    "timezone": "Europe/Zurich"
  }
}
```

**Response Structure**:

```json
{
  "forecast": {
    "latitude": 46.0207,
    "longitude": 7.7491,
    "elevation": 1608,
    "daily": {
      "time": ["2026-02-02", "2026-02-03", "..."],
      "snow_depth": [1.2, 1.3, "..."],
      "snowfall_sum": [5.0, 12.0, "..."],
      "temperature_2m_max": [-2.1, -5.8, "..."],
      "temperature_2m_min": [-8.2, -12.5, "..."]
    }
  },
  "snow_analysis": {
    "ski_conditions": "excellent",
    "snow_depth_trend": "increasing",
    "recent_snowfall": "Fresh powder: 12cm in last 24h"
  }
}
```

---

#### 4. `meteo__get_air_quality`

**Purpose**: Get air quality index, pollutants, UV index, and pollen data

**Parameters**:

- `latitude` (double, required): Latitude in decimal degrees
- `longitude` (double, required): Longitude in decimal degrees
- `forecastDays` (integer, optional): Number of forecast days (1-5, default: 3)
- `includePollen` (boolean, optional): Include pollen data - Europe only
  (default: true)
- `timezone` (string, optional): Timezone identifier (default: "UTC")

**Returns**: Air quality data with health interpretations

**Example Usage**:

```json
{
  "name": "meteo__get_air_quality",
  "arguments": {
    "latitude": 52.52,
    "longitude": 13.405,
    "forecastDays": 3,
    "includePollen": true,
    "timezone": "Europe/Berlin"
  }
}
```

**Response Structure**:

```json
{
  "forecast": {
    "latitude": 52.52,
    "longitude": 13.405,
    "current": {
      "time": "2026-02-02T14:00:00Z",
      "european_aqi": 23,
      "us_aqi": 45,
      "pm10": 18.5,
      "pm2_5": 12.3,
      "ozone": 89.2,
      "uv_index": 2.1
    }
  },
  "interpretation": {
    "european_aqi_level": "Fair",
    "us_aqi_level": "Good",
    "uv_level": "Low",
    "health_recommendations": [
      "Air quality is acceptable for most people",
      "Sensitive individuals should limit prolonged outdoor activity",
      "Minimal UV protection needed"
    ]
  }
}
```

---

## MCP Resources

### 📚 Available Resources (4 total)

Resources provide reference data that AI assistants can use to interpret tool
responses and provide better guidance.

#### 1. `weather://codes`

**Purpose**: WMO weather code interpretations and travel impact

**Content**: JSON mapping of weather codes (0-99) to descriptions, categories,
and travel impact assessments

**Use Cases**:

- Interpreting weather_code values from forecast responses
- Explaining weather conditions to users
- Assessing travel safety based on weather codes

**Sample Content**:

```json
{
  "0": {
    "description": "Clear sky",
    "category": "clear",
    "icon": "☀️",
    "travel_impact": "none"
  },
  "51": {
    "description": "Light drizzle",
    "category": "rainy",
    "icon": "🌦️",
    "travel_impact": "minimal"
  },
  "95": {
    "description": "Thunderstorm",
    "category": "stormy",
    "icon": "⛈️",
    "travel_impact": "severe"
  }
}
```

---

#### 2. `weather://parameters`

**Purpose**: Complete reference of available weather parameters

**Content**: Documentation of all weather and snow parameters including units,
descriptions, and data types

**Use Cases**:

- Understanding parameter meanings in API responses
- Checking measurement units (°C, mm, km/h, %)
- Learning about specialized parameters (snow depth, apparent temperature)

**Sample Content**:

```json
{
  "hourly": {
    "temperature_2m": {
      "unit": "°C",
      "description": "Air temperature at 2 meters above ground",
      "type": "float"
    },
    "precipitation": {
      "unit": "mm",
      "description": "Total precipitation (rain, showers, snow)",
      "type": "float"
    }
  },
  "daily": {
    "temperature_2m_max": {
      "unit": "°C",
      "description": "Maximum daily temperature",
      "type": "float"
    }
  }
}
```

---

#### 3. `weather://aqi-reference`

**Purpose**: Air Quality Index scales and health impact guidance

**Content**: European and US AQI scales, UV index levels, and pollen information
with health recommendations

**Use Cases**:

- Interpreting AQI values from air quality responses
- Providing health guidance for sensitive groups
- Understanding UV exposure risks
- Assessing pollen allergy risks

**Sample Content**:

```json
{
  "european_aqi": {
    "0-20": {
      "level": "Good",
      "description": "Air quality is satisfactory",
      "health_impact": "No health concerns"
    },
    "20-40": {
      "level": "Fair",
      "description": "Air quality is acceptable for most people",
      "health_impact": "Unusually sensitive individuals may experience minor symptoms"
    }
  },
  "uv_index": {
    "0-2": {
      "level": "Low",
      "recommendations": ["Minimal sun protection required"]
    },
    "3-5": {
      "level": "Moderate",
      "recommendations": ["Seek shade during midday hours", "Wear sunscreen"]
    }
  }
}
```

---

#### 4. `weather://swiss-locations`

**Purpose**: Pre-defined Swiss locations with coordinates

**Content**: Database of major Swiss cities, mountains, ski resorts, and
landmarks with precise coordinates and elevation data

**Use Cases**:

- Quick coordinate lookup for Swiss locations
- Identifying ski resorts and mountain peaks
- Planning trips with elevation considerations
- Reference for major Swiss geographical features

**Sample Content**:

```json
{
  "cities": {
    "zurich": {
      "name": "Zurich",
      "latitude": 47.3769,
      "longitude": 8.5417,
      "elevation": 408,
      "region": "Zurich",
      "type": "city"
    }
  },
  "mountains": {
    "matterhorn": {
      "name": "Matterhorn",
      "latitude": 45.9763,
      "longitude": 7.6586,
      "elevation": 4478,
      "region": "Valais",
      "type": "mountain"
    }
  },
  "ski_resorts": {
    "zermatt": {
      "name": "Zermatt",
      "latitude": 46.0207,
      "longitude": 7.7491,
      "elevation": 1608,
      "region": "Valais",
      "type": "resort"
    }
  }
}
```

---

## MCP Prompts

### 🎯 Available Prompts (3 total)

Prompts provide structured workflows that guide AI assistants through complex
multi-step weather-related planning tasks.

#### 1. `meteo__ski-trip-weather`

**Purpose**: Comprehensive ski trip planning workflow

**Parameters**:

- `resort` (string, optional): Ski resort name
- `dates` (string, optional): Travel dates or timeframe

**Workflow Steps**:

1. **Location Identification**: Use `search_location` to find resort coordinates
2. **Snow Assessment**: Use `get_snow_conditions` for depth and snowfall
   analysis
3. **Weather Check**: Use `get_weather` for temperature and general conditions
4. **Safety Evaluation**: Assess wind, visibility, and storm risks
5. **Recommendations**: Provide ski condition rating and gear suggestions

**Sample Generated Prompt**:

```markdown
# Ski Trip Weather Planning for Zermatt on this weekend

## Step 1: Identify Resort Location

- Resort: Zermatt (coordinates: 46.0207, 7.7491, elevation: 1608m)

## Step 2: Check Snow Conditions

Use get_snow_conditions tool:

- Current snow depth (ideal: >50cm)
- Recent snowfall (fresh powder: >10cm in last 24h)
- Snow depth trend (increasing/stable/decreasing)

## Step 3: Weather Assessment

Use get_weather tool:

- Temperature range (ideal: -10°C to -5°C)
- Wind conditions (safe: <30 km/h)
- Precipitation forecast
- Weather codes for storm risk

## Step 4: Overall Assessment

Rate ski conditions as Excellent/Good/Fair/Poor based on:

- Snow depth and quality
- Weather safety
- Recent snowfall
```

---

#### 2. `meteo__plan-outdoor-activity`

**Purpose**: Weather-aware outdoor activity planning with safety assessment

**Parameters**:

- `activity` (string, optional): Activity type (hiking, cycling, climbing, etc.)
- `location` (string, optional): Location for the activity
- `timeframe` (string, optional): When to do the activity

**Activity Sensitivity Levels**:

- **High**: Rock climbing, via ferrata, high-altitude hiking (>2500m)
- **Medium**: Day hiking, road cycling, trail running, camping
- **Low**: Walking, photography, sightseeing

**Workflow Steps**:

1. **Activity Analysis**: Assess weather sensitivity and safety requirements
2. **Location Setup**: Use `search_location` if coordinates needed
3. **Weather Forecast**: Use `get_weather` for comprehensive conditions
4. **Air Quality Check**: Use `get_air_quality` for health considerations
5. **Suitability Assessment**: Rate as Ideal/Acceptable/Poor
6. **Recommendations**: Provide timing, gear, and safety guidance

---

#### 3. `meteo__weather-aware-travel`

**Purpose**: Multi-destination travel planning with weather integration

**Parameters**:

- `destination` (string, optional): Travel destination
- `travelDates` (string, optional): Travel dates or duration
- `tripType` (string, optional): Type of trip (business, leisure, adventure)

**Workflow Steps**:

1. **Destination Research**: Use `search_location` for coordinates and timezone
2. **Weather Analysis**: Use `get_weather` for travel period forecast
3. **Seasonal Context**: Analyze typical weather patterns for the region
4. **Activity Planning**: Match weather to suitable activities
5. **Packing Recommendations**: Weather-appropriate clothing and gear
6. **Alternative Planning**: Backup options for poor weather days

**Sample Generated Workflow**:

```markdown
# Travel Weather Planning for Switzerland March 15-20

## Step 1: Destination Analysis

- Use search_location for major Swiss cities
- Consider elevation differences (Zurich 408m vs. Zermatt 1608m)

## Step 2: Weather Forecast Analysis

- Check 5-day weather for each destination
- Compare temperature ranges and precipitation
- Assess travel safety conditions

## Step 3: Seasonal Considerations

- March = Late winter/early spring in Switzerland
- Mountain weather more variable than lowlands
- Snow possible at elevation >1000m

## Step 4: Activity Recommendations

Based on weather conditions:

- Sunny days: Outdoor sightseeing, hiking
- Rainy days: Museums, indoor attractions
- Snowy days: Ski resorts, winter activities
```

---

## MCP Client Integration

### Connection Setup

To connect to the MCP server:

1. **Server URL**: `http://localhost:8080` (development) or your deployed URL
2. **Protocol**: MCP over Server-Sent Events (SSE)
3. **Capabilities**: Tools, Resources, Prompts
4. **Authentication**: None required (public weather data)

### Example Client Code

```javascript
import { McpClient } from '@modelcontextprotocol/client';

const client = new McpClient({
  serverUrl: 'http://localhost:8080',
  capabilities: ['tools', 'resources', 'prompts'],
});

// List available tools
const tools = await client.listTools();
console.log('Available tools:', tools);

// Call weather tool
const weather = await client.callTool('meteo__get_weather', {
  latitude: 47.3769,
  longitude: 8.5417,
  forecastDays: 5,
});

// Access weather codes resource
const weatherCodes = await client.readResource('weather://codes');
```

### Claude Desktop Integration

Add to your Claude Desktop MCP configuration:

```json
{
  "mcpServers": {
    "open-meteo": {
      "command": "docker",
      "args": ["run", "-p", "8080:8080", "open-meteo-mcp:2.0.0"],
      "env": {
        "AZURE_OPENAI_KEY": "your-key-here"
      }
    }
  }
}
```

### Error Handling

The MCP server returns standard MCP error responses:

```json
{
  "error": {
    "code": -1,
    "message": "Invalid coordinates",
    "data": {
      "details": "Latitude must be between -90 and 90",
      "parameter": "latitude",
      "value": 95.0
    }
  }
}
```

Common error codes:

- `-1`: Invalid parameters or validation error
- `-2`: External API error (Open-Meteo service unavailable)
- `-3`: Rate limit exceeded
- `-4`: Tool not found
- `-5`: Resource not found

---

## Best Practices

### Tool Usage

1. **Use search_location first** when working with place names
2. **Combine weather and air quality** for health-sensitive planning
3. **Check snow conditions** for mountain activities above 1000m elevation
4. **Reference weather codes** resource to interpret forecast data
5. **Use appropriate forecast days** (weather: 1-16, snow: 1-16, air quality:
   1-5)

### Resource References

Always reference relevant resources in responses:

- `weather://codes` for weather condition interpretation
- `weather://aqi-reference` for health guidance
- `weather://parameters` for understanding measurement units
- `weather://swiss-locations` for Swiss geography

### Prompt Integration

Use prompts for complex workflows:

- Multi-step planning (ski trips, travel, activities)
- Safety-critical assessments (mountain weather, air quality)
- Educational scenarios (teaching weather interpretation)

---

## Support and Documentation

- **GitHub**:
  [schlpbch/open-meteo-mcp-java](https://github.com/schlpbch/open-meteo-mcp-java)
- **Architecture**: See [ARCHITECTURE.md](../ARCHITECTURE.md) for complete
  system design with three API endpoints
- **API Reference**: See `/docs/API_REFERENCE.md` for detailed schemas
- **OpenAPI Specs**: See `/docs/openapi-tools.yaml` and
  `/docs/openapi-chat.yaml`
- **Data Source**: [Open-Meteo API](https://open-meteo.com/en/docs)
