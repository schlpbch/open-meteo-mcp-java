package com.openmeteo.mcp.service.util;

/**
 * Formats weather data for display.
 * <p>
 * Provides human-readable formatting for temperature, precipitation, wind,
 * AQI, and UV index values.
 * </p>
 */
public class WeatherFormatter {

    /**
     * Formats temperature with units.
     *
     * @param celsius Temperature in Celsius
     * @return Formatted temperature string (e.g., "15.5°C")
     */
    public static String formatTemperature(double celsius) {
        return String.format("%.1f°C", celsius);
    }

    /**
     * Formats precipitation with descriptive label.
     *
     * @param mm Precipitation in millimeters
     * @return Formatted precipitation string
     */
    public static String formatPrecipitation(double mm) {
        if (mm == 0) {
            return "No precipitation";
        } else if (mm < 1) {
            return String.format("Light (%.1f mm)", mm);
        } else if (mm < 5) {
            return String.format("Moderate (%.1f mm)", mm);
        } else if (mm < 10) {
            return String.format("Heavy (%.1f mm)", mm);
        } else {
            return String.format("Very heavy (%.1f mm)", mm);
        }
    }

    /**
     * Formats wind speed and direction.
     *
     * @param speedKmh  Wind speed in km/h
     * @param direction Wind direction in degrees (0-360)
     * @return Formatted wind string (e.g., "10 km/h NE")
     */
    public static String formatWind(double speedKmh, int direction) {
        String directionStr = formatWindDirection(direction);
        return String.format("%.0f km/h %s", speedKmh, directionStr);
    }

    /**
     * Converts wind direction in degrees to compass direction.
     *
     * @param degrees Wind direction in degrees (0-360)
     * @return Compass direction (N, NE, E, SE, S, SW, W, NW)
     */
    private static String formatWindDirection(int degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) Math.round(((degrees % 360) / 45.0)) % 8;
        return directions[index];
    }

    /**
     * Interprets AQI value to quality level.
     *
     * @param aqi        AQI value
     * @param isEuropean True for European AQI scale, false for US AQI scale
     * @return Air quality level description
     */
    public static String interpretAqi(int aqi, boolean isEuropean) {
        if (isEuropean) {
            // European AQI scale (0-100+)
            if (aqi <= 20) return "Good";
            if (aqi <= 40) return "Fair";
            if (aqi <= 60) return "Moderate";
            if (aqi <= 80) return "Poor";
            if (aqi <= 100) return "Very Poor";
            return "Extremely Poor";
        } else {
            // US AQI scale (0-500)
            if (aqi <= 50) return "Good";
            if (aqi <= 100) return "Moderate";
            if (aqi <= 150) return "Unhealthy for Sensitive Groups";
            if (aqi <= 200) return "Unhealthy";
            if (aqi <= 300) return "Very Unhealthy";
            return "Hazardous";
        }
    }

    /**
     * Interprets UV index to exposure level.
     *
     * @param uvIndex UV index value
     * @return UV exposure level (Low, Moderate, High, Very High, Extreme)
     */
    public static String interpretUvIndex(double uvIndex) {
        if (uvIndex < 3) return "Low";
        if (uvIndex < 6) return "Moderate";
        if (uvIndex < 8) return "High";
        if (uvIndex < 11) return "Very High";
        return "Extreme";
    }
}
