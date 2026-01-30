package com.openmeteo.mcp.service.util;

/**
 * Input validation helpers for API parameters.
 * <p>
 * Provides validation methods for coordinates, strings, and parameter clamping.
 * </p>
 */
public class ValidationUtil {

    /**
     * Validates latitude is within valid range.
     *
     * @param latitude Latitude in decimal degrees
     * @throws IllegalArgumentException if latitude is out of range
     */
    public static void validateLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90, got: " + latitude
            );
        }
    }

    /**
     * Validates longitude is within valid range.
     *
     * @param longitude Longitude in decimal degrees
     * @throws IllegalArgumentException if longitude is out of range
     */
    public static void validateLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180, got: " + longitude
            );
        }
    }

    /**
     * Clamps a value between minimum and maximum bounds.
     *
     * @param value Value to clamp
     * @param min   Minimum allowed value
     * @param max   Maximum allowed value
     * @return Clamped value
     */
    public static int clampForecastDays(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Validates a string is not null or blank.
     *
     * @param value     String value to validate
     * @param fieldName Name of the field (for error message)
     * @throws IllegalArgumentException if value is null or blank
     */
    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null or blank"
            );
        }
    }
}
