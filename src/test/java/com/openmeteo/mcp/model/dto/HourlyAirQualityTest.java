package com.openmeteo.mcp.model.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for HourlyAirQuality DTO record.
 */
class HourlyAirQualityTest {

    @Test
    void shouldCreateHourlyAirQualityWithAllFields() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z", "2024-01-30T01:00Z");
        var europeanAqi = List.of(10, 20);
        var usAqi = List.of(15, 25);
        var pm10 = List.of(20.0, 30.0);
        var pm25 = List.of(10.0, 15.0);
        var co = List.of(200.0, 250.0);
        var no2 = List.of(5.0, 10.0);
        var so2 = List.of(1.0, 2.0);
        var o3 = List.of(50.0, 60.0);
        var uv = List.of(0.0, 1.0);
        var alder = List.of(10.0, 15.0);
        var birch = List.of(5.0, 8.0);
        var grass = List.of(20.0, 25.0);
        var mugwort = List.of(2.0, 3.0);
        var olive = List.of(1.0, 1.5);
        var ragweed = List.of(0.5, 1.0);

        // Act
        var aq = new HourlyAirQuality(times, europeanAqi, usAqi, pm10, pm25, co, no2, so2,
                o3, uv, alder, birch, grass, mugwort, olive, ragweed);

        // Assert
        assertThat(aq).isNotNull();
        assertThat(aq.time()).isEqualTo(times);
        assertThat(aq.europeanAqi()).isEqualTo(europeanAqi);
        assertThat(aq.pm10()).isEqualTo(pm10);
        assertThat(aq.alderPollen()).isEqualTo(alder);
    }

    @Test
    void shouldCreateHourlyAirQualityWithNullFields() {
        // Arrange & Act
        var aq = new HourlyAirQuality(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(aq).isNotNull();
        assertThat(aq.time()).isNull();
        assertThat(aq.europeanAqi()).isNull();
    }

    @Test
    void shouldCreateWithOnlyEssentialFields() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z");
        var aqi = List.of(25);

        // Act
        var aq = new HourlyAirQuality(times, aqi, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(aq.time()).isEqualTo(times);
        assertThat(aq.europeanAqi()).isEqualTo(aqi);
    }

    @Test
    void shouldSupportRecordEquality() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z");
        var aq1 = new HourlyAirQuality(times, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);
        var aq2 = new HourlyAirQuality(times, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(aq1).isEqualTo(aq2);
    }

    @Test
    void shouldHandleEmptyLists() {
        // Arrange
        List<String> emptyStrings = List.of();
        List<Integer> emptyInts = List.of();
        List<Double> emptyDoubles = List.of();

        // Act
        var aq = new HourlyAirQuality(emptyStrings, emptyInts, emptyInts, emptyDoubles, emptyDoubles,
                emptyDoubles, emptyDoubles, emptyDoubles, emptyDoubles, emptyDoubles, emptyDoubles, emptyDoubles,
                emptyDoubles, emptyDoubles, emptyDoubles, emptyDoubles);

        // Assert
        assertThat(aq.time()).isEmpty();
        assertThat(aq.europeanAqi()).isEmpty();
    }

    @Test
    void shouldHandleFullDayForecast() {
        // Arrange - 24 hour forecast
        var times = List.of("2024-01-30T00:00Z", "2024-01-30T01:00Z", "2024-01-30T02:00Z",
                "2024-01-30T03:00Z", "2024-01-30T04:00Z", "2024-01-30T05:00Z");
        var aqi = List.of(10, 12, 15, 20, 25, 22);

        // Act
        var aq = new HourlyAirQuality(times, aqi, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(aq.time()).hasSize(6);
        assertThat(aq.europeanAqi()).hasSize(6);
    }

    @Test
    void shouldBeSerializable() {
        // Arrange
        var aq = new HourlyAirQuality(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(aq).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void shouldSupportToString() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z");
        var aq = new HourlyAirQuality(times, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Act
        String result = aq.toString();

        // Assert
        assertThat(result).contains("HourlyAirQuality");
        assertThat(result).contains("time");
    }

    @Test
    void shouldHandleAqiValues() {
        // Arrange - Good to Poor AQI progression
        var times = List.of("2024-01-30T00:00Z", "2024-01-30T01:00Z", "2024-01-30T02:00Z");
        var europeanAqi = List.of(10, 50, 100); // Good -> Moderate -> Poor
        var usAqi = List.of(15, 75, 150);

        // Act
        var aq = new HourlyAirQuality(times, europeanAqi, usAqi, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(aq.europeanAqi()).contains(10, 50, 100);
        assertThat(aq.usAqi()).contains(15, 75, 150);
    }

    @Test
    void shouldHandlePollutantValues() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z");
        var pm10 = List.of(35.0);
        var pm25 = List.of(12.0);
        var co = List.of(400.0);
        var no2 = List.of(25.0);
        var so2 = List.of(5.0);
        var o3 = List.of(75.0);

        // Act
        var aq = new HourlyAirQuality(times, null, null, pm10, pm25, co, no2, so2, o3,
                null, null, null, null, null, null, null);

        // Assert
        assertThat(aq.pm10()).contains(35.0);
        assertThat(aq.pm25()).contains(12.0);
        assertThat(aq.carbonMonoxide()).contains(400.0);
    }

    @Test
    void shouldHandlePollenData() {
        // Arrange - European pollen levels
        var times = List.of("2024-01-30T00:00Z", "2024-01-30T01:00Z");
        var birch = List.of(0.0, 150.0); // High birch pollen
        var grass = List.of(25.0, 50.0); // Moderate grass pollen
        var ragweed = List.of(0.0, 5.0); // Low ragweed pollen

        // Act
        var aq = new HourlyAirQuality(times, null, null, null, null, null, null, null,
                null, null, null, birch, grass, null, null, ragweed);

        // Assert
        assertThat(aq.birchPollen()).contains(0.0, 150.0);
        assertThat(aq.grassPollen()).contains(25.0, 50.0);
        assertThat(aq.ragweedPollen()).contains(0.0, 5.0);
    }

    @Test
    void shouldHandleNullPollenForNonEuropeanLocations() {
        // Arrange - US location with no pollen data
        var times = List.of("2024-01-30T00:00Z");
        var usAqi = List.of(50);

        // Act
        var aq = new HourlyAirQuality(times, null, usAqi, null, null, null, null, null,
                null, null, null, null, null, null, null, null);

        // Assert
        assertThat(aq.usAqi()).isEqualTo(usAqi);
        assertThat(aq.alderPollen()).isNull();
    }

    @Test
    void shouldHandleUvIndexData() {
        // Arrange
        var times = List.of("2024-01-30T00:00Z", "2024-01-30T12:00Z");
        var uv = List.of(0.0, 5.0);

        // Act
        var aq = new HourlyAirQuality(times, null, null, null, null, null, null, null,
                null, uv, null, null, null, null, null, null);

        // Assert
        assertThat(aq.uvIndex()).contains(0.0, 5.0);
    }
}
