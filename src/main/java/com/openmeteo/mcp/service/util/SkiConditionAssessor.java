package com.openmeteo.mcp.service.util;

/**
 * Assesses ski conditions based on snow depth, temperature, and weather.
 * <p>
 * Provides ski condition ratings and wind chill calculations.
 * </p>
 */
public class SkiConditionAssessor {

    /**
     * Assesses ski conditions based on current conditions.
     *
     * @param snowDepth       Snow depth in meters
     * @param recentSnowfall  Recent snowfall in cm (last 24h)
     * @param temperature     Temperature in Celsius
     * @param weatherCode     WMO weather code
     * @return Ski condition assessment (Excellent, Good, Fair, Poor)
     */
    public static String assessSkiConditions(
            double snowDepth,
            double recentSnowfall,
            double temperature,
            int weatherCode
    ) {
        // Excellent: Fresh powder, ideal temps, clear skies
        if (recentSnowfall > 10 &&
                temperature >= -15 && temperature <= -5 &&
                (weatherCode >= 0 && weatherCode <= 2)) {
            return "Excellent";
        }

        // Good: Good depth, stable temps, mostly clear
        if (snowDepth > 0.5 &&
                temperature >= -10 && temperature <= 0 &&
                (weatherCode >= 0 && weatherCode <= 3)) {
            return "Good";
        }

        // Fair: Minimal depth, temps below freezing
        if (snowDepth > 0.2 && temperature < 5) {
            return "Fair";
        }

        return "Poor";
    }

    /**
     * Calculates wind chill using the North American wind chill formula.
     * <p>
     * Wind chill is only calculated for wind speeds >= 4.8 km/h.
     * For lower speeds, the actual temperature is returned.
     * </p>
     *
     * @param tempCelsius  Temperature in Celsius
     * @param windSpeedKmh Wind speed in km/h
     * @return Wind chill temperature in Celsius
     */
    public static double calculateWindChill(double tempCelsius, double windSpeedKmh) {
        // No wind chill at low speeds
        if (windSpeedKmh < 4.8) {
            return tempCelsius;
        }

        // Convert to Fahrenheit for formula
        double tempF = tempCelsius * 9.0 / 5.0 + 32.0;
        double windMph = windSpeedKmh * 0.621371;

        // North American wind chill formula
        double windChillF = 35.74 + (0.6215 * tempF)
                - (35.75 * Math.pow(windMph, 0.16))
                + (0.4275 * tempF * Math.pow(windMph, 0.16));

        // Convert back to Celsius
        return (windChillF - 32.0) * 5.0 / 9.0;
    }
}
