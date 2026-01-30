package com.openmeteo.mcp.model.request;

import java.io.Serializable;

/**
 * Request parameters for snow conditions API.
 * <p>
 * Contains input parameters for fetching snow conditions and mountain weather
 * from the Open-Meteo API.
 * </p>
 *
 * @param latitude      Latitude in decimal degrees (-90 to 90)
 * @param longitude     Longitude in decimal degrees (-180 to 180)
 * @param forecastDays  Number of forecast days (1-16)
 * @param includeHourly Whether to include hourly snow data
 * @param timezone      Timezone identifier (default: "auto")
 */
public record SnowRequest(
        double latitude,
        double longitude,
        int forecastDays,
        boolean includeHourly,
        String timezone
) implements Serializable {

    /**
     * Compact constructor with validation and defaults.
     */
    public SnowRequest {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        if (forecastDays < 1 || forecastDays > 16) {
            throw new IllegalArgumentException("Forecast days must be between 1 and 16");
        }
        if (timezone == null || timezone.isBlank()) {
            timezone = "auto";
        }
    }

    /**
     * Creates a default SnowRequest with 7-day forecast and hourly data.
     *
     * @param latitude  Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @return SnowRequest with defaults
     */
    public static SnowRequest withDefaults(double latitude, double longitude) {
        return new SnowRequest(latitude, longitude, 7, true, "auto");
    }
}
