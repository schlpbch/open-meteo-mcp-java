package com.openmeteo.mcp.service.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for WeatherFormatter.
 */
class WeatherFormatterTest {

    @Test
    void shouldFormatTemperature() {
        assertThat(WeatherFormatter.formatTemperature(15.5))
                .isEqualTo("15.5°C");
        assertThat(WeatherFormatter.formatTemperature(-10.2))
                .isEqualTo("-10.2°C");
        assertThat(WeatherFormatter.formatTemperature(0.0))
                .isEqualTo("0.0°C");
    }

    @Test
    void shouldFormatNoPrecipitation() {
        assertThat(WeatherFormatter.formatPrecipitation(0))
                .isEqualTo("No precipitation");
    }

    @Test
    void shouldFormatLightPrecipitation() {
        assertThat(WeatherFormatter.formatPrecipitation(0.5))
                .isEqualTo("Light (0.5 mm)");
        assertThat(WeatherFormatter.formatPrecipitation(0.9))
                .isEqualTo("Light (0.9 mm)");
    }

    @Test
    void shouldFormatModeratePrecipitation() {
        assertThat(WeatherFormatter.formatPrecipitation(3.0))
                .isEqualTo("Moderate (3.0 mm)");
        assertThat(WeatherFormatter.formatPrecipitation(4.5))
                .isEqualTo("Moderate (4.5 mm)");
    }

    @Test
    void shouldFormatHeavyPrecipitation() {
        assertThat(WeatherFormatter.formatPrecipitation(7.0))
                .isEqualTo("Heavy (7.0 mm)");
        assertThat(WeatherFormatter.formatPrecipitation(9.5))
                .isEqualTo("Heavy (9.5 mm)");
    }

    @Test
    void shouldFormatVeryHeavyPrecipitation() {
        assertThat(WeatherFormatter.formatPrecipitation(15.0))
                .isEqualTo("Very heavy (15.0 mm)");
        assertThat(WeatherFormatter.formatPrecipitation(50.0))
                .isEqualTo("Very heavy (50.0 mm)");
    }

    @Test
    void shouldFormatWind() {
        assertThat(WeatherFormatter.formatWind(10.0, 0))
                .isEqualTo("10 km/h N");
        assertThat(WeatherFormatter.formatWind(25.5, 90))
                .isEqualTo("26 km/h E");
        assertThat(WeatherFormatter.formatWind(15.0, 180))
                .isEqualTo("15 km/h S");
        assertThat(WeatherFormatter.formatWind(30.0, 270))
                .isEqualTo("30 km/h W");
    }

    @Test
    void shouldFormatWindWithIntermediateDirections() {
        assertThat(WeatherFormatter.formatWind(10.0, 45))
                .isEqualTo("10 km/h NE");
        assertThat(WeatherFormatter.formatWind(10.0, 135))
                .isEqualTo("10 km/h SE");
        assertThat(WeatherFormatter.formatWind(10.0, 225))
                .isEqualTo("10 km/h SW");
        assertThat(WeatherFormatter.formatWind(10.0, 315))
                .isEqualTo("10 km/h NW");
    }

    @Test
    void shouldInterpretEuropeanAqi() {
        assertThat(WeatherFormatter.interpretAqi(15, true))
                .isEqualTo("Good");
        assertThat(WeatherFormatter.interpretAqi(35, true))
                .isEqualTo("Fair");
        assertThat(WeatherFormatter.interpretAqi(55, true))
                .isEqualTo("Moderate");
        assertThat(WeatherFormatter.interpretAqi(75, true))
                .isEqualTo("Poor");
        assertThat(WeatherFormatter.interpretAqi(95, true))
                .isEqualTo("Very Poor");
        assertThat(WeatherFormatter.interpretAqi(120, true))
                .isEqualTo("Extremely Poor");
    }

    @Test
    void shouldInterpretUsAqi() {
        assertThat(WeatherFormatter.interpretAqi(40, false))
                .isEqualTo("Good");
        assertThat(WeatherFormatter.interpretAqi(75, false))
                .isEqualTo("Moderate");
        assertThat(WeatherFormatter.interpretAqi(120, false))
                .isEqualTo("Unhealthy for Sensitive Groups");
        assertThat(WeatherFormatter.interpretAqi(180, false))
                .isEqualTo("Unhealthy");
        assertThat(WeatherFormatter.interpretAqi(250, false))
                .isEqualTo("Very Unhealthy");
        assertThat(WeatherFormatter.interpretAqi(400, false))
                .isEqualTo("Hazardous");
    }

    @Test
    void shouldInterpretUvIndex() {
        assertThat(WeatherFormatter.interpretUvIndex(2.0))
                .isEqualTo("Low");
        assertThat(WeatherFormatter.interpretUvIndex(4.5))
                .isEqualTo("Moderate");
        assertThat(WeatherFormatter.interpretUvIndex(7.0))
                .isEqualTo("High");
        assertThat(WeatherFormatter.interpretUvIndex(9.5))
                .isEqualTo("Very High");
        assertThat(WeatherFormatter.interpretUvIndex(11.0))
                .isEqualTo("Extreme");
    }
}
