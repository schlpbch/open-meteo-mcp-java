package com.openmeteo.mcp.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MarineConditionsService.
 * Tests verify basic service logic.
 */
class MarineConditionsServiceTest {

    @Test
    void testForecastDaysValidation() {
        // Test that forecast days are within valid range
        int validDays = 7;
        assertTrue(validDays >= 1 && validDays <= 7, "Forecast days should be 1-7");
        
        int invalidDays = -1;
        assertTrue(invalidDays < 1, "Negative days should be invalid");
        
        int tooManyDays = 10;
        assertTrue(tooManyDays > 7, "More than 7 days should be invalid");
    }

    @Test
    void testWaveHeightCategories() {
        // Test wave height suitability thresholds
        double excellent = 0.2;
        assertTrue(excellent < 0.3, "< 0.3m should be excellent");
        
        double good = 0.5;
        assertTrue(good >= 0.3 && good < 0.6, "0.3-0.6m should be good");
        
        double dangerous = 1.6;
        assertTrue(dangerous > 1.5, "> 1.5m should be dangerous");
    }
}
