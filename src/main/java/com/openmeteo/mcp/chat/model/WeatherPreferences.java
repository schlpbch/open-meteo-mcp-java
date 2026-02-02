package com.openmeteo.mcp.chat.model;

/**
 * User preferences for weather information.
 * 
 * @param temperatureUnit Preferred temperature unit (celsius or fahrenheit)
 * @param windSpeedUnit Preferred wind speed unit (kmh, ms, mph, kn)
 * @param precipitationUnit Preferred precipitation unit (mm or inch)
 * @param timezone Preferred timezone for weather data
 * 
 * @since 1.2.0
 */
public record WeatherPreferences(
    String temperatureUnit,
    String windSpeedUnit,
    String precipitationUnit,
    String timezone
) {
    /**
     * Default preferences (metric, UTC)
     */
    public static final WeatherPreferences DEFAULT = new WeatherPreferences(
        "celsius",
        "kmh",
        "mm",
        "UTC"
    );
    
    /**
     * Compact constructor with defaults
     */
    public WeatherPreferences {
        if (temperatureUnit == null || temperatureUnit.isBlank()) {
            temperatureUnit = "celsius";
        }
        if (windSpeedUnit == null || windSpeedUnit.isBlank()) {
            windSpeedUnit = "kmh";
        }
        if (precipitationUnit == null || precipitationUnit.isBlank()) {
            precipitationUnit = "mm";
        }
        if (timezone == null || timezone.isBlank()) {
            timezone = "UTC";
        }
    }
}
