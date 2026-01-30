package com.openmeteo.mcp.service;

import com.openmeteo.mcp.client.OpenMeteoClient;
import com.openmeteo.mcp.model.dto.SnowConditions;
import com.openmeteo.mcp.service.util.SkiConditionAssessor;
import com.openmeteo.mcp.service.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for snow conditions and ski planning operations.
 * <p>
 * Provides snow data retrieval with ski condition assessment.
 * </p>
 */
@Service
public class SnowConditionsService {

    private static final Logger log = LoggerFactory.getLogger(SnowConditionsService.class);

    private final OpenMeteoClient client;

    public SnowConditionsService(OpenMeteoClient client) {
        this.client = client;
    }

    /**
     * Gets snow conditions for ski planning.
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-16)
     * @param includeHourly Whether to include hourly snow data
     * @param timezone      Timezone identifier
     * @return CompletableFuture with SnowConditions
     */
    public CompletableFuture<SnowConditions> getSnowConditions(
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

        log.info("Getting snow conditions: lat={}, lon={}, days={}",
                latitude, longitude, clampedDays);

        // Delegate to client
        return client.getSnowConditions(latitude, longitude, clampedDays,
                includeHourly, timezone);
    }

    /**
     * Gets snow conditions with ski assessment.
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-16)
     * @param includeHourly Whether to include hourly snow data
     * @param timezone      Timezone identifier
     * @return CompletableFuture with enriched snow data
     */
    public CompletableFuture<Map<String, Object>> getSnowConditionsWithAssessment(
            double latitude,
            double longitude,
            int forecastDays,
            boolean includeHourly,
            String timezone
    ) {
        return getSnowConditions(latitude, longitude, forecastDays,
                        includeHourly, timezone)
                .thenApply(conditions -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("conditions", conditions);

                    // Add ski assessment if data available
                    if (conditions.hourly() != null && conditions.daily() != null) {
                        var hourly = conditions.hourly();
                        var daily = conditions.daily();

                        // Get most recent data (index 0)
                        if (!hourly.snowDepth().isEmpty() &&
                                !daily.snowfallSum().isEmpty() &&
                                !hourly.temperature2m().isEmpty() &&
                                !hourly.weathercode().isEmpty()) {

                            double snowDepth = hourly.snowDepth().get(0);
                            double recentSnowfall = daily.snowfallSum().get(0);
                            double temperature = hourly.temperature2m().get(0);
                            int weatherCode = hourly.weathercode().get(0);

                            String assessment = SkiConditionAssessor.assessSkiConditions(
                                    snowDepth, recentSnowfall, temperature, weatherCode
                            );

                            Map<String, Object> skiInfo = new HashMap<>();
                            skiInfo.put("assessment", assessment);
                            skiInfo.put("snow_depth_m", snowDepth);
                            skiInfo.put("recent_snowfall_cm", recentSnowfall);
                            skiInfo.put("temperature_c", temperature);

                            result.put("ski_assessment", skiInfo);
                        }
                    }

                    return result;
                });
    }
}
