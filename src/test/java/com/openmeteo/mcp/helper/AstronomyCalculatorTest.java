package com.openmeteo.mcp.helper;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AstronomyCalculator.
 * Tests verify the astronomy calculation logic.
 */
class AstronomyCalculatorTest {

    @Test
    void testCalculateAstronomyData_Zurich() {
        double latitude = 47.3769;
        double longitude = 8.5417;
        String timezone = "Europe/Zurich";

        Map<String, Object> result = AstronomyCalculator.calculateAstronomyData(latitude, longitude, timezone);

        assertNotNull(result);
        assertTrue(result.containsKey("sunrise"));
        assertTrue(result.containsKey("sunset"));
        assertTrue(result.containsKey("day_length_hours"));
        assertTrue(result.containsKey("moon_phase"));
    }

    @Test
    void testCalculateAstronomyData_ValidDayLength() {
        double latitude = 47.3769;
        double longitude = 8.5417;
        String timezone = "Europe/Zurich";

        Map<String, Object> result = AstronomyCalculator.calculateAstronomyData(latitude, longitude, timezone);

        double dayLength = ((Number) result.get("day_length_hours")).doubleValue();
        
        // Day length should be between 0 and 24 hours
        assertTrue(dayLength >= 0 && dayLength <= 24, 
                "Day length should be between 0 and 24 hours, got: " + dayLength);
    }

    @Test
    void testCalculateAstronomyData_MoonPhase() {
        double latitude = 47.3769;
        double longitude = 8.5417;
        String timezone = "Europe/Zurich";

        Map<String, Object> result = AstronomyCalculator.calculateAstronomyData(latitude, longitude, timezone);

        String moonPhase = (String) result.get("moon_phase");
        
        assertNotNull(moonPhase);
        assertFalse(moonPhase.isEmpty());
    }

    @Test
    void testCalculateAstronomyData_DifferentLocations() {
        // Test multiple locations to ensure calculations work globally
        double[][] locations = {
                {47.3769, 8.5417},   // Zurich
                {46.9480, 7.4474},   // Bern
                {46.2044, 6.1432}    // Geneva
        };

        for (double[] location : locations) {
            Map<String, Object> result = AstronomyCalculator.calculateAstronomyData(
                    location[0], location[1], "UTC"
            );

            assertNotNull(result, "Result should not be null for location: " + location[0] + ", " + location[1]);
            assertTrue(result.containsKey("sunrise"));
            assertTrue(result.containsKey("sunset"));
        }
    }
}
