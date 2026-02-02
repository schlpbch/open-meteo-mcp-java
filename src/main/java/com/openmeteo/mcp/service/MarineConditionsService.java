package com.openmeteo.mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for retrieving marine/wave conditions from Open-Meteo Marine API.
 * <p>
 * Provides wave height, direction, period, and swell data for coastal areas and large lakes.
 * </p>
 */
@Service
public class MarineConditionsService {

    private static final Logger log = LoggerFactory.getLogger(MarineConditionsService.class);
    private static final String MARINE_API_URL = "https://marine-api.open-meteo.com/v1/marine";

    private final WebClient webClient;

    public MarineConditionsService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(MARINE_API_URL).build();
    }

    /**
     * Get marine conditions for a location.
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-7)
     * @param timezone      Timezone for timestamps
     * @return CompletableFuture with marine conditions data
     */
    public CompletableFuture<Map<String, Object>> getMarineConditions(
            double latitude,
            double longitude,
            int forecastDays,
            String timezone) {

        log.info("Fetching marine conditions: lat={}, lon={}, days={}, tz={}",
                latitude, longitude, forecastDays, timezone);

        // Validate forecast days
        if (forecastDays < 1 || forecastDays > 7) {
            forecastDays = 7;
        }

        final int finalForecastDays = forecastDays;

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("forecast_days", finalForecastDays)
                        .queryParam("hourly", String.join(",",
                                "wave_height",
                                "wave_direction",
                                "wave_period",
                                "wind_wave_height",
                                "wind_wave_direction",
                                "wind_wave_period",
                                "swell_wave_height",
                                "swell_wave_direction",
                                "swell_wave_period"
                        ))
                        .queryParam("timezone", timezone)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .toFuture()
                .thenApply(response -> processMarineData((Map<String, Object>) response, latitude, longitude, timezone))
                .exceptionally(ex -> {
                    log.error("Error fetching marine conditions", ex);
                    return Map.of(
                            "error", "Failed to fetch marine conditions",
                            "message", ex.getMessage()
                    );
                });
    }

    /**
     * Process and enrich marine conditions data.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> processMarineData(
            Map<String, Object> response,
            double latitude,
            double longitude,
            String timezone) {

        Map<String, Object> hourly = (Map<String, Object>) response.getOrDefault("hourly", Map.of());

        // Get current conditions (first hour)
        Map<String, Object> current = extractCurrentConditions(hourly);

        // Add safety assessment
        Map<String, Object> assessment = assessMarineConditions(current);

        // Build enriched response
        Map<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("timezone", timezone);
        result.put("current", current);
        result.put("hourly", hourly);
        result.put("assessment", assessment);

        return result;
    }

    /**
     * Extract current conditions from hourly data.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractCurrentConditions(Map<String, Object> hourly) {
        Map<String, Object> current = new HashMap<>();

        // Get first value from each hourly array
        for (Map.Entry<String, Object> entry : hourly.entrySet()) {
            if (entry.getValue() instanceof List) {
                List<?> values = (List<?>) entry.getValue();
                if (!values.isEmpty() && !"time".equals(entry.getKey())) {
                    current.put(entry.getKey(), values.get(0));
                }
            }
        }

        return current;
    }

    /**
     * Assess marine conditions for safety and suitability.
     */
    private Map<String, Object> assessMarineConditions(Map<String, Object> current) {
        double waveHeight = getDoubleValue(current, "wave_height", 0.0);
        double swellHeight = getDoubleValue(current, "swell_wave_height", 0.0);
        double windWaveHeight = getDoubleValue(current, "wind_wave_height", 0.0);

        String suitability;
        String recommendation;
        List<String> activities;

        if (waveHeight < 0.3) {
            suitability = "Excellent";
            recommendation = "Ideal conditions for all water activities";
            activities = List.of("Swimming", "Kayaking", "Paddleboarding", "Sailing", "Fishing");
        } else if (waveHeight < 0.6) {
            suitability = "Good";
            recommendation = "Good conditions for most water activities";
            activities = List.of("Swimming", "Kayaking", "Sailing", "Fishing");
        } else if (waveHeight < 1.0) {
            suitability = "Moderate";
            recommendation = "Suitable for experienced water sports enthusiasts";
            activities = List.of("Sailing", "Windsurfing", "Fishing");
        } else if (waveHeight < 1.5) {
            suitability = "Challenging";
            recommendation = "Only for experienced sailors and water sports athletes";
            activities = List.of("Sailing (experienced)", "Windsurfing");
        } else {
            suitability = "Dangerous";
            recommendation = "Not recommended for recreational water activities";
            activities = List.of();
        }

        return Map.of(
                "suitability", suitability,
                "recommendation", recommendation,
                "suitable_activities", activities,
                "wave_height_m", waveHeight,
                "swell_height_m", swellHeight,
                "wind_wave_height_m", windWaveHeight
        );
    }

    /**
     * Safely extract double value from map.
     */
    private double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
}
