package com.openmeteo.mcp.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for generating weather alerts based on thresholds.
 * <p>
 * Analyzes weather data and generates actionable alerts for severe conditions.
 * </p>
 */
public class WeatherAlertGenerator {

    /**
     * Generate weather alerts based on current and forecast data.
     *
     * @param current  Current weather conditions
     * @param hourly   Hourly forecast data
     * @param daily    Daily forecast data
     * @param timezone Timezone for the location
     * @return List of alert objects with type, severity, timing, and recommendations
     */
    public static List<Map<String, Object>> generateWeatherAlerts(
            Map<String, Object> current,
            Map<String, Object> hourly,
            Map<String, Object> daily,
            String timezone) {

        List<Map<String, Object>> alerts = new ArrayList<>();

        // Extract current conditions
        double currentTemp = getDoubleValue(current, "temperature", 15.0);
        double currentWindSpeed = getDoubleValue(current, "windspeed", 0.0);
        int currentWeatherCode = getIntValue(current, "weathercode", 0);

        // Check for heat warnings (temperature > 30°C for 3+ hours)
        if (hourly != null && hourly.containsKey("temperature")) {
            List<?> temps = (List<?>) hourly.get("temperature");
            int consecutiveHotHours = 0;
            for (Object temp : temps) {
                if (temp instanceof Number && ((Number) temp).doubleValue() > 30.0) {
                    consecutiveHotHours++;
                    if (consecutiveHotHours >= 3) {
                        alerts.add(createAlert(
                                "HEAT_WARNING",
                                "Warning",
                                "High temperatures expected",
                                "Temperature above 30°C for 3+ hours",
                                List.of(
                                        "Stay hydrated",
                                        "Avoid strenuous outdoor activities during peak heat",
                                        "Seek shade and air conditioning"
                                )
                        ));
                        break;
                    }
                } else {
                    consecutiveHotHours = 0;
                }
            }
        }

        // Check for cold warnings (temperature < -10°C)
        if (currentTemp < -10.0) {
            alerts.add(createAlert(
                    "COLD_WARNING",
                    "Warning",
                    "Extreme cold conditions",
                    String.format("Temperature at %.1f°C", currentTemp),
                    List.of(
                            "Dress in warm layers",
                            "Limit time outdoors",
                            "Watch for frostbite and hypothermia"
                    )
            ));
        }

        // Check for storm warnings (wind gusts > 80 km/h or thunderstorms)
        if (currentWindSpeed > 80.0 || currentWeatherCode >= 95) {
            String alertType = currentWeatherCode >= 95 ? "THUNDERSTORM_WARNING" : "STORM_WARNING";
            String description = currentWeatherCode >= 95
                    ? "Thunderstorm conditions"
                    : String.format("High wind speeds (%.1f km/h)", currentWindSpeed);

            alerts.add(createAlert(
                    alertType,
                    "Warning",
                    "Severe weather conditions",
                    description,
                    List.of(
                            "Avoid outdoor activities",
                            "Secure loose objects",
                            "Stay indoors if possible"
                    )
            ));
        }

        // Check for wind advisories (gusts 50-80 km/h)
        if (currentWindSpeed >= 50.0 && currentWindSpeed < 80.0) {
            alerts.add(createAlert(
                    "WIND_ADVISORY",
                    "Advisory",
                    "Strong winds expected",
                    String.format("Wind speeds at %.1f km/h", currentWindSpeed),
                    List.of(
                            "Secure outdoor items",
                            "Use caution when driving",
                            "Be aware of falling branches"
                    )
            ));
        }

        // If no alerts, add an "all clear" message
        if (alerts.isEmpty()) {
            alerts.add(createAlert(
                    "ALL_CLEAR",
                    "Info",
                    "No weather alerts",
                    "Current conditions are within normal ranges",
                    List.of("Enjoy your day!")
            ));
        }

        return alerts;
    }

    /**
     * Create an alert object.
     */
    private static Map<String, Object> createAlert(
            String type,
            String severity,
            String title,
            String description,
            List<String> recommendations) {

        Map<String, Object> alert = new HashMap<>();
        alert.put("type", type);
        alert.put("severity", severity);
        alert.put("title", title);
        alert.put("description", description);
        alert.put("recommendations", recommendations);
        return alert;
    }

    /**
     * Safely extract double value from map.
     */
    private static double getDoubleValue(Map<String, Object> map, String key, double defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * Safely extract int value from map.
     */
    private static int getIntValue(Map<String, Object> map, String key, int defaultValue) {
        if (map == null || !map.containsKey(key)) {
            return defaultValue;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
}
