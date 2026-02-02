package com.openmeteo.mcp.tool;

import com.openmeteo.mcp.helper.AstronomyCalculator;
import com.openmeteo.mcp.helper.ComfortIndexCalculator;
import com.openmeteo.mcp.helper.WeatherAlertGenerator;
import com.openmeteo.mcp.service.AirQualityService;
import com.openmeteo.mcp.service.LocationService;
import com.openmeteo.mcp.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Advanced MCP Tools Handler providing weather alerts, comfort index, astronomy, and comparison tools.
 *
 * Exposes 5 advanced Open-Meteo MCP tools for AI assistants:
 * 1. meteo__get_weather_alerts - Weather alerts based on thresholds
 * 2. meteo__get_comfort_index - Outdoor activity comfort score (0-100)
 * 3. meteo__get_astronomy - Sunrise, sunset, golden hour, moon phase
 * 4. meteo__search_location_swiss - Swiss-specific location search
 * 5. meteo__compare_locations - Multi-location weather comparison
 */
@Component
public class AdvancedToolsHandler {

    private static final Logger log = LoggerFactory.getLogger(AdvancedToolsHandler.class);

    private final WeatherService weatherService;
    private final AirQualityService airQualityService;
    private final LocationService locationService;

    public AdvancedToolsHandler(
            WeatherService weatherService,
            AirQualityService airQualityService,
            LocationService locationService) {
        this.weatherService = weatherService;
        this.airQualityService = airQualityService;
        this.locationService = locationService;
    }

    /**
     * MCP Tool: meteo__get_weather_alerts
     *
     * Generate weather alerts based on thresholds and current forecast.
     */
    @McpTool(name = "meteo__get_weather_alerts", description = """
            Generate weather alerts based on thresholds and current forecast.

            Automatically identifies severe weather conditions and generates actionable alerts.

            ALERT TYPES:
            - Heat warnings (temperature > 30°C for 3+ hours)
            - Cold warnings (temperature < -10°C)
            - Storm warnings (wind gusts > 80 km/h or thunderstorms)
            - UV warnings (UV index > 8)
            - Wind advisories (gusts 50-80 km/h)

            SEVERITY LEVELS:
            - Advisory: Precautionary, plan accordingly
            - Watch: Conditions favorable for alert type
            - Warning: Conditions expected, take precautions

            USE THIS TOOL FOR:
            - Check for heat waves during summer
            - Monitor for storms before outdoor events
            - Plan sun protection based on UV alerts
            """)
    public CompletableFuture<Map<String, Object>> getWeatherAlerts(
            @McpToolParam(description = "Latitude in decimal degrees", required = true) double latitude,
            @McpToolParam(description = "Longitude in decimal degrees", required = true) double longitude,
            @McpToolParam(description = "Hours to check for alerts (1-168, default: 24)") int forecastHours,
            @McpToolParam(description = "Timezone for timestamps (default: 'auto')") String timezone) {
        log.info("Tool invoked: meteo__get_weather_alerts(lat={}, lon={}, hours={}, timezone={})",
                latitude, longitude, forecastHours, timezone);

        if (forecastHours <= 0) forecastHours = 24;
        if (timezone == null || timezone.isEmpty()) timezone = "auto";

        int forecastDays = Math.min(Math.max(forecastHours / 24 + 1, 1), 16);
        String finalTimezone = timezone;

        return weatherService.getWeatherWithInterpretation(latitude, longitude, forecastDays, true, timezone)
                .thenApply(weatherData -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> current = (Map<String, Object>) weatherData.getOrDefault("current", Map.of());
                    @SuppressWarnings("unchecked")
                    Map<String, Object> hourly = (Map<String, Object>) weatherData.getOrDefault("hourly", Map.of());
                    @SuppressWarnings("unchecked")
                    Map<String, Object> daily = (Map<String, Object>) weatherData.getOrDefault("daily", Map.of());
                    String tz = (String) weatherData.getOrDefault("timezone", finalTimezone);

                    var alerts = WeatherAlertGenerator.generateWeatherAlerts(current, hourly, daily, tz);

                    return Map.of(
                            "latitude", latitude,
                            "longitude", longitude,
                            "timezone", tz,
                            "alerts", alerts
                    );
                });
    }

    /**
     * MCP Tool: meteo__get_comfort_index
     *
     * Calculates outdoor activity comfort index (0-100).
     */
    @McpTool(name = "meteo__get_comfort_index", description = """
            Calculates outdoor activity comfort index (0-100).

            Combines weather, air quality, UV, and precipitation factors into a single
            comfort score for planning outdoor activities.

            SCORE INTERPRETATION:
            - 80-100: Perfect for outdoor activities
            - 60-79: Good conditions
            - 40-59: Fair conditions, plan accordingly
            - 20-39: Poor conditions, seek indoor alternatives
            - 0-19: Very poor conditions

            FACTORS INCLUDED:
            - Thermal comfort (temperature, humidity, wind chill)
            - Air quality (PM2.5, PM10, AQI)
            - Precipitation risk
            - UV safety (skin protection needs)
            - Weather conditions (storms, visibility)

            USE THIS TOOL FOR:
            - \"Is it good weather for hiking?\"
            - \"What's the outdoor comfort level?\"
            - \"Can I do outdoor sports today?\"
            """)
    public CompletableFuture<Map<String, Object>> getComfortIndex(
            @McpToolParam(description = "Latitude in decimal degrees", required = true) double latitude,
            @McpToolParam(description = "Longitude in decimal degrees", required = true) double longitude,
            @McpToolParam(description = "Timezone for timestamps (default: 'auto')") String timezone) {
        log.info("Tool invoked: meteo__get_comfort_index(lat={}, lon={}, timezone={})",
                latitude, longitude, timezone);

        if (timezone == null || timezone.isEmpty()) timezone = "auto";
        String finalTimezone = timezone;

        return weatherService.getWeatherWithInterpretation(latitude, longitude, 1, false, timezone)
                .thenCombine(
                        airQualityService.getAirQualityWithInterpretation(latitude, longitude, 1, false, timezone),
                        (weatherData, airQualityData) -> {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> current = (Map<String, Object>) weatherData.getOrDefault("current", Map.of());
                            @SuppressWarnings("unchecked")
                            Map<String, Object> currentAqi = (Map<String, Object>) airQualityData.getOrDefault("current", Map.of());
                            String tz = (String) weatherData.getOrDefault("timezone", finalTimezone);

                            var comfortIndex = ComfortIndexCalculator.calculateComfortIndex(current, currentAqi);

                            return Map.of(
                                    "latitude", latitude,
                                    "longitude", longitude,
                                    "timezone", tz,
                                    "comfort_index", comfortIndex
                            );
                        });
    }

    /**
     * MCP Tool: meteo__get_astronomy
     *
     * Provides astronomical data for a location (sunrise, sunset, golden hour).
     */
    @McpTool(name = "meteo__get_astronomy", description = """
            Provides astronomical data for a location (sunrise, sunset, golden hour).

            Useful for photography, event planning, and outdoor activity scheduling.

            DATA PROVIDED:
            - Sunrise and sunset times
            - Day length
            - Golden hour (best lighting for photography)
            - Blue hour (evening twilight)
            - Moon phase information
            - Best photography windows

            USE CASES:
            - Photography location scouting
            - Outdoor event planning
            - Sunrise/sunset viewing trips
            - Time-lapse and video production planning

            EXAMPLES:
            - \"When is sunset in Zurich?\"
            - \"Best time for golden hour photography?\"
            - \"What's the sunrise time for hiking?\"
            """)
    public CompletableFuture<Map<String, Object>> getAstronomy(
            @McpToolParam(description = "Latitude in decimal degrees", required = true) double latitude,
            @McpToolParam(description = "Longitude in decimal degrees", required = true) double longitude,
            @McpToolParam(description = "Timezone for timestamps (default: 'auto')") String timezone) {
        log.info("Tool invoked: meteo__get_astronomy(lat={}, lon={}, timezone={})",
                latitude, longitude, timezone);

        if (timezone == null || timezone.isEmpty() || "auto".equals(timezone)) {
            return weatherService.getWeatherWithInterpretation(latitude, longitude, 1, false, "auto")
                    .thenApply(weatherData -> {
                        String tz = (String) weatherData.getOrDefault("timezone", "UTC");
                        var astronomy = AstronomyCalculator.calculateAstronomyData(latitude, longitude, tz);

                        return Map.of(
                                "latitude", latitude,
                                "longitude", longitude,
                                "timezone", tz,
                                "astronomy", astronomy
                        );
                    });
        } else {
            var astronomy = AstronomyCalculator.calculateAstronomyData(latitude, longitude, timezone);

            return CompletableFuture.completedFuture(Map.of(
                    "latitude", latitude,
                    "longitude", longitude,
                    "timezone", timezone,
                    "astronomy", astronomy
            ));
        }
    }

    /**
     * MCP Tool: meteo__search_location_swiss
     *
     * Search for locations in Switzerland with optional geographic features.
     */
    @McpTool(name = "meteo__search_location_swiss", description = """
            Search for locations in Switzerland with optional geographic features.

            Specialized search for Swiss locations including cities, mountains, lakes, and passes.

            FEATURE TYPES SUPPORTED:
            - PPL: Populated places (cities, towns, villages)
            - MT: Mountains and peaks
            - LK: Lakes and water bodies
            - PS: Mountain passes
            - STM: Streams and rivers

            EXAMPLES:
            - \"Find Zurich\" → Zurich city
            - \"Search for Matterhorn\" → Mountain peak
            - \"Find Lake Geneva\" → Lake location
            - \"Find Gotthard Pass\" → Mountain pass

            USE THIS TOOL WHEN:
            - Searching specifically within Switzerland
            - Looking for mountains, lakes, or geographic features
            - Need precise coordinates for Swiss locations
            - Want to filter by location type
            """)
    public CompletableFuture<Map<String, Object>> searchLocationSwiss(
            @McpToolParam(description = "Location name to search", required = true) String name,
            @McpToolParam(description = "Include geographic features like mountains, lakes (default: false)") boolean includeFeatures,
            @McpToolParam(description = "Language for results (de, fr, it, en; default: en)") String language,
            @McpToolParam(description = "Number of results (1-50, default: 10)") int count) {
        log.info("Tool invoked: meteo__search_location_swiss(name={}, includeFeatures={}, language={}, count={})",
                name, includeFeatures, language, count);

        if (language == null || language.isEmpty()) language = "en";
        if (count <= 0) count = 10;

        final String finalLanguage = language;
        final int finalCount = count;

        return locationService.searchLocation(name, finalCount * 2, finalLanguage, "CH")
                .thenApply(response -> {
                    var results = response.results();
                    if (results == null) {
                        return Map.of(
                                "query", name,
                                "results", List.of(),
                                "total", 0,
                                "country", "CH",
                                "include_features", includeFeatures,
                                "language", finalLanguage
                        );
                    }

                    var filtered = results.stream()
                            .filter(loc -> includeFeatures || loc.featureCode() == null || loc.featureCode().startsWith("PPL"))
                            .sorted((a, b) -> {
                                Long popA = a.population() != null ? a.population() : 0L;
                                Long popB = b.population() != null ? b.population() : 0L;
                                return popB.compareTo(popA);
                            })
                            .limit(finalCount)
                            .toList();

                    return Map.of(
                            "query", name,
                            "results", filtered,
                            "total", filtered.size(),
                            "country", "CH",
                            "include_features", includeFeatures,
                            "language", finalLanguage
                    );
                });
    }

    /**
     * MCP Tool: meteo__compare_locations
     *
     * Compare weather conditions across multiple locations.
     */
    @McpTool(name = "meteo__compare_locations", description = """
            Compare weather conditions across multiple locations.

            Rank locations by specified weather criteria to find the best destination.

            COMPARISON CRITERIA:
            - best_overall: Overall comfort and conditions
            - warmest: Highest temperature
            - driest: Lowest precipitation probability
            - sunniest: Best weather codes and visibility
            - best_air_quality: Lowest AQI
            - calmest: Lowest wind speeds

            EXAMPLES:
            - Compare weekend weather between Zurich, Bern, and Geneva
            - Find the warmest location for outdoor activities
            - Identify the driest location for hiking
            - Compare air quality across multiple cities

            USE THIS TOOL WHEN:
            - Choosing between multiple destination options
            - Planning group activities
            - Finding optimal conditions for specific activities
            """)
    public CompletableFuture<Map<String, Object>> compareLocations(
            @McpToolParam(description = "List of location maps with 'name', 'latitude', 'longitude'", required = true) List<Map<String, Object>> locations,
            @McpToolParam(description = "Comparison criteria (default: 'best_overall')") String criteria,
            @McpToolParam(description = "Days to forecast (1-16, default: 1)") int forecastDays) {
        log.info("Tool invoked: meteo__compare_locations(locations={}, criteria={}, days={})",
                locations.size(), criteria, forecastDays);

        if (criteria == null || criteria.isEmpty()) criteria = "best_overall";
        if (forecastDays <= 0) forecastDays = 1;

        final String finalCriteria = criteria;
        final int finalForecastDays = forecastDays;

        List<CompletableFuture<Map<String, Object>>> futures = locations.stream()
                .map(loc -> {
                    String locName = (String) loc.getOrDefault("name", "Unknown");
                    double lat = ((Number) loc.getOrDefault("latitude", 46.95)).doubleValue();
                    double lon = ((Number) loc.getOrDefault("longitude", 7.45)).doubleValue();

                    return weatherService.getWeatherWithInterpretation(lat, lon, finalForecastDays, false, "auto")
                            .thenCombine(
                                    airQualityService.getAirQualityWithInterpretation(lat, lon, 1, false, "auto"),
                                    (weather, airQuality) -> {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> current = (Map<String, Object>) weather.getOrDefault("current", Map.of());
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> currentAqi = (Map<String, Object>) airQuality.getOrDefault("current", Map.of());

                                        var comfortIndex = ComfortIndexCalculator.calculateComfortIndex(current, currentAqi);

                                        return Map.of(
                                                "name", locName,
                                                "latitude", lat,
                                                "longitude", lon,
                                                "temperature", current.getOrDefault("temperature", 0.0),
                                                "wind_speed", current.getOrDefault("windspeed", 0.0),
                                                "weather_code", current.getOrDefault("weathercode", 0),
                                                "comfort_index", comfortIndex.get("overall"),
                                                "aqi", currentAqi.getOrDefault("european_aqi", 0.0),
                                                "recommendation", comfortIndex.get("recommendation")
                                        );
                                    })
                            .exceptionally(ex -> Map.of("name", locName, "error", ex.getMessage()));
                })
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<Map<String, Object>> results = futures.stream()
                            .map(CompletableFuture::join)
                            .collect(java.util.stream.Collectors.toList());

                    results.sort((a, b) -> {
                        if (a.containsKey("error") || b.containsKey("error")) return 0;

                        return switch (finalCriteria) {
                            case "warmest" -> Double.compare(
                                    ((Number) b.getOrDefault("temperature", 0.0)).doubleValue(),
                                    ((Number) a.getOrDefault("temperature", 0.0)).doubleValue()
                            );
                            case "calmest" -> Double.compare(
                                    ((Number) a.getOrDefault("wind_speed", 999.0)).doubleValue(),
                                    ((Number) b.getOrDefault("wind_speed", 999.0)).doubleValue()
                            );
                            case "sunniest" -> Integer.compare(
                                    ((Number) a.getOrDefault("weather_code", 99)).intValue(),
                                    ((Number) b.getOrDefault("weather_code", 99)).intValue()
                            );
                            case "best_air_quality" -> Double.compare(
                                    ((Number) a.getOrDefault("aqi", 999.0)).doubleValue(),
                                    ((Number) b.getOrDefault("aqi", 999.0)).doubleValue()
                            );
                            default -> Double.compare(
                                    ((Number) b.getOrDefault("comfort_index", 0.0)).doubleValue(),
                                    ((Number) a.getOrDefault("comfort_index", 0.0)).doubleValue()
                            );
                        };
                    });

                    return Map.of(
                            "criteria", finalCriteria,
                            "locations", results,
                            "winner", results.isEmpty() ? null : results.get(0),
                            "comparison_timestamp", java.time.Instant.now().toString()
                    );
                });
    }
}
