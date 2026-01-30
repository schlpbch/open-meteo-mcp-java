package com.openmeteo.mcp.model.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DailyWeather DTO record.
 */
class DailyWeatherTest {

    @Test
    void shouldCreateDailyWeatherWithAllFields() {
        // Arrange
        var dates = List.of("2024-01-30", "2024-01-31");
        var tempMax = List.of(15.0, 16.0);
        var tempMin = List.of(5.0, 6.0);
        var apparentMax = List.of(14.0, 15.0);
        var apparentMin = List.of(4.0, 5.0);
        var precipSum = List.of(0.0, 2.5);
        var rainSum = List.of(0.0, 2.5);
        var snowSum = List.of(0.0, 0.0);
        var codes = List.of(0, 61);
        var sunrises = List.of("2024-01-30T07:15Z", "2024-01-31T07:14Z");
        var sunsets = List.of("2024-01-30T17:30Z", "2024-01-31T17:31Z");
        var windSpeed = List.of(10.0, 12.0);
        var windGusts = List.of(20.0, 22.0);
        var windDir = List.of(180, 185);
        var precipProb = List.of(0, 40);
        var uvIndexMax = List.of(1.0, 1.5);

        // Act
        var weather = new DailyWeather(dates, tempMax, tempMin, apparentMax, apparentMin,
                precipSum, rainSum, snowSum, codes, sunrises, sunsets, windSpeed, windGusts,
                windDir, precipProb, uvIndexMax);

        // Assert
        assertThat(weather).isNotNull();
        assertThat(weather.time()).isEqualTo(dates);
        assertThat(weather.temperature2mMax()).isEqualTo(tempMax);
        assertThat(weather.temperature2mMin()).isEqualTo(tempMin);
    }

    @Test
    void shouldCreateDailyWeatherWithNullFields() {
        // Arrange & Act
        var weather = new DailyWeather(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(weather).isNotNull();
        assertThat(weather.time()).isNull();
    }

    @Test
    void shouldSupportRecordEquality() {
        // Arrange
        var dates = List.of("2024-01-30");
        var weather1 = new DailyWeather(dates, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
        var weather2 = new DailyWeather(dates, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(weather1).isEqualTo(weather2);
    }

    @Test
    void shouldHandleEmptyLists() {
        // Arrange
        List<String> emptyStrings = List.of();
        List<Double> emptyDoubles = List.of();
        List<Integer> emptyInts = List.of();
        var weather = new DailyWeather(emptyStrings, emptyDoubles, emptyDoubles, emptyDoubles, emptyDoubles,
                emptyDoubles, emptyDoubles, emptyDoubles, emptyInts, emptyStrings, emptyStrings, emptyDoubles,
                emptyDoubles, emptyInts, emptyInts, emptyDoubles);

        // Assert
        assertThat(weather.time()).isEmpty();
        assertThat(weather.temperature2mMax()).isEmpty();
    }

    @Test
    void shouldHandleWeekForecast() {
        // Arrange
        var dates = List.of("2024-01-30", "2024-01-31", "2024-02-01", "2024-02-02",
                "2024-02-03", "2024-02-04", "2024-02-05");
        var tempMax = List.of(15.0, 16.0, 14.0, 13.0, 12.0, 11.0, 10.0);
        var tempMin = List.of(5.0, 6.0, 4.0, 3.0, 2.0, 1.0, 0.0);
        var weather = new DailyWeather(dates, tempMax, tempMin, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(weather.time()).hasSize(7);
        assertThat(weather.temperature2mMax()).hasSize(7);
    }

    @Test
    void shouldBeSerializable() {
        // Arrange
        var weather = new DailyWeather(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(weather).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void shouldSupportToString() {
        // Arrange
        var dates = List.of("2024-01-30");
        var weather = new DailyWeather(dates, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Act
        String result = weather.toString();

        // Assert
        assertThat(result).contains("DailyWeather");
        assertThat(result).contains("time");
    }

    @Test
    void shouldHandleTemperatureRanges() {
        // Arrange
        var dates = List.of("2024-01-30", "2024-01-31");
        var tempMax = List.of(-10.0, 30.0);
        var tempMin = List.of(-20.0, 20.0);
        var weather = new DailyWeather(dates, tempMax, tempMin, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(weather.temperature2mMax()).contains(-10.0, 30.0);
        assertThat(weather.temperature2mMin()).contains(-20.0, 20.0);
    }

    @Test
    void shouldHandlePrecipitationSum() {
        // Arrange
        var dates = List.of("2024-01-30", "2024-01-31");
        var precipSum = List.of(0.0, 25.5);
        var weather = new DailyWeather(dates, null, null, null, null, precipSum, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(weather.precipitationSum()).contains(0.0, 25.5);
    }

    @Test
    void shouldHandleSunriseSunset() {
        // Arrange
        var dates = List.of("2024-01-30");
        var sunrise = List.of("2024-01-30T07:15Z");
        var sunset = List.of("2024-01-30T17:30Z");
        var weather = new DailyWeather(dates, null, null, null, null, null, null, null,
                null, sunrise, sunset, null, null, null, null, null);

        // Assert
        assertThat(weather.sunrise()).contains("2024-01-30T07:15Z");
        assertThat(weather.sunset()).contains("2024-01-30T17:30Z");
    }

    @Test
    void shouldHandleWeatherCodes() {
        // Arrange
        var dates = List.of("2024-01-30", "2024-01-31");
        var codes = List.of(0, 61); // 0=Clear, 61=Slight rain
        var weather = new DailyWeather(dates, null, null, null, null, null, null, null,
                codes, null, null, null, null, null, null, null);

        // Assert
        assertThat(weather.weathercode()).contains(0, 61);
    }
}
