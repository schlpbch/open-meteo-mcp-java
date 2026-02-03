package com.openmeteo.mcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for retrieving historical weather data from Open-Meteo Archive API.
 * <p>
 * Provides access to historical weather data from 1940 to present day.
 * </p>
 */
@Service
public class HistoricalWeatherService {

    private static final Logger log = LoggerFactory.getLogger(HistoricalWeatherService.class);
    private static final String ARCHIVE_API_URL = "https://archive-api.open-meteo.com/v1/archive";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final WebClient webClient;

    public HistoricalWeatherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(ARCHIVE_API_URL).build();
    }

    /**
     * Get historical weather data for a location and date range.
     *
     * @param latitude   Latitude in decimal degrees
     * @param longitude  Longitude in decimal degrees
     * @param startDate  Start date (YYYY-MM-DD)
     * @param endDate    End date (YYYY-MM-DD)
     * @param timezone   Timezone for timestamps
     * @return CompletableFuture with historical weather data
     */
    public CompletableFuture<Map<String, Object>> getHistoricalWeather(
            double latitude,
            double longitude,
            String startDate,
            String endDate,
            String timezone) {

        log.info("Fetching historical weather: lat={}, lon={}, start={}, end={}, tz={}",
                latitude, longitude, startDate, endDate, timezone);

        // Validate dates
        validateDateRange(startDate, endDate);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("latitude", latitude)
                        .queryParam("longitude", longitude)
                        .queryParam("start_date", startDate)
                        .queryParam("end_date", endDate)
                        .queryParam("daily", String.join(",",
                                "temperature_2m_max",
                                "temperature_2m_min",
                                "temperature_2m_mean",
                                "precipitation_sum",
                                "rain_sum",
                                "snowfall_sum",
                                "weathercode",
                                "windspeed_10m_max",
                                "windgusts_10m_max"
                        ))
                        .queryParam("timezone", timezone)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .toFuture()
                .thenApply(response -> processHistoricalData((Map<String, Object>) response, latitude, longitude, startDate, endDate, timezone))
                .exceptionally(ex -> {
                    log.error("Error fetching historical weather", ex);
                    return Map.of(
                            "error", "Failed to fetch historical weather data",
                            "message", ex.getMessage()
                    );
                });
    }

    /**
     * Process and enrich historical weather data.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> processHistoricalData(
            Map<String, Object> response,
            double latitude,
            double longitude,
            String startDate,
            String endDate,
            String timezone) {

        Map<String, Object> daily = (Map<String, Object>) response.getOrDefault("daily", Map.of());

        // Calculate statistics
        Map<String, Object> statistics = calculateStatistics(daily);

        // Build enriched response
        Map<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("timezone", timezone);
        result.put("start_date", startDate);
        result.put("end_date", endDate);
        result.put("daily", daily);
        result.put("statistics", statistics);

        return result;
    }

    /**
     * Calculate statistical summaries from daily data.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> calculateStatistics(Map<String, Object> daily) {
        Map<String, Object> stats = new HashMap<>();

        // Temperature statistics
        List<Number> maxTemps = (List<Number>) daily.get("temperature_2m_max");
        List<Number> minTemps = (List<Number>) daily.get("temperature_2m_min");
        List<Number> meanTemps = (List<Number>) daily.get("temperature_2m_mean");

        if (maxTemps != null && !maxTemps.isEmpty()) {
            stats.put("temperature", Map.of(
                    "max", maxTemps.stream().mapToDouble(Number::doubleValue).max().orElse(0.0),
                    "min", minTemps != null ? minTemps.stream().mapToDouble(Number::doubleValue).min().orElse(0.0) : 0.0,
                    "mean", meanTemps != null ? meanTemps.stream().mapToDouble(Number::doubleValue).average().orElse(0.0) : 0.0
            ));
        }

        // Precipitation statistics
        List<Number> precipitation = (List<Number>) daily.get("precipitation_sum");
        if (precipitation != null && !precipitation.isEmpty()) {
            double totalPrecip = precipitation.stream().mapToDouble(Number::doubleValue).sum();
            long rainyDays = precipitation.stream().filter(p -> p.doubleValue() > 0.1).count();

            stats.put("precipitation", Map.of(
                    "total_mm", totalPrecip,
                    "rainy_days", rainyDays,
                    "average_mm", precipitation.stream().mapToDouble(Number::doubleValue).average().orElse(0.0)
            ));
        }

        // Wind statistics
        List<Number> windSpeed = (List<Number>) daily.get("windspeed_10m_max");
        if (windSpeed != null && !windSpeed.isEmpty()) {
            stats.put("wind", Map.of(
                    "max_speed_kmh", windSpeed.stream().mapToDouble(Number::doubleValue).max().orElse(0.0),
                    "average_speed_kmh", windSpeed.stream().mapToDouble(Number::doubleValue).average().orElse(0.0)
            ));
        }

        return stats;
    }

    /**
     * Validate date range.
     */
    private void validateDateRange(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate end = LocalDate.parse(endDate, DATE_FORMATTER);
            LocalDate now = LocalDate.now();
            LocalDate earliestDate = LocalDate.of(1940, 1, 1);

            if (start.isBefore(earliestDate)) {
                throw new IllegalArgumentException("Start date cannot be before 1940-01-01");
            }

            if (end.isAfter(now)) {
                throw new IllegalArgumentException("End date cannot be in the future");
            }

            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Start date must be before or equal to end date");
            }

            // Limit to 1 year range to prevent excessive data
            if (start.plusYears(1).isBefore(end)) {
                log.warn("Date range exceeds 1 year, may return large dataset");
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD format", e);
        }
    }
}
