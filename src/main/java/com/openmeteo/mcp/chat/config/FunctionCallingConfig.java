package com.openmeteo.mcp.chat.config;

import com.openmeteo.mcp.tool.AdvancedToolsHandler;
import com.openmeteo.mcp.tool.McpToolsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Configuration for Spring AI function calling with MCP tools.
 * Registers all 11 Open-Meteo MCP tools as Spring AI functions.
 * 
 * @since 2.0.0
 */
@Configuration
@ConditionalOnProperty(name = "openmeteo.chat.enabled", havingValue = "true")
public class FunctionCallingConfig {
    
    private static final Logger log = LoggerFactory.getLogger(FunctionCallingConfig.class);
    
    private final McpToolsHandler mcpTools;
    private final AdvancedToolsHandler advancedTools;
    
    public FunctionCallingConfig(McpToolsHandler mcpTools, AdvancedToolsHandler advancedTools) {
        this.mcpTools = mcpTools;
        this.advancedTools = advancedTools;
        log.info("Initializing function calling with 11 MCP tools");
    }
    
    // Core Tools (4)
    
    @Bean
    @Description("Search for locations by name to get coordinates for weather queries")
    public Function<SearchLocationRequest, CompletableFuture<Map<String, Object>>> meteo__search_location() {
        return request -> {
            String language = request.language() != null ? request.language() : "en";
            int count = request.count() > 0 ? request.count() : 10;
            String country = request.country() != null ? request.country() : "";
            
            return mcpTools.searchLocation(request.query(), count, language, country)
                .thenApply(response -> Map.of(
                    "results", response.results() != null ? response.results() : java.util.List.of()
                ));
        };
    }
    
    @Bean
    @Description("Get weather forecast for a location (temperature, rain, sunshine)")
    public Function<GetWeatherRequest, CompletableFuture<Map<String, Object>>> meteo__get_weather() {
        return request -> mcpTools.getWeather(
            request.latitude(),
            request.longitude(),
            request.forecastDays() > 0 ? request.forecastDays() : 7,
            request.timezone() != null ? request.timezone() : "UTC"
        );
    }
    
    @Bean
    @Description("Get snow conditions and forecasts for mountain locations")
    public Function<SnowConditionsRequest, CompletableFuture<Map<String, Object>>> meteo__get_snow_conditions() {
        return request -> mcpTools.getSnowConditions(
            request.latitude(),
            request.longitude(),
            request.forecastDays() > 0 ? request.forecastDays() : 7,
            request.timezone() != null ? request.timezone() : "UTC"
        );
    }
    
    @Bean
    @Description("Get air quality forecast including AQI, pollutants, UV index, and pollen data")
    public Function<AirQualityRequest, CompletableFuture<Map<String, Object>>> meteo__get_air_quality() {
        return request -> mcpTools.getAirQuality(
            request.latitude(),
            request.longitude(),
            request.forecastDays() > 0 ? request.forecastDays() : 5,
            request.includePollen(),
            request.timezone() != null ? request.timezone() : "auto"
        );
    }
    
    // Advanced Tools (7)
    
    @Bean
    @Description("Generate weather alerts based on thresholds and current forecast")
    public Function<WeatherAlertsRequest, CompletableFuture<Map<String, Object>>> meteo__get_weather_alerts() {
        return request -> advancedTools.getWeatherAlerts(
            request.latitude(),
            request.longitude(),
            request.forecastHours() > 0 ? request.forecastHours() : 24,
            request.timezone() != null ? request.timezone() : "auto"
        );
    }
    
    @Bean
    @Description("Calculate outdoor activity comfort index (0-100)")
    public Function<ComfortIndexRequest, CompletableFuture<Map<String, Object>>> meteo__get_comfort_index() {
        return request -> advancedTools.getComfortIndex(
            request.latitude(),
            request.longitude(),
            request.timezone() != null ? request.timezone() : "auto"
        );
    }
    
    @Bean
    @Description("Get astronomical data for a location (sunrise, sunset, golden hour)")
    public Function<AstronomyRequest, CompletableFuture<Map<String, Object>>> meteo__get_astronomy() {
        return request -> advancedTools.getAstronomy(
            request.latitude(),
            request.longitude(),
            request.timezone() != null ? request.timezone() : "auto"
        );
    }
    
    @Bean
    @Description("Search for locations in Switzerland with optional geographic features")
    public Function<SearchSwissRequest, CompletableFuture<Map<String, Object>>> meteo__search_location_swiss() {
        return request -> advancedTools.searchLocationSwiss(
            request.query(),
            request.includeFeatures(),
            request.language() != null ? request.language() : "en",
            request.count() > 0 ? request.count() : 10
        );
    }
    
    @Bean
    @Description("Compare weather conditions across multiple locations")
    public Function<CompareLocationsRequest, CompletableFuture<Map<String, Object>>> meteo__compare_locations() {
        return request -> advancedTools.compareLocations(
            request.locations(),
            request.criteria() != null ? request.criteria() : "best_overall",
            request.forecastDays() > 0 ? request.forecastDays() : 1
        );
    }
    
    @Bean
    @Description("Get historical weather data from 1940 to present")
    public Function<HistoricalWeatherRequest, CompletableFuture<Map<String, Object>>> meteo__get_historical_weather() {
        return request -> advancedTools.getHistoricalWeather(
            request.latitude(),
            request.longitude(),
            request.startDate(),
            request.endDate(),
            request.timezone() != null ? request.timezone() : "auto"
        );
    }
    
    @Bean
    @Description("Get wave/swell data for coastal areas and large lakes")
    public Function<MarineConditionsRequest, CompletableFuture<Map<String, Object>>> meteo__get_marine_conditions() {
        return request -> advancedTools.getMarineConditions(
            request.latitude(),
            request.longitude(),
            request.forecastDays() > 0 ? request.forecastDays() : 7,
            request.timezone() != null ? request.timezone() : "auto"
        );
    }
    
    // Request Records
    
    public record SearchLocationRequest(String query, String language, int count, String country) {}
    public record GetWeatherRequest(double latitude, double longitude, int forecastDays, String timezone) {}
    public record SnowConditionsRequest(double latitude, double longitude, int forecastDays, String timezone) {}
    public record AirQualityRequest(double latitude, double longitude, int forecastDays, boolean includePollen, String timezone) {}
    public record WeatherAlertsRequest(double latitude, double longitude, int forecastHours, String timezone) {}
    public record ComfortIndexRequest(double latitude, double longitude, String timezone) {}
    public record AstronomyRequest(double latitude, double longitude, String timezone) {}
    public record SearchSwissRequest(String query, boolean includeFeatures, String language, int count) {}
    public record CompareLocationsRequest(java.util.List<java.util.Map<String, Object>> locations, String criteria, int forecastDays) {}
    public record HistoricalWeatherRequest(double latitude, double longitude, String startDate, String endDate, String timezone) {}
    public record MarineConditionsRequest(double latitude, double longitude, int forecastDays, String timezone) {}
}
