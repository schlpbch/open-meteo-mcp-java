package com.openmeteo.mcp.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for calculating outdoor activity comfort index.
 * <p>
 * Combines weather, air quality, UV, and precipitation factors into a single
 * comfort score (0-100) for planning outdoor activities.
 * </p>
 */
public class ComfortIndexCalculator {

    /**
     * Calculate outdoor activity comfort index (0-100).
     *
     * @param weather    Current weather conditions
     * @param airQuality Current air quality data
     * @return Map containing overall comfort index, factor breakdown, and recommendation
     */
    public static Map<String, Object> calculateComfortIndex(
            Map<String, Object> weather,
            Map<String, Object> airQuality) {

        // Extract weather data
        double temperature = getDoubleValue(weather, "temperature", 15.0);
        double windSpeed = getDoubleValue(weather, "windspeed", 0.0);
        int weatherCode = getIntValue(weather, "weathercode", 0);
        double humidity = getDoubleValue(weather, "humidity", 50.0);

        // Extract air quality data
        double aqi = getDoubleValue(airQuality, "european_aqi", 20.0);
        double pm25 = getDoubleValue(airQuality, "pm2_5", 10.0);
        double uvIndex = getDoubleValue(airQuality, "uv_index", 3.0);

        // Calculate individual factor scores (0-100)
        double thermalComfort = calculateThermalComfort(temperature, humidity, windSpeed);
        double airQualityScore = calculateAirQualityScore(aqi, pm25);
        double precipitationScore = calculatePrecipitationScore(weatherCode);
        double uvScore = calculateUVScore(uvIndex);

        // Weighted overall score
        double overall = (thermalComfort * 0.35) +
                (airQualityScore * 0.25) +
                (precipitationScore * 0.25) +
                (uvScore * 0.15);

        // Create factor breakdown
        Map<String, Double> factors = new HashMap<>();
        factors.put("thermal_comfort", thermalComfort);
        factors.put("air_quality", airQualityScore);
        factors.put("precipitation_risk", precipitationScore);
        factors.put("uv_safety", uvScore);

        // Generate recommendation
        String recommendation = generateRecommendation(overall);

        // Build result
        Map<String, Object> result = new HashMap<>();
        result.put("overall", Math.round(overall));
        result.put("factors", factors);
        result.put("recommendation", recommendation);

        return result;
    }

    /**
     * Calculate thermal comfort score based on temperature, humidity, and wind.
     */
    private static double calculateThermalComfort(double temp, double humidity, double windSpeed) {
        // Ideal temperature range: 15-25°C
        double tempScore;
        if (temp >= 15 && temp <= 25) {
            tempScore = 100;
        } else if (temp >= 10 && temp < 15) {
            tempScore = 80 - ((15 - temp) * 4);
        } else if (temp > 25 && temp <= 30) {
            tempScore = 80 - ((temp - 25) * 4);
        } else if (temp < 10) {
            tempScore = Math.max(0, 60 - ((10 - temp) * 5));
        } else {
            tempScore = Math.max(0, 60 - ((temp - 30) * 5));
        }

        // Adjust for humidity (ideal: 40-60%)
        if (humidity < 30 || humidity > 70) {
            tempScore *= 0.9;
        }

        // Adjust for wind (high wind reduces comfort)
        if (windSpeed > 30) {
            tempScore *= 0.8;
        } else if (windSpeed > 20) {
            tempScore *= 0.9;
        }

        return Math.max(0, Math.min(100, tempScore));
    }

    /**
     * Calculate air quality score.
     */
    private static double calculateAirQualityScore(double aqi, double pm25) {
        // European AQI scale: 0-20 (Good), 20-40 (Fair), 40-60 (Moderate), 60-80 (Poor), 80-100 (Very Poor), 100+ (Extremely Poor)
        double score;
        if (aqi <= 20) {
            score = 100;
        } else if (aqi <= 40) {
            score = 80;
        } else if (aqi <= 60) {
            score = 60;
        } else if (aqi <= 80) {
            score = 40;
        } else if (aqi <= 100) {
            score = 20;
        } else {
            score = 10;
        }

        // Additional penalty for high PM2.5
        if (pm25 > 35) {
            score *= 0.7;
        } else if (pm25 > 25) {
            score *= 0.85;
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * Calculate precipitation score based on weather code.
     */
    private static double calculatePrecipitationScore(int weatherCode) {
        // Clear/Partly cloudy: 0-3
        if (weatherCode <= 3) {
            return 100;
        }
        // Fog/Mist: 45-48
        if (weatherCode >= 45 && weatherCode <= 48) {
            return 70;
        }
        // Light rain/drizzle: 51-61
        if (weatherCode >= 51 && weatherCode <= 61) {
            return 50;
        }
        // Moderate rain: 63-65
        if (weatherCode >= 63 && weatherCode <= 65) {
            return 30;
        }
        // Heavy rain/snow: 66+
        if (weatherCode >= 66 && weatherCode < 95) {
            return 20;
        }
        // Thunderstorms: 95-99
        if (weatherCode >= 95) {
            return 0;
        }
        return 80; // Default for other codes
    }

    /**
     * Calculate UV safety score.
     */
    private static double calculateUVScore(double uvIndex) {
        // UV Index: 0-2 (Low), 3-5 (Moderate), 6-7 (High), 8-10 (Very High), 11+ (Extreme)
        if (uvIndex <= 2) {
            return 100;
        } else if (uvIndex <= 5) {
            return 80;
        } else if (uvIndex <= 7) {
            return 60;
        } else if (uvIndex <= 10) {
            return 40;
        } else {
            return 20;
        }
    }

    /**
     * Generate recommendation based on overall score.
     */
    private static String generateRecommendation(double score) {
        if (score >= 80) {
            return "Perfect for outdoor activities! Conditions are ideal.";
        } else if (score >= 60) {
            return "Good conditions for outdoor activities. Minor adjustments may be needed.";
        } else if (score >= 40) {
            return "Fair conditions. Plan accordingly and be prepared for less-than-ideal weather.";
        } else if (score >= 20) {
            return "Poor conditions. Consider indoor alternatives or postpone outdoor plans.";
        } else {
            return "Very poor conditions. Outdoor activities not recommended.";
        }
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
