package com.openmeteo.mcp.helper;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeatherAlertGenerator.
 * Tests verify the alert generation logic matches the actual implementation.
 */
class WeatherAlertGeneratorTest {

    @Test
    void testGenerateWeatherAlerts_AllClear() {
        Map<String, Object> current = Map.of(
                "temperature", 20.0,
                "windspeed", 10.0,
                "weathercode", 0
        );

        Map<String, Object> hourly = Map.of(
                "temperature", List.of(20.0, 21.0, 22.0)
        );

        List<Map<String, Object>> alerts = WeatherAlertGenerator.generateWeatherAlerts(
                current, hourly, Map.of(), "Europe/Zurich"
        );

        assertNotNull(alerts);
        assertFalse(alerts.isEmpty(), "Should always return at least one alert");
        assertEquals("ALL_CLEAR", alerts.get(0).get("type"));
        assertEquals("Info", alerts.get(0).get("severity"));
    }

    @Test
    void testGenerateWeatherAlerts_HeatWarning() {
        Map<String, Object> current = Map.of(
                "temperature", 32.0,
                "windspeed", 10.0,
                "weathercode", 0
        );

        // 4 consecutive hours above 30°C
        Map<String, Object> hourly = Map.of(
                "temperature", List.of(32.0, 33.0, 34.0, 33.0)
        );

        List<Map<String, Object>> alerts = WeatherAlertGenerator.generateWeatherAlerts(
                current, hourly, Map.of(), "Europe/Zurich"
        );

        assertNotNull(alerts);
        assertFalse(alerts.isEmpty());
        assertEquals("HEAT_WARNING", alerts.get(0).get("type"));
        assertEquals("Warning", alerts.get(0).get("severity"));
        assertTrue(alerts.get(0).containsKey("recommendations"));
    }

    @Test
    void testGenerateWeatherAlerts_ColdWarning() {
        Map<String, Object> current = Map.of(
                "temperature", -15.0,
                "windspeed", 10.0,
                "weathercode", 0
        );

        List<Map<String, Object>> alerts = WeatherAlertGenerator.generateWeatherAlerts(
                current, Map.of(), Map.of(), "Europe/Zurich"
        );

        assertNotNull(alerts);
        assertFalse(alerts.isEmpty());
        assertEquals("COLD_WARNING", alerts.get(0).get("type"));
        assertEquals("Warning", alerts.get(0).get("severity"));
    }

    @Test
    void testGenerateWeatherAlerts_StormWarning() {
        Map<String, Object> current = Map.of(
                "temperature", 20.0,
                "windspeed", 85.0,  // > 80 km/h
                "weathercode", 0
        );

        List<Map<String, Object>> alerts = WeatherAlertGenerator.generateWeatherAlerts(
                current, Map.of(), Map.of(), "Europe/Zurich"
        );

        assertNotNull(alerts);
        assertFalse(alerts.isEmpty());
        assertEquals("STORM_WARNING", alerts.get(0).get("type"));
        assertEquals("Warning", alerts.get(0).get("severity"));
    }

    @Test
    void testGenerateWeatherAlerts_ThunderstormWarning() {
        Map<String, Object> current = Map.of(
                "temperature", 20.0,
                "windspeed", 50.0,
                "weathercode", 95  // Thunderstorm code
        );

        List<Map<String, Object>> alerts = WeatherAlertGenerator.generateWeatherAlerts(
                current, Map.of(), Map.of(), "Europe/Zurich"
        );

        assertNotNull(alerts);
        assertFalse(alerts.isEmpty());
        assertEquals("THUNDERSTORM_WARNING", alerts.get(0).get("type"));
        assertEquals("Warning", alerts.get(0).get("severity"));
    }

    @Test
    void testGenerateWeatherAlerts_WindAdvisory() {
        Map<String, Object> current = Map.of(
                "temperature", 20.0,
                "windspeed", 65.0,  // 50-80 km/h
                "weathercode", 0
        );

        List<Map<String, Object>> alerts = WeatherAlertGenerator.generateWeatherAlerts(
                current, Map.of(), Map.of(), "Europe/Zurich"
        );

        assertNotNull(alerts);
        assertFalse(alerts.isEmpty());
        assertEquals("WIND_ADVISORY", alerts.get(0).get("type"));
        assertEquals("Advisory", alerts.get(0).get("severity"));
    }

    @Test
    void testGenerateWeatherAlerts_AlertStructure() {
        Map<String, Object> current = Map.of(
                "temperature", -15.0,
                "windspeed", 10.0,
                "weathercode", 0
        );

        List<Map<String, Object>> alerts = WeatherAlertGenerator.generateWeatherAlerts(
                current, Map.of(), Map.of(), "Europe/Zurich"
        );

        Map<String, Object> alert = alerts.get(0);
        
        assertTrue(alert.containsKey("type"));
        assertTrue(alert.containsKey("severity"));
        assertTrue(alert.containsKey("title"));
        assertTrue(alert.containsKey("description"));
        assertTrue(alert.containsKey("recommendations"));
        
        @SuppressWarnings("unchecked")
        List<String> recommendations = (List<String>) alert.get("recommendations");
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
    }
}
