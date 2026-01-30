package com.openmeteo.mcp.model.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HourlyWeather DTO record.
 */
class HourlyWeatherTest {

    @Test
    void shouldCreateHourlyWeatherWithAllFields() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z", "2024-01-30T01:00Z");
        var temps = List.of(10.5, 11.2);
        var apparent = List.of(9.0, 10.0);
        var precip = List.of(0.0, 0.5);
        var rain = List.of(0.0, 0.5);
        var snow = List.of(0.0, 0.0);
        var snowDepth = List.of(0.0, 0.0);
        var codes = List.of(0, 1);
        var cloudCover = List.of(10, 20);
        var visibility = List.of(10000.0, 9500.0);
        var windSpeed = List.of(5.0, 6.0);
        var windDir = List.of(180, 185);
        var windGusts = List.of(8.0, 9.0);
        var pressure = List.of(1013.0, 1012.0);
        var humidity = List.of(70, 75);
        var dewpoint = List.of(5.0, 6.0);
        var precipProb = List.of(0, 20);
        var uvIndex = List.of(0.0, 1.0);

        // Act
        var weather = new HourlyWeather(times, temps, apparent, precip, rain, snow, snowDepth,
                codes, cloudCover, visibility, windSpeed, windDir, windGusts, pressure,
                humidity, dewpoint, precipProb, uvIndex);

        // Assert
        assertThat(weather).isNotNull();
        assertThat(weather.time()).isEqualTo(times);
        assertThat(weather.temperature2m()).isEqualTo(temps);
        assertThat(weather.apparentTemperature()).isEqualTo(apparent);
        assertThat(weather.precipitation()).isEqualTo(precip);
    }

    @Test
    void shouldCreateHourlyWeatherWithNullFields() {
        // Arrange & Act
        var weather = new HourlyWeather(null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null);

        // Assert
        assertThat(weather).isNotNull();
        assertThat(weather.time()).isNull();
        assertThat(weather.temperature2m()).isNull();
    }

    @Test
    void shouldSupportRecordEquality() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z");
        var weather1 = new HourlyWeather(times, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null);
        var weather2 = new HourlyWeather(times, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null);

        // Assert
        assertThat(weather1).isEqualTo(weather2);
    }

    @Test
    void shouldHandleEmptyLists() {
        // Arrange
        List<String> times = List.of();
        List<Double> doubles = List.of();
        List<Integer> ints = List.of();
        var weather = new HourlyWeather(times, doubles, doubles, doubles, doubles, doubles, doubles,
                ints, ints, doubles, doubles, ints, doubles, doubles,
                ints, doubles, ints, doubles);

        // Assert
        assertThat(weather.time()).isEmpty();
        assertThat(weather.temperature2m()).isEmpty();
    }

    @Test
    void shouldHandleLargeLists() {
        // Arrange
        List<String> times = List.of("2024-01-30T00:00Z", "2024-01-30T01:00Z", "2024-01-30T02:00Z",
                "2024-01-30T03:00Z", "2024-01-30T04:00Z");
        List<Double> temps = List.of(10.0, 11.0, 12.0, 13.0, 14.0);
        var weather = new HourlyWeather(times, temps, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null);

        // Assert
        assertThat(weather.time()).hasSize(5);
        assertThat(weather.temperature2m()).hasSize(5);
    }

    @Test
    void shouldBeSerializable() {
        // Arrange
        var weather = new HourlyWeather(null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null);

        // Assert
        assertThat(weather).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void shouldSupportToString() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z");
        var weather = new HourlyWeather(times, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null);

        // Act
        String result = weather.toString();

        // Assert
        assertThat(result).contains("HourlyWeather");
        assertThat(result).contains("time");
    }

    @Test
    void shouldHandleTemperatureVariations() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z", "2024-01-30T01:00Z", "2024-01-30T02:00Z");
        var temps = List.of(-5.0, 0.0, 25.5);
        var weather = new HourlyWeather(times, temps, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null);

        // Assert
        assertThat(weather.temperature2m()).contains(-5.0, 0.0, 25.5);
    }

    @Test
    void shouldHandlePrecipitationValues() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z", "2024-01-30T01:00Z");
        var precip = List.of(0.0, 5.5);
        var weather = new HourlyWeather(times, null, null, precip, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null);

        // Assert
        assertThat(weather.precipitation()).contains(0.0, 5.5);
    }
}
