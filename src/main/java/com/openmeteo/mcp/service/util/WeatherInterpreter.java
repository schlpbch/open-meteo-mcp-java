package com.openmeteo.mcp.service.util;

/**
 * Interprets WMO weather codes (0-99) to human-readable information.
 * <p>
 * Provides categorization and severity assessment for weather conditions.
 * </p>
 */
public class WeatherInterpreter {

    /**
     * Weather code interpretation result.
     *
     * @param code        WMO weather code
     * @param description Human-readable description
     * @param category    Weather category (Clear, Cloudy, Rain, etc.)
     * @param severity    Severity level (none, low, medium, high, extreme)
     */
    public record WeatherCodeInfo(
            int code,
            String description,
            String category,
            String severity
    ) {
    }

    /**
     * Interprets a WMO weather code to detailed information.
     *
     * @param code WMO weather code (0-99)
     * @return WeatherCodeInfo with description, category, and severity
     */
    public static WeatherCodeInfo interpretWeatherCode(int code) {
        return switch (code) {
            case 0 -> new WeatherCodeInfo(0, "Clear sky", "Clear", "none");
            case 1 -> new WeatherCodeInfo(1, "Mainly clear", "Cloudy", "none");
            case 2 -> new WeatherCodeInfo(2, "Partly cloudy", "Cloudy", "none");
            case 3 -> new WeatherCodeInfo(3, "Overcast", "Cloudy", "none");
            case 45 -> new WeatherCodeInfo(45, "Fog", "Fog", "low");
            case 48 -> new WeatherCodeInfo(48, "Depositing rime fog", "Fog", "low");
            case 51 -> new WeatherCodeInfo(51, "Light drizzle", "Drizzle", "low");
            case 53 -> new WeatherCodeInfo(53, "Moderate drizzle", "Drizzle", "low");
            case 55 -> new WeatherCodeInfo(55, "Dense drizzle", "Drizzle", "low");
            case 56 -> new WeatherCodeInfo(56, "Light freezing drizzle", "Drizzle", "medium");
            case 57 -> new WeatherCodeInfo(57, "Dense freezing drizzle", "Drizzle", "medium");
            case 61 -> new WeatherCodeInfo(61, "Slight rain", "Rain", "low");
            case 63 -> new WeatherCodeInfo(63, "Moderate rain", "Rain", "medium");
            case 65 -> new WeatherCodeInfo(65, "Heavy rain", "Rain", "medium");
            case 66 -> new WeatherCodeInfo(66, "Light freezing rain", "Rain", "high");
            case 67 -> new WeatherCodeInfo(67, "Heavy freezing rain", "Rain", "high");
            case 71 -> new WeatherCodeInfo(71, "Slight snowfall", "Snow", "low");
            case 73 -> new WeatherCodeInfo(73, "Moderate snowfall", "Snow", "medium");
            case 75 -> new WeatherCodeInfo(75, "Heavy snowfall", "Snow", "high");
            case 77 -> new WeatherCodeInfo(77, "Snow grains", "Snow", "medium");
            case 80 -> new WeatherCodeInfo(80, "Slight rain showers", "Rain", "low");
            case 81 -> new WeatherCodeInfo(81, "Moderate rain showers", "Rain", "medium");
            case 82 -> new WeatherCodeInfo(82, "Violent rain showers", "Rain", "high");
            case 85 -> new WeatherCodeInfo(85, "Slight snow showers", "Snow", "medium");
            case 86 -> new WeatherCodeInfo(86, "Heavy snow showers", "Snow", "high");
            case 95 -> new WeatherCodeInfo(95, "Thunderstorm", "Thunderstorm", "high");
            case 96 -> new WeatherCodeInfo(96, "Thunderstorm with slight hail", "Thunderstorm", "high");
            case 99 -> new WeatherCodeInfo(99, "Thunderstorm with heavy hail", "Thunderstorm", "extreme");
            default -> new WeatherCodeInfo(code, "Unknown", "Unknown", "none");
        };
    }

    /**
     * Gets the weather category for a given code.
     *
     * @param code WMO weather code
     * @return Weather category string
     */
    public static String getWeatherCategory(int code) {
        return interpretWeatherCode(code).category();
    }

    /**
     * Assesses the impact of weather on travel and outdoor activities.
     *
     * @param code WMO weather code
     * @return Impact level (none, minor, moderate, significant, severe)
     */
    public static String getTravelImpact(int code) {
        String severity = interpretWeatherCode(code).severity();
        return switch (severity) {
            case "none" -> "none";
            case "low" -> "minor";
            case "medium" -> "moderate";
            case "high" -> "significant";
            case "extreme" -> "severe";
            default -> "unknown";
        };
    }
}
