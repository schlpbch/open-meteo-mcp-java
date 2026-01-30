package com.openmeteo.mcp.tool;

import com.openmeteo.mcp.model.dto.GeocodingResponse;
import com.openmeteo.mcp.service.AirQualityService;
import com.openmeteo.mcp.service.LocationService;
import com.openmeteo.mcp.service.SnowConditionsService;
import com.openmeteo.mcp.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * MCP Tools Handler providing weather, snow, air quality, and location tools.
 *
 * Exposes the 4 main Open-Meteo MCP tools for AI assistants:
 * 1. search_location - Geocoding and location search
 * 2. get_weather - Weather forecast with temperature, precipitation
 * 3. get_snow_conditions - Snow depth and ski conditions
 * 4. get_air_quality - Air quality, AQI, pollutants, UV index, pollen
 *
 * All tools return enriched data with interpretations and assessments.
 */
@Component
public class McpToolsHandler {

    private static final Logger log = LoggerFactory.getLogger(McpToolsHandler.class);

    private final LocationService locationService;
    private final WeatherService weatherService;
    private final SnowConditionsService snowConditionsService;
    private final AirQualityService airQualityService;

    public McpToolsHandler(
            LocationService locationService,
            WeatherService weatherService,
            SnowConditionsService snowConditionsService,
            AirQualityService airQualityService
    ) {
        this.locationService = locationService;
        this.weatherService = weatherService;
        this.snowConditionsService = snowConditionsService;
        this.airQualityService = airQualityService;
    }

    /**
     * MCP Tool: search_location
     *
     * Searches for locations by name to get coordinates for weather queries.
     *
     * Convert location names to coordinates using fuzzy search. Essential for
     * natural language weather queries like "weather in Zurich" instead of
     * requiring latitude/longitude coordinates.
     *
     * EXAMPLES:
     * - "Zurich" → Returns Zurich, Switzerland with coordinates
     * - "Bern" → Returns multiple matches (Bern CH, Bern US, etc.)
     * - "Zermatt" → Returns ski resort with elevation data
     * - "Lake Geneva" → Returns lake coordinates
     *
     * FEATURES:
     * - Fuzzy matching (handles typos)
     * - Multi-language support
     * - Country filtering (e.g., country="CH" for Switzerland only)
     * - Returns population, timezone, elevation
     *
     * WORKFLOW:
     * 1. Search for location by name
     * 2. Select result (usually first is best match)
     * 3. Use latitude/longitude for get_weather or get_snow_conditions
     *
     * USE THIS TOOL WHEN:
     * - User provides location name instead of coordinates
     * - Need to find coordinates for a city, mountain, or landmark
     * - Want to discover locations in a specific country
     *
     * @param name     Location name to search for (e.g., "Zurich", "Eiger", "Lake Lucerne")
     * @param count    Number of results to return (1-100, default: 10)
     * @param language Language for results (default: 'en', options: 'de', 'fr', 'it', etc.)
     * @param country  Optional country code filter (e.g., 'CH' for Switzerland, 'DE' for Germany)
     * @return CompletableFuture with GeocodingResponse containing:
     *         - results (list): List of matching locations with name, latitude, longitude,
     *           elevation (meters), country, timezone, population
     */
    @McpTool(description = """
            Searches for locations by name to get coordinates for weather queries.

            Convert location names to coordinates using fuzzy search. Essential for natural language weather queries like "weather in Zurich" instead of requiring latitude/longitude coordinates.

            EXAMPLES:
            - "Zurich" → Returns Zurich, Switzerland with coordinates
            - "Bern" → Returns multiple matches (Bern CH, Bern US, etc.)
            - "Zermatt" → Returns ski resort with elevation data
            - "Lake Geneva" → Returns lake coordinates

            FEATURES:
            - Fuzzy matching (handles typos)
            - Multi-language support (en, de, fr, it, etc.)
            - Country filtering (e.g., country="CH" for Switzerland only)
            - Returns population, timezone, elevation

            WORKFLOW:
            1. Search for location by name
            2. Select result (usually first is best match)
            3. Use latitude/longitude for get_weather or get_snow_conditions

            USE THIS TOOL WHEN:
            - User provides location name instead of coordinates
            - Need to find coordinates for a city, mountain, or landmark
            - Want to discover locations in a specific country

            RETURNS: List of locations with name, latitude, longitude, elevation (meters), country, timezone, population
            """)
    public CompletableFuture<GeocodingResponse> searchLocation(
            String name,
            int count,
            String language,
            String country
    ) {
        log.info("Tool invoked: search_location(name={}, count={}, language={}, country={})",
                name, count, language, country != null && !country.isEmpty() ? country : "none");

        // Default language to English if not provided
        if (language == null || language.isEmpty()) {
            language = "en";
        }

        // Default country to empty string if not provided
        if (country == null) {
            country = "";
        }

        // Default count to 10 if not provided
        if (count <= 0) {
            count = 10;
        }

        return locationService.searchLocation(name, count, language, country)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Tool error: search_location failed", ex);
                    } else {
                        log.info("Tool completed: search_location returned {} results",
                                result.results() != null ? result.results().size() : 0);
                    }
                });
    }

    /**
     * MCP Tool: get_weather
     *
     * Retrieves weather forecast for a location (temperature, rain, sunshine).
     *
     * Get current weather conditions for any location in Switzerland (or worldwide).
     *
     * EXAMPLES:
     * - "What's the weather in Zürich?" → latitude: 47.3769, longitude: 8.5417
     * - "Weather at destination" → Use coordinates from journey endpoint
     * - "Is it raining in Bern?" → Check precipitation field
     *
     * PROVIDES:
     * - Current temperature (°C)
     * - Weather condition (clear, cloudy, rain, snow)
     * - Precipitation amount (mm)
     * - Wind speed (km/h)
     * - Humidity (%)
     * - Hourly and daily forecasts
     *
     * DATA SOURCE: Open-Meteo API (free, no API key required)
     * PERFORMANCE: < 200ms
     *
     * USE THIS TOOL WHEN:
     * - User asks about weather conditions
     * - Planning outdoor activities
     * - Checking if weather affects travel
     * - Combined with journey planning
     *
     * @param latitude      Latitude in decimal degrees (e.g., 46.9479 for Bern)
     * @param longitude     Longitude in decimal degrees (e.g., 7.4474 for Bern)
     * @param forecastDays  Number of forecast days (1-16, default: 7)
     * @param timezone      Timezone for timestamps (e.g., 'Europe/Zurich', default: 'UTC')
     * @return CompletableFuture with weather data containing:
     *         - current (map): Current weather with temperature, weather_code, wind_speed, humidity
     *         - hourly (list | null): Hourly forecasts if available
     *         - daily (list): Daily forecasts with min/max temps, precipitation, weather codes
     *         - location (map): Location metadata with coordinates and timezone
     */
    @McpTool(description = """
            Retrieves weather forecast for a location (temperature, rain, sunshine).

            Get current weather conditions for any location in Switzerland (or worldwide).

            EXAMPLES:
            - "What's the weather in Zürich?" → latitude: 47.3769, longitude: 8.5417
            - "Weather at destination" → Use coordinates from journey endpoint
            - "Is it raining in Bern?" → Check precipitation field

            PROVIDES:
            - Current temperature (°C)
            - Weather condition (clear, cloudy, rain, snow)
            - Precipitation amount (mm)
            - Wind speed (km/h)
            - Humidity (%)
            - Hourly and daily forecasts

            DATA SOURCE: Open-Meteo API (free, no API key required)
            PERFORMANCE: < 200ms

            USE THIS TOOL WHEN:
            - User asks about weather conditions
            - Planning outdoor activities
            - Checking if weather affects travel
            - Combined with journey planning

            RETURNS: Weather data with current conditions, hourly forecasts, daily summaries, and location metadata
            """)
    public CompletableFuture<Map<String, Object>> getWeather(
            double latitude,
            double longitude,
            int forecastDays,
            String timezone
    ) {
        log.info("Tool invoked: get_weather(lat={}, lon={}, days={}, timezone={})",
                latitude, longitude, forecastDays, timezone);

        // Default forecast days to 7 if not provided or invalid
        if (forecastDays <= 0) {
            forecastDays = 7;
        }

        // Default timezone to UTC if not provided
        if (timezone == null || timezone.isEmpty()) {
            timezone = "UTC";
        }

        return weatherService.getWeatherWithInterpretation(
                latitude, longitude, forecastDays, true, timezone
        )
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Tool error: get_weather failed", ex);
                    } else {
                        log.info("Tool completed: get_weather returned forecast for {}, {}",
                                latitude, longitude);
                    }
                });
    }

    /**
     * MCP Tool: get_snow_conditions
     *
     * Retrieves snow conditions and forecasts for mountain locations.
     *
     * PARAMETERS:
     * - latitude (required): Latitude in decimal degrees
     * - longitude (required): Longitude in decimal degrees
     * - forecastDays (optional): Number of forecast days (1-16, default: 7)
     * - timezone (optional): Timezone for timestamps (default: "Europe/Zurich")
     *
     * RETURNS:
     * - Current snow depth (meters)
     * - Recent snowfall (cm)
     * - Forecast snowfall
     * - Temperature trends
     * - Hourly and daily snow data
     *
     * USE THIS TOOL FOR:
     * - Ski trip planning
     * - Checking snow conditions at resorts
     * - Mountain weather forecasts
     * - Avalanche risk assessment (via snow depth trends)
     *
     * @param latitude      Latitude in decimal degrees (e.g., 45.9763 for Zermatt)
     * @param longitude     Longitude in decimal degrees (e.g., 7.6586 for Zermatt)
     * @param forecastDays  Number of forecast days (1-16, default: 7)
     * @param timezone      Timezone for timestamps (default: 'Europe/Zurich')
     * @return CompletableFuture with snow data containing:
     *         - current (map): Current snow depth and recent snowfall
     *         - hourly (list | null): Hourly snow data if available
     *         - daily (list): Daily snow forecasts with accumulation and temperature
     *         - location (map): Mountain location metadata
     */
    @McpTool(description = """
            Retrieves snow conditions and forecasts for mountain locations.

            Essential for ski trip planning and mountain weather assessment.

            PARAMETERS:
            - latitude (required): Latitude in decimal degrees
            - longitude (required): Longitude in decimal degrees
            - forecastDays (optional): Number of forecast days (1-16, default: 7)
            - timezone (optional): Timezone for timestamps (default: "Europe/Zurich")

            RETURNS:
            - Current snow depth (meters)
            - Recent snowfall (cm)
            - Forecast snowfall
            - Temperature trends
            - Hourly and daily snow data

            USE THIS TOOL FOR:
            - Ski trip planning
            - Checking snow conditions at resorts
            - Mountain weather forecasts
            - Avalanche risk assessment (via snow depth trends)

            CONDITION ASSESSMENT:
            - Excellent: Fresh snow (>10cm), -15°C to -5°C, clear skies
            - Good: Good depth (>50cm), stable temps (<0°C), mostly clear
            - Fair: Minimal depth (>20cm), temps below freezing, acceptable visibility
            - Poor: Insufficient snow, warm temps (>5°C), poor weather
            """)
    public CompletableFuture<Map<String, Object>> getSnowConditions(
            double latitude,
            double longitude,
            int forecastDays,
            String timezone
    ) {
        log.info("Tool invoked: get_snow_conditions(lat={}, lon={}, days={}, timezone={})",
                latitude, longitude, forecastDays, timezone);

        // Default forecast days to 7 if not provided or invalid
        if (forecastDays <= 0) {
            forecastDays = 7;
        }

        // Default timezone to UTC if not provided
        if (timezone == null || timezone.isEmpty()) {
            timezone = "UTC";
        }

        return snowConditionsService.getSnowConditionsWithAssessment(
                latitude, longitude, forecastDays, true, timezone
        )
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Tool error: get_snow_conditions failed", ex);
                    } else {
                        log.info("Tool completed: get_snow_conditions returned data for {}, {}",
                                latitude, longitude);
                    }
                });
    }

    /**
     * MCP Tool: get_air_quality
     *
     * Retrieves air quality forecast including AQI, pollutants, UV index, and pollen data.
     *
     * Monitor air quality for health-aware outdoor planning, allergy management,
     * and UV exposure assessment. Provides both European and US Air Quality Indices
     * along with detailed pollutant measurements.
     *
     * EXAMPLES:
     * - "What's the air quality in Zurich?" → AQI, PM2.5, PM10, ozone levels
     * - "Pollen forecast for Bern?" → Grass, birch, alder pollen counts
     * - "UV index for tomorrow?" → UV radiation forecast
     *
     * PROVIDES:
     * - European AQI (0-100+) and US AQI (0-500)
     * - Particulate matter (PM10, PM2.5)
     * - Gases (O3, NO2, SO2, CO, NH3)
     * - UV index (current and clear sky)
     * - Pollen data (Europe only): alder, birch, grass, mugwort, olive, ragweed
     *
     * HEALTH GUIDELINES:
     * - European AQI: 0-20 (Good), 20-40 (Fair), 40-60 (Moderate), 60-80 (Poor), 80-100 (Very Poor), 100+ (Extremely Poor)
     * - US AQI: 0-50 (Good), 51-100 (Moderate), 101-150 (Unhealthy for Sensitive), 151-200 (Unhealthy), 201-300 (Very Unhealthy), 301-500 (Hazardous)
     * - UV Index: 0-2 (Low), 3-5 (Moderate), 6-7 (High), 8-10 (Very High), 11+ (Extreme)
     *
     * USE THIS TOOL WHEN:
     * - Planning outdoor activities for people with asthma/allergies
     * - Assessing air quality for exercise or sports
     * - Checking pollen levels during allergy season
     * - Monitoring UV exposure for sun safety
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-5, default: 5)
     * @param includePollen Include pollen data (default: true, Europe only)
     * @param timezone      Timezone for timestamps (default: 'auto')
     * @return CompletableFuture with air quality data containing:
     *         - current (map): Current AQI, pollutants (PM10, PM2.5, O3, NO2, SO2, CO), UV index
     *         - hourly (list): Hourly air quality forecasts
     *         - pollen (map | null): Pollen data if includePollen=true and location is in Europe
     *         - location (map): Location metadata
     */
    @McpTool(description = """
            Retrieves air quality forecast including AQI, pollutants, UV index, and pollen data.

            Monitor air quality for health-aware outdoor planning, allergy management, and UV exposure assessment. Provides both European and US Air Quality Indices along with detailed pollutant measurements.

            EXAMPLES:
            - "What's the air quality in Zurich?" → AQI, PM2.5, PM10, ozone levels
            - "Pollen forecast for Bern?" → Grass, birch, alder pollen counts
            - "UV index for tomorrow?" → UV radiation forecast

            PROVIDES:
            - European AQI (0-100+) and US AQI (0-500)
            - Particulate matter (PM10, PM2.5)
            - Gases (O3, NO2, SO2, CO, NH3)
            - UV index (current and clear sky)
            - Pollen data (Europe only): alder, birch, grass, mugwort, olive, ragweed

            HEALTH GUIDELINES:
            - European AQI: 0-20 (Good), 20-40 (Fair), 40-60 (Moderate), 60-80 (Poor), 80-100 (Very Poor), 100+ (Extremely Poor)
            - US AQI: 0-50 (Good), 51-100 (Moderate), 101-150 (Unhealthy for Sensitive), 151-200 (Unhealthy), 201-300 (Very Unhealthy), 301-500 (Hazardous)
            - UV Index: 0-2 (Low), 3-5 (Moderate), 6-7 (High), 8-10 (Very High), 11+ (Extreme)

            USE THIS TOOL WHEN:
            - Planning outdoor activities for people with asthma/allergies
            - Assessing air quality for exercise or sports
            - Checking pollen levels during allergy season
            - Monitoring UV exposure for sun safety
            """)
    public CompletableFuture<Map<String, Object>> getAirQuality(
            double latitude,
            double longitude,
            int forecastDays,
            boolean includePollen,
            String timezone
    ) {
        log.info("Tool invoked: get_air_quality(lat={}, lon={}, days={}, pollen={}, timezone={})",
                latitude, longitude, forecastDays, includePollen, timezone);

        // Default forecast days to 3 if not provided or invalid
        if (forecastDays <= 0) {
            forecastDays = 3;
        }

        // Default timezone to UTC if not provided
        if (timezone == null || timezone.isEmpty()) {
            timezone = "UTC";
        }

        return airQualityService.getAirQualityWithInterpretation(
                latitude, longitude, forecastDays, includePollen, timezone
        )
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Tool error: get_air_quality failed", ex);
                    } else {
                        log.info("Tool completed: get_air_quality returned data for {}, {}",
                                latitude, longitude);
                    }
                });
    }
}
