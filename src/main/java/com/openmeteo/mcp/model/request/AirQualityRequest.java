package com.openmeteo.mcp.model.request;

import java.io.Serializable;

/**
 * Request parameters for air quality API.
 * <p>
 * Contains input parameters for fetching air quality forecasts including
 * AQI, pollutants, and pollen data from the Open-Meteo API.
 * </p>
 *
 * @param latitude      Latitude in decimal degrees (-90 to 90)
 * @param longitude     Longitude in decimal degrees (-180 to 180)
 * @param forecastDays  Number of forecast days (1-5 for air quality)
 * @param includePollen Whether to include pollen data (Europe only)
 * @param timezone      Timezone identifier (default: "auto")
 */
public record AirQualityRequest(
        double latitude,
        double longitude,
        int forecastDays,
        boolean includePollen,
        String timezone
) implements Serializable {

    /**
     * Compact constructor with validation and defaults.
     */
    public AirQualityRequest {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
        if (forecastDays < 1 || forecastDays > 5) {
            throw new IllegalArgumentException("Air quality forecast days must be between 1 and 5");
        }
        if (timezone == null || timezone.isBlank()) {
            timezone = "auto";
        }
    }

    /**
     * Creates a default AirQualityRequest with 3-day forecast and pollen data.
     *
     * @param latitude  Latitude in decimal degrees
     * @param longitude Longitude in decimal degrees
     * @return AirQualityRequest with defaults
     */
    public static AirQualityRequest withDefaults(double latitude, double longitude) {
        return new AirQualityRequest(latitude, longitude, 3, true, "auto");
    }
}
