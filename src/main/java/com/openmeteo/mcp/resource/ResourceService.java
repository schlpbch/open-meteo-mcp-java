package com.openmeteo.mcp.resource;

import com.openmeteo.mcp.resource.util.ResourceLoader;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for providing MCP resources.
 * <p>
 * Serves JSON data files as MCP resources for reference data lookup.
 * Resources include weather codes, parameters, AQI scales, and location data.
 * </p>
 */
@Service
public class ResourceService {

    private final ResourceLoader resourceLoader;

    public ResourceService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Get weather codes resource.
     *
     * WMO (World Meteorological Organization) weather code reference.
     *
     * Contains WMO weather code reference with descriptions, categories,
     * icons, and travel impact assessments.
     *
     * USE THIS RESOURCE WHEN:
     * - Interpreting weather codes from get_weather tool responses
     * - Explaining weather conditions to users (e.g., code 71 = "Light snow")
     * - Providing travel impact guidance based on weather
     * - Checking weather categorization (clear, cloudy, rainy, snowy, stormy)
     *
     * CONTAINS:
     * - 0-99: WMO weather codes with descriptions
     * - Each code with category, icon, and travel impact assessment
     * - Examples: 0=Clear sky, 1=Mainly clear, 3=Overcast, 51=Light drizzle, 80=Moderate rain showers
     *
     * @return JSON string containing weather codes data
     */
    @McpResource(uri = "weather://codes", description = """
            WMO (World Meteorological Organization) weather code reference.

            Complete reference of WMO codes with descriptions, categories, icons, and travel impact assessments.

            USE THIS RESOURCE WHEN:
            - Interpreting weather codes from get_weather tool responses
            - Explaining weather conditions to users (e.g., code 71 = "Light snow")
            - Providing travel impact guidance based on weather
            - Checking weather categorization (clear, cloudy, rainy, snowy, stormy)

            CONTAINS:
            - Codes 0-99: WMO weather codes with descriptions
            - Each code with category, icon, and travel impact assessment
            - Examples:
              - 0: Clear sky
              - 1: Mainly clear
              - 3: Overcast
              - 51: Light drizzle
              - 80: Moderate rain showers
              - 85: Heavy snow showers
              - 95: Thunderstorm with hail

            CATEGORIES: Clear, Cloudy, Rainy, Snowy, Stormy
            """)
    public String getWeatherCodes() {
        return resourceLoader.loadResource("data/weather-codes.json");
    }

    /**
     * Get weather parameters resource.
     *
     * Complete reference of available weather and snow parameters from Open-Meteo API.
     *
     * Documents available weather and snow parameters from Open-Meteo API
     * including hourly and daily parameters with units and descriptions.
     *
     * USE THIS RESOURCE WHEN:
     * - Understanding what parameters are available in weather responses
     * - Checking units and measurement types (°C, mm, km/h, %)
     * - Learning about snow-specific parameters (snow depth, snowfall, snow water equivalent)
     * - Interpreting complex metrics (apparent temperature, dew point, cloud cover)
     *
     * INCLUDES:
     * - Hourly parameters: temperature, precipitation, wind, humidity, pressure, etc.
     * - Daily parameters: min/max temperature, precipitation sum, weather codes, etc.
     * - Snow parameters: snow depth, snowfall, snow water equivalent
     * - Each parameter with unit, description, and data type
     *
     * @return JSON string containing weather parameters data
     */
    @McpResource(uri = "weather://parameters", description = """
            Complete reference of available weather and snow parameters from Open-Meteo API.

            Comprehensive documentation of weather and snow parameters including hourly and daily measurements with units and descriptions.

            USE THIS RESOURCE WHEN:
            - Understanding what parameters are available in weather responses
            - Checking units and measurement types (°C, mm, km/h, %)
            - Learning about snow-specific parameters (snow depth, snowfall, snow water equivalent)
            - Interpreting complex metrics (apparent temperature, dew point, cloud cover)
            - Understanding relative humidity, pressure, and visibility measurements

            PARAMETER CATEGORIES:

            TEMPERATURE:
            - temperature (°C)
            - apparent_temperature (°C)
            - dew_point (°C)
            - temperature_max (°C)
            - temperature_min (°C)

            PRECIPITATION:
            - precipitation (mm)
            - precipitation_probability (%)
            - rain (mm)
            - snowfall (cm)
            - snow_depth (cm)

            WIND & PRESSURE:
            - wind_speed (km/h)
            - wind_direction (°)
            - wind_gusts (km/h)
            - pressure (hPa)

            OTHER:
            - humidity (%)
            - cloud_cover (%)
            - visibility (m)
            - uv_index
            - sunshine_duration (s)

            SNOW PARAMETERS (Mountain specific):
            - snow_depth (m)
            - snowfall (cm)
            - snow_water_equivalent (mm)
            """)
    public String getWeatherParameters() {
        return resourceLoader.loadResource("data/weather-parameters.json");
    }

    /**
     * Get AQI reference resource.
     *
     * Air Quality Index (AQI) scales and health impact guidance.
     *
     * Contains European and US AQI scales with health implications,
     * UV index guidance, and pollen level information.
     *
     * USE THIS RESOURCE WHEN:
     * - Interpreting AQI values from get_air_quality tool responses
     * - Providing health guidance based on air quality (sensitive groups warnings, etc.)
     * - Explaining UV index levels and sun exposure risks
     * - Interpreting pollen counts and allergy risk levels
     * - Planning outdoor activities for people with respiratory conditions
     *
     * CONTAINS:
     * - European AQI (0-100+): Good, Fair, Moderate, Poor, Very Poor, Extremely Poor
     * - US AQI (0-500): Good, Moderate, Unhealthy for Sensitive, Unhealthy, Very Unhealthy, Hazardous
     * - UV Index levels (0-11+): Low, Moderate, High, Very High, Extreme
     * - Pollen levels and health recommendations
     *
     * @return JSON string containing AQI reference data
     */
    @McpResource(uri = "weather://aqi-reference", description = """
            Air Quality Index (AQI) scales and health impact guidance.

            Complete reference of AQI scales with health implications, UV index guidance, and pollen level information.

            USE THIS RESOURCE WHEN:
            - Interpreting AQI values from get_air_quality tool responses
            - Providing health guidance based on air quality (sensitive groups warnings, etc.)
            - Explaining UV index levels and sun exposure risks
            - Interpreting pollen counts and allergy risk levels
            - Planning outdoor activities for people with respiratory conditions

            EUROPEAN AQI SCALE (0-100+):
            - 0-20: Good - Air pollution poses little or no risk
            - 20-40: Fair - Acceptable air quality; some pollutants may be of moderate concern
            - 40-60: Moderate - Members of general public may begin to experience health effects
            - 60-80: Poor - General public likely to be affected; members of vulnerable groups more serious
            - 80-100: Very Poor - Health alert; entire population may begin to experience health effects
            - 100+: Extremely Poor - Health warning; entire population is more likely to be affected

            US AQI SCALE (0-500):
            - 0-50: Good - Air quality is satisfactory
            - 51-100: Moderate - Acceptable; some pollutants may be a concern
            - 101-150: Unhealthy for Sensitive Groups - Sensitive groups may experience effects
            - 151-200: Unhealthy - General public may begin to experience effects
            - 201-300: Very Unhealthy - Health alert; general public is likely to be affected
            - 301-500: Hazardous - Health warning of emergency conditions

            UV INDEX LEVELS (0-11+):
            - 0-2: Low - No protection required
            - 3-5: Moderate - Wear sunscreen; seek shade 10am-4pm
            - 6-7: High - Seek shade 10am-4pm; wear protective clothing and sunscreen
            - 8-10: Very High - Minimize sun exposure 10am-4pm
            - 11+: Extreme - Take full precautions; avoid sun exposure

            POLLEN LEVELS & HEALTH RECOMMENDATIONS:
            - Low: Minimal symptoms expected
            - Moderate: Some people with pollen sensitivity may experience symptoms
            - High: Many people with pollen sensitivity likely to experience symptoms
            - Very High: Most people with pollen sensitivity will experience symptoms
            """)
    public String getAqiReference() {
        return resourceLoader.loadResource("data/aqi-reference.json");
    }

    /**
     * Get Swiss locations resource.
     *
     * Reference database of major Swiss locations with geographical coordinates.
     *
     * Contains major Swiss cities, mountains, passes, and lakes
     * with GPS coordinates and elevation data.
     *
     * USE THIS RESOURCE WHEN:
     * - Looking up coordinates for Swiss locations without using search_location
     * - Identifying ski resorts and mountain peaks in Switzerland
     * - Planning trips to specific Swiss regions
     * - Comparing elevations for weather impact analysis
     * - Quick reference for major Swiss towns, mountains, and landmarks
     *
     * LOCATION CATEGORIES:
     * - Major cities: Zurich, Bern, Basel, Geneva, Lausanne, Lucerne, etc.
     * - Mountains & Peaks: Matterhorn, Eiger, Jungfrau, Monte Rosa, etc.
     * - Ski Resorts: Zermatt, Verbier, St. Moritz, Davos, Saas-Fee, etc.
     * - Mountain Passes: Gotthard, Simplon, Furka, etc.
     * - Lakes: Lake Geneva, Lake Zurich, Lake Lucerne, etc.
     *
     * Each location includes: name, latitude, longitude, elevation (meters)
     *
     * @return JSON string containing Swiss locations data
     */
    @McpResource(uri = "weather://swiss-locations", description = """
            Reference database of major Swiss locations with geographical coordinates.

            Complete database of major Swiss locations with precise geographical data for weather queries.

            USE THIS RESOURCE WHEN:
            - Looking up coordinates for Swiss locations without using search_location
            - Identifying ski resorts and mountain peaks in Switzerland
            - Planning trips to specific Swiss regions
            - Comparing elevations for weather impact analysis
            - Quick reference for major Swiss towns, mountains, and landmarks

            LOCATION CATEGORIES:

            MAJOR CITIES:
            - Zurich, Bern, Basel, Geneva, Lausanne, Lucerne, Schaffhausen, Chur, Sion

            MOUNTAINS & PEAKS:
            - Matterhorn (4,478m)
            - Eiger (3,970m)
            - Jungfrau (4,158m)
            - Monte Rosa (4,634m)
            - Säntis (2,502m)
            - Matterhorn, Breithorn, Castor, Pollux

            SKI RESORTS:
            - Zermatt, Verbier, St. Moritz, Davos, Saas-Fee, Grindelwald, Andermatt, Lenzerheide

            MOUNTAIN PASSES:
            - Gotthard Pass, Simplon Pass, Furka Pass, Grimsel Pass, Spügen Pass

            LAKES:
            - Lake Geneva (Lac Léman)
            - Lake Zurich (Zürichsee)
            - Lake Lucerne (Vierwaldstättersee)
            - Lake Bern (Wohlensee)
            - Lake Constance (Bodensee)

            DATA PER LOCATION:
            - name (String)
            - latitude (Float, decimal degrees)
            - longitude (Float, decimal degrees)
            - elevation (Integer, meters above sea level)
            - region (String, Swiss canton or region)
            - type (String: city, mountain, resort, pass, lake)
            """)
    public String getSwissLocations() {
        return resourceLoader.loadResource("data/swiss-locations.json");
    }

    /**
     * Get all available resources as a map.
     * <p>
     * Useful for MCP protocol registration.
     * </p>
     *
     * @return Map of URI to resource content
     */
    public Map<String, String> getAllResources() {
        return Map.of(
                "weather://codes", getWeatherCodes(),
                "weather://parameters", getWeatherParameters(),
                "weather://aqi-reference", getAqiReference(),
                "weather://swiss-locations", getSwissLocations()
        );
    }

    /**
     * Get resource by URI.
     * <p>
     * Returns null if URI is not recognized.
     * </p>
     *
     * @param uri Resource URI (e.g., "weather://codes")
     * @return Resource content as JSON string, or null if URI not found
     */
    public String getResourceByUri(String uri) {
        return switch (uri) {
            case "weather://codes" -> getWeatherCodes();
            case "weather://parameters" -> getWeatherParameters();
            case "weather://aqi-reference" -> getAqiReference();
            case "weather://swiss-locations" -> getSwissLocations();
            default -> null;
        };
    }
}
