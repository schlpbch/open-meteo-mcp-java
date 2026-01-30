package com.openmeteo.mcp.tool;

import com.openmeteo.mcp.model.dto.GeocodingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller exposing MCP tools as HTTP endpoints.
 *
 * Provides REST API access to the 4 main Open-Meteo MCP tools:
 * - GET /api/tools/search-location
 * - GET /api/tools/weather
 * - GET /api/tools/snow-conditions
 * - GET /api/tools/air-quality
 *
 * All endpoints are async and return JSON responses.
 */
@RestController
@RequestMapping("/api/tools")
@CrossOrigin(origins = "*", maxAge = 3600)
public class McpToolsController {

    private static final Logger log = LoggerFactory.getLogger(McpToolsController.class);

    private final McpToolsHandler toolsHandler;

    public McpToolsController(McpToolsHandler toolsHandler) {
        this.toolsHandler = toolsHandler;
    }

    /**
     * Search for locations by name (geocoding).
     *
     * GET /api/tools/search-location?name=London&count=10&language=en&country=
     *
     * @param name     Location name to search for (required)
     * @param count    Maximum number of results (optional, default: 10)
     * @param language Language code (optional, default: en)
     * @param country  ISO 3166-1 alpha-2 country code (optional)
     * @return CompletableFuture with GeocodingResponse
     */
    @GetMapping("/search-location")
    public CompletableFuture<ResponseEntity<GeocodingResponse>> searchLocation(
            @RequestParam String name,
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "en") String language,
            @RequestParam(defaultValue = "") String country
    ) {
        log.info("REST: GET /api/tools/search-location?name={}&count={}&language={}&country={}",
                name, count, language, country);

        return toolsHandler.searchLocation(name, count, language, country)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("REST error in searchLocation", ex);
                    return ResponseEntity.status(500).build();
                });
    }

    /**
     * Get weather forecast.
     *
     * GET /api/tools/weather?latitude=51.5074&longitude=-0.1278&forecastDays=7&timezone=Europe/London
     *
     * @param latitude     Latitude in decimal degrees (required)
     * @param longitude    Longitude in decimal degrees (required)
     * @param forecastDays Number of forecast days (optional, default: 7)
     * @param timezone     Timezone identifier (optional, default: UTC)
     * @return CompletableFuture with enriched weather data
     */
    @GetMapping("/weather")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getWeather(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "7") int forecastDays,
            @RequestParam(defaultValue = "UTC") String timezone
    ) {
        log.info("REST: GET /api/tools/weather?latitude={}&longitude={}&forecastDays={}&timezone={}",
                latitude, longitude, forecastDays, timezone);

        return toolsHandler.getWeather(latitude, longitude, forecastDays, timezone)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("REST error in getWeather", ex);
                    return ResponseEntity.status(500).build();
                });
    }

    /**
     * Get snow conditions for ski planning.
     *
     * GET /api/tools/snow-conditions?latitude=46.4917&longitude=10.2619&forecastDays=5&timezone=Europe/Zurich
     *
     * @param latitude     Latitude in decimal degrees (required)
     * @param longitude    Longitude in decimal degrees (required)
     * @param forecastDays Number of forecast days (optional, default: 7)
     * @param timezone     Timezone identifier (optional, default: UTC)
     * @return CompletableFuture with enriched snow data
     */
    @GetMapping("/snow-conditions")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getSnowConditions(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "7") int forecastDays,
            @RequestParam(defaultValue = "UTC") String timezone
    ) {
        log.info("REST: GET /api/tools/snow-conditions?latitude={}&longitude={}&forecastDays={}&timezone={}",
                latitude, longitude, forecastDays, timezone);

        return toolsHandler.getSnowConditions(latitude, longitude, forecastDays, timezone)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("REST error in getSnowConditions", ex);
                    return ResponseEntity.status(500).build();
                });
    }

    /**
     * Get air quality forecast.
     *
     * GET /api/tools/air-quality?latitude=52.52&longitude=13.405&forecastDays=3&includePollen=true&timezone=Europe/Berlin
     *
     * @param latitude       Latitude in decimal degrees (required)
     * @param longitude      Longitude in decimal degrees (required)
     * @param forecastDays   Number of forecast days (optional, default: 3)
     * @param includePollen  Include pollen data (optional, default: true)
     * @param timezone       Timezone identifier (optional, default: UTC)
     * @return CompletableFuture with enriched air quality data
     */
    @GetMapping("/air-quality")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getAirQuality(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "3") int forecastDays,
            @RequestParam(defaultValue = "true") boolean includePollen,
            @RequestParam(defaultValue = "UTC") String timezone
    ) {
        log.info("REST: GET /api/tools/air-quality?latitude={}&longitude={}&forecastDays={}&includePollen={}&timezone={}",
                latitude, longitude, forecastDays, includePollen, timezone);

        return toolsHandler.getAirQuality(latitude, longitude, forecastDays, includePollen, timezone)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> {
                    log.error("REST error in getAirQuality", ex);
                    return ResponseEntity.status(500).build();
                });
    }

    /**
     * Health check endpoint.
     *
     * GET /api/tools/health
     *
     * @return Simple OK response indicating service is running
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK", "service", "Open-Meteo MCP Tools"));
    }
}
