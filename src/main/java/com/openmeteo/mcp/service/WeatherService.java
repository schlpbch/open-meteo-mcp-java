package com.openmeteo.mcp.service;

import com.openmeteo.mcp.client.OpenMeteoClient;
import com.openmeteo.mcp.model.dto.WeatherForecast;
import com.openmeteo.mcp.service.util.ValidationUtil;
import com.openmeteo.mcp.service.util.WeatherFormatter;
import com.openmeteo.mcp.service.util.WeatherInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for weather forecast operations.
 * <p>
 * Provides weather data retrieval with validation and interpretation.
 * </p>
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final OpenMeteoClient client;

    public WeatherService(OpenMeteoClient client) {
        this.client = client;
    }

    /**
     * Gets weather forecast with validation and enrichment.
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-16)
     * @param includeHourly Whether to include hourly forecast data
     * @param timezone      Timezone identifier
     * @return CompletableFuture with WeatherForecast
     */
    public CompletableFuture<WeatherForecast> getWeather(
            double latitude,
            double longitude,
            int forecastDays,
            boolean includeHourly,
            String timezone
    ) {
        // Validate inputs
        ValidationUtil.validateLatitude(latitude);
        ValidationUtil.validateLongitude(longitude);

        // Clamp forecast days to API limits (1-16)
        int clampedDays = ValidationUtil.clampForecastDays(forecastDays, 1, 16);

        log.info("Getting weather: lat={}, lon={}, days={}, includeHourly={}",
                latitude, longitude, clampedDays, includeHourly);

        // Delegate to client
        return client.getWeather(latitude, longitude, clampedDays, includeHourly, timezone);
    }

    /**
     * Gets weather with interpretation of current conditions.
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-16)
     * @param includeHourly Whether to include hourly forecast data
     * @param timezone      Timezone identifier
     * @return CompletableFuture with enriched weather data
     */
    public CompletableFuture<Map<String, Object>> getWeatherWithInterpretation(
            double latitude,
            double longitude,
            int forecastDays,
            boolean includeHourly,
            String timezone
    ) {
        return getWeather(latitude, longitude, forecastDays, includeHourly, timezone)
                .thenApply(forecast -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("forecast", forecast);

                    // Add interpretation if current weather available
                    if (forecast.currentWeather() != null) {
                        var current = forecast.currentWeather();
                        var interpretation = WeatherInterpreter.interpretWeatherCode(
                                current.weathercode()
                        );

                        Map<String, Object> interpreted = new HashMap<>();
                        interpreted.put("description", interpretation.description());
                        interpreted.put("category", interpretation.category());
                        interpreted.put("severity", interpretation.severity());
                        interpreted.put("travel_impact",
                                WeatherInterpreter.getTravelImpact(current.weathercode()));
                        interpreted.put("formatted_temperature",
                                WeatherFormatter.formatTemperature(current.temperature()));
                        interpreted.put("formatted_wind",
                                WeatherFormatter.formatWind(current.windspeed(),
                                        current.winddirection()));

                        result.put("interpretation", interpreted);
                    }

                    return result;
                });
    }
}
