package com.openmeteo.mcp.service;

import com.openmeteo.mcp.client.OpenMeteoClient;
import com.openmeteo.mcp.model.dto.AirQualityForecast;
import com.openmeteo.mcp.service.util.ValidationUtil;
import com.openmeteo.mcp.service.util.WeatherFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for air quality forecast operations.
 * <p>
 * Provides air quality data retrieval with AQI and UV interpretation.
 * </p>
 */
@Service
public class AirQualityService {

    private static final Logger log = LoggerFactory.getLogger(AirQualityService.class);

    private final OpenMeteoClient client;

    public AirQualityService(OpenMeteoClient client) {
        this.client = client;
    }

    /**
     * Gets air quality forecast.
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-5)
     * @param includePollen Whether to include pollen data (Europe only)
     * @param timezone      Timezone identifier
     * @return CompletableFuture with AirQualityForecast
     */
    public CompletableFuture<AirQualityForecast> getAirQuality(
            double latitude,
            double longitude,
            int forecastDays,
            boolean includePollen,
            String timezone
    ) {
        // Validate inputs
        ValidationUtil.validateLatitude(latitude);
        ValidationUtil.validateLongitude(longitude);

        // Clamp forecast days to API limits (1-5 for air quality)
        int clampedDays = ValidationUtil.clampForecastDays(forecastDays, 1, 5);

        log.info("Getting air quality: lat={}, lon={}, days={}, pollen={}",
                latitude, longitude, clampedDays, includePollen);

        // Delegate to client
        return client.getAirQuality(latitude, longitude, clampedDays,
                includePollen, timezone);
    }

    /**
     * Gets air quality with interpretation.
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-5)
     * @param includePollen Whether to include pollen data (Europe only)
     * @param timezone      Timezone identifier
     * @return CompletableFuture with enriched air quality data
     */
    public CompletableFuture<Map<String, Object>> getAirQualityWithInterpretation(
            double latitude,
            double longitude,
            int forecastDays,
            boolean includePollen,
            String timezone
    ) {
        return getAirQuality(latitude, longitude, forecastDays,
                        includePollen, timezone)
                .thenApply(forecast -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("forecast", forecast);

                    // Add interpretation if current data available
                    if (forecast.current() != null) {
                        var current = forecast.current();

                        Map<String, Object> interpreted = new HashMap<>();

                        if (current.europeanAqi() != null) {
                            interpreted.put("european_aqi_level",
                                    WeatherFormatter.interpretAqi(current.europeanAqi(), true));
                        }

                        if (current.usAqi() != null) {
                            interpreted.put("us_aqi_level",
                                    WeatherFormatter.interpretAqi(current.usAqi(), false));
                        }

                        if (current.uvIndex() != null) {
                            interpreted.put("uv_index_level",
                                    WeatherFormatter.interpretUvIndex(current.uvIndex()));
                        }

                        result.put("interpretation", interpreted);
                    }

                    return result;
                });
    }
}
