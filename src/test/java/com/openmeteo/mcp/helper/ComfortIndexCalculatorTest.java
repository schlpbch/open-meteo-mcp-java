package com.openmeteo.mcp.helper;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ComfortIndexCalculator.
 * Tests verify the comfort index calculation logic.
 */
class ComfortIndexCalculatorTest {

    @Test
    void testCalculateComfortIndex_Perfect() {
        Map<String, Object> weather = Map.of(
                "temperature", 20.0,
                "humidity", 50.0,
                "windspeed", 5.0,
                "weathercode", 0
        );

        Map<String, Object> airQuality = Map.of(
                "european_aqi", 15.0,
                "pm2_5", 8.0,
                "uv_index", 3.0
        );

        Map<String, Object> result = ComfortIndexCalculator.calculateComfortIndex(weather, airQuality);

        assertNotNull(result);
        assertTrue(result.containsKey("overall"));
        assertTrue(result.containsKey("recommendation"));
        assertTrue(result.containsKey("factors"));
        
        long overall = (Long) result.get("overall");
        assertTrue(overall >= 75, "Perfect conditions should score >= 75, got: " + overall);
    }

    @Test
    void testCalculateComfortIndex_Structure() {
        Map<String, Object> weather = Map.of(
                "temperature", 18.0,
                "humidity", 60.0,
                "windspeed", 10.0,
                "weathercode", 1
        );

        Map<String, Object> airQuality = Map.of(
                "european_aqi", 25.0,
                "pm2_5", 12.0,
                "uv_index", 4.0
        );

        Map<String, Object> result = ComfortIndexCalculator.calculateComfortIndex(weather, airQuality);

        // Verify structure
        assertTrue(result.containsKey("overall"));
        assertTrue(result.containsKey("factors"));
        assertTrue(result.containsKey("recommendation"));
        
        @SuppressWarnings("unchecked")
        Map<String, Double> factors = (Map<String, Double>) result.get("factors");
        
        assertTrue(factors.containsKey("thermal_comfort"));
        assertTrue(factors.containsKey("air_quality"));
        assertTrue(factors.containsKey("precipitation_risk"));
        assertTrue(factors.containsKey("uv_safety"));
    }

    @Test
    void testCalculateComfortIndex_EmptyData() {
        Map<String, Object> weather = Map.of();
        Map<String, Object> airQuality = Map.of();

        Map<String, Object> result = ComfortIndexCalculator.calculateComfortIndex(weather, airQuality);

        assertNotNull(result);
        assertTrue(result.containsKey("overall"));
        assertTrue(result.containsKey("recommendation"));
    }

    @Test
    void testCalculateComfortIndex_RecommendationText() {
        Map<String, Object> weather = Map.of(
                "temperature", 22.0,
                "humidity", 50.0,
                "windspeed", 5.0,
                "weathercode", 0
        );

        Map<String, Object> airQuality = Map.of(
                "european_aqi", 10.0,
                "pm2_5", 5.0,
                "uv_index", 3.0
        );

        Map<String, Object> result = ComfortIndexCalculator.calculateComfortIndex(weather, airQuality);

        String recommendation = (String) result.get("recommendation");
        assertNotNull(recommendation);
        assertFalse(recommendation.isEmpty());
    }
}
