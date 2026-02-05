package com.openmeteo.mcp.model.chat;

import java.util.Map;

/**
 * Context wrapper for enriching chat streaming with weather data.
 * Provides location information and optional weather data to enhance AI responses.
 * 
 * @param sessionId Session identifier
 * @param location Human-readable location (e.g., "Zurich, Switzerland")
 * @param latitude Latitude coordinate
 * @param longitude Longitude coordinate
 * @param weatherData Optional weather data map (temperature, conditions, etc.)
 * @param metadata Additional context metadata
 * 
 * @since 2.1.0
 */
public record ChatStreamContext(
    String sessionId,
    String location,
    Double latitude,
    Double longitude,
    Map<String, Object> weatherData,
    Map<String, Object> metadata
) {
    
    /**
     * Create context with just location coordinates.
     */
    public static ChatStreamContext withLocation(
        String sessionId,
        double latitude,
        double longitude
    ) {
        return new ChatStreamContext(
            sessionId,
            String.format("%.2f,%.2f", latitude, longitude),
            latitude,
            longitude,
            null,
            null
        );
    }
    
    /**
     * Create context with location and weather data.
     */
    public static ChatStreamContext withWeather(
        String sessionId,
        String location,
        double latitude,
        double longitude,
        Map<String, Object> weatherData
    ) {
        return new ChatStreamContext(
            sessionId,
            location,
            latitude,
            longitude,
            weatherData,
            null
        );
    }
    
    /**
     * Create full context with all data.
     */
    public static ChatStreamContext full(
        String sessionId,
        String location,
        double latitude,
        double longitude,
        Map<String, Object> weatherData,
        Map<String, Object> metadata
    ) {
        return new ChatStreamContext(
            sessionId,
            location,
            latitude,
            longitude,
            weatherData,
            metadata
        );
    }
    
    /**
     * Check if weather data is available.
     */
    public boolean hasWeatherData() {
        return weatherData != null && !weatherData.isEmpty();
    }
    
    /**
     * Check if metadata is available.
     */
    public boolean hasMetadata() {
        return metadata != null && !metadata.isEmpty();
    }
    
    /**
     * Get formatted location string.
     */
    public String getFormattedLocation() {
        if (location != null && !location.isEmpty()) {
            return location;
        }
        return String.format("%.2f°, %.2f°", latitude, longitude);
    }
}
