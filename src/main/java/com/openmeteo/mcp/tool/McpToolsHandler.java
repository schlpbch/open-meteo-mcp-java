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
     * Search for locations by name using geocoding.
     *
     * @param name     Location name to search for (e.g., "London", "New York", "Tokyo")
     * @param count    Maximum number of results to return (default: 10, max: 100)
     * @param language Language code for results (e.g., "en" for English, "de" for German)
     * @param country  Optional ISO 3166-1 alpha-2 country code to filter results
     * @return CompletableFuture with GeocodingResponse containing location results
     *
     * Example usage:
     * - search_location("London", 10, "en", "") → Returns 10 locations named London
     * - search_location("Zurich", 5, "en", "CH") → Returns 5 locations in Switzerland
     */
    @McpTool(description = "Search for locations by name using geocoding. Returns multiple location results with coordinates, elevation, and country information.")
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
     * Get weather forecast with temperature, precipitation, wind, and current conditions.
     * Returns enriched data with WMO code interpretation and travel impact assessment.
     *
     * @param latitude      Latitude in decimal degrees (-90 to 90)
     * @param longitude     Longitude in decimal degrees (-180 to 180)
     * @param forecastDays  Number of forecast days to retrieve (1-16, default: 7)
     * @param timezone      Timezone identifier (e.g., "UTC", "Europe/London", "America/New_York")
     * @return CompletableFuture with enriched weather data including:
     *         - Current weather conditions with interpretation
     *         - Hourly forecast (24-hour format)
     *         - Daily summary with max/min temperatures
     *         - WMO weather code interpretation
     *         - Travel impact assessment
     *
     * Example usage:
     * - get_weather(51.5074, -0.1278, 7, "Europe/London") → 7-day forecast for London
     * - get_weather(48.8566, 2.3522, 5, "Europe/Paris") → 5-day forecast for Paris
     */
    @McpTool(description = "Get weather forecast with temperature, precipitation, wind, and current conditions. Returns enriched data with WMO code interpretation and travel impact assessment.")
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
     * Get snow conditions for ski trip planning with depth, recent snowfall, and assessments.
     * Returns enriched data with ski condition assessment based on snow depth, recent snowfall,
     * temperature, and current weather conditions.
     *
     * @param latitude      Latitude in decimal degrees (-90 to 90)
     * @param longitude     Longitude in decimal degrees (-180 to 180)
     * @param forecastDays  Number of forecast days (1-16, default: 7)
     * @param timezone      Timezone identifier (e.g., "UTC", "Europe/Zurich")
     * @return CompletableFuture with enriched snow data including:
     *         - Current and forecast snow depth in meters
     *         - Snowfall totals in cm
     *         - Snow conditions assessment ("Poor", "Fair", "Good", "Excellent")
     *         - Temperature forecast
     *         - Weather conditions
     *         - Hourly snow data (if available)
     *
     * Example usage:
     * - get_snow_conditions(46.4917, 10.2619, 5, "Europe/Zurich") → 5-day ski conditions for Alps
     * - get_snow_conditions(45.4872, 11.8753, 3, "Europe/Venice") → 3-day snow forecast for Dolomites
     */
    @McpTool(description = "Get snow conditions for ski trip planning with depth, recent snowfall, and assessments. Returns enriched data with ski condition assessment.")
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
     * Get air quality forecast with AQI, pollutants, UV index, and pollen data.
     * Returns enriched data with AQI level interpretation and UV index guidance.
     *
     * @param latitude      Latitude in decimal degrees (-90 to 90)
     * @param longitude     Longitude in decimal degrees (-180 to 180)
     * @param forecastDays  Number of forecast days (1-5, default: 3)
     * @param includePollen Whether to include pollen data (Europe only, default: true)
     * @param timezone      Timezone identifier (e.g., "UTC", "Europe/Berlin")
     * @return CompletableFuture with enriched air quality data including:
     *         - Current European AQI (0-500) with level interpretation
     *         - Current US AQI (0-500) with level interpretation
     *         - Pollutant concentrations (PM10, PM2.5, O3, NO2, SO2, CO)
     *         - UV Index with health guidance
     *         - Pollen data (6 types: alder, birch, grass, mugwort, olive, ragweed)
     *         - Hourly and daily forecasts
     *
     * Example usage:
     * - get_air_quality(52.5200, 13.4050, 3, true, "Europe/Berlin") → 3-day AQI forecast for Berlin with pollen
     * - get_air_quality(40.7128, -74.0060, 2, false, "America/New_York") → 2-day AQI forecast for NYC
     */
    @McpTool(description = "Get air quality forecast with AQI, pollutants, UV index, and pollen data. Returns enriched data with AQI level interpretation and UV index guidance.")
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
