package com.openmeteo.mcp.service;

import com.openmeteo.mcp.client.OpenMeteoClient;
import com.openmeteo.mcp.model.dto.AirQualityForecast;
import com.openmeteo.mcp.model.dto.CurrentAirQuality;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AirQualityService.
 */
@ExtendWith(MockitoExtension.class)
class AirQualityServiceTest {

    @Mock
    private OpenMeteoClient client;

    @InjectMocks
    private AirQualityService service;

    @Test
    void shouldGetAirQualitySuccessfully() {
        // Arrange
        var mockForecast = new AirQualityForecast(
                47.3769, 8.5417, 408.0, "Europe/Zurich", "CET", 3600,
                null, null, 1.5
        );

        when(client.getAirQuality(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act
        var result = service.getAirQuality(47.3769, 8.5417, 3, true,
                "Europe/Zurich").join();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.latitude()).isEqualTo(47.3769);
        assertThat(result.longitude()).isEqualTo(8.5417);

        verify(client).getAirQuality(47.3769, 8.5417, 3, true, "Europe/Zurich");
    }

    @Test
    void shouldValidateLatitude() {
        // Act & Assert
        assertThatThrownBy(() ->
                service.getAirQuality(91.0, 8.5417, 3, false, "UTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude");
    }

    @Test
    void shouldValidateLongitude() {
        // Act & Assert
        assertThatThrownBy(() ->
                service.getAirQuality(47.3769, 181.0, 3, false, "UTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude");
    }

    @Test
    void shouldClampForecastDaysToMaximum() {
        // Arrange
        var mockForecast = new AirQualityForecast(47.3769, 8.5417, null,
                "UTC", null, null, null, null, null);
        when(client.getAirQuality(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act - Request 10 days (should be clamped to 5)
        service.getAirQuality(47.3769, 8.5417, 10, false, "UTC").join();

        // Assert - Verify clamped to 5
        verify(client).getAirQuality(47.3769, 8.5417, 5, false, "UTC");
    }

    @Test
    void shouldClampForecastDaysToMinimum() {
        // Arrange
        var mockForecast = new AirQualityForecast(47.3769, 8.5417, null,
                "UTC", null, null, null, null, null);
        when(client.getAirQuality(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act - Request 0 days (should be clamped to 1)
        service.getAirQuality(47.3769, 8.5417, 0, false, "UTC").join();

        // Assert - Verify clamped to 1
        verify(client).getAirQuality(47.3769, 8.5417, 1, false, "UTC");
    }

    @Test
    void shouldGetAirQualityWithInterpretation() {
        // Arrange
        var current = new CurrentAirQuality(
                "2024-01-30T12:00",
                35,     // European AQI
                75,     // US AQI
                20.0,   // PM10
                10.0,   // PM2.5
                300.0,  // CO
                15.0,   // NO2
                5.0,    // SO2
                80.0,   // O3
                4.5     // UV index
        );

        var mockForecast = new AirQualityForecast(
                47.3769, 8.5417, null, "UTC", null, null,
                current, null, 1.0
        );

        when(client.getAirQuality(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act
        var result = service.getAirQualityWithInterpretation(
                47.3769, 8.5417, 3, false, "UTC"
        ).join();

        // Assert
        assertThat(result).containsKey("forecast");
        assertThat(result).containsKey("interpretation");

        @SuppressWarnings("unchecked")
        Map<String, Object> interpretation =
                (Map<String, Object>) result.get("interpretation");

        assertThat(interpretation).containsKey("european_aqi_level");
        assertThat(interpretation).containsKey("us_aqi_level");
        assertThat(interpretation).containsKey("uv_index_level");

        assertThat(interpretation.get("european_aqi_level")).isEqualTo("Fair");
        assertThat(interpretation.get("us_aqi_level")).isEqualTo("Moderate");
        assertThat(interpretation.get("uv_index_level")).isEqualTo("Moderate");
    }

    @Test
    void shouldGetAirQualityWithoutInterpretationWhenNoCurrentData() {
        // Arrange
        var mockForecast = new AirQualityForecast(
                47.3769, 8.5417, null, "UTC", null, null,
                null,  // No current data
                null, null
        );

        when(client.getAirQuality(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act
        var result = service.getAirQualityWithInterpretation(
                47.3769, 8.5417, 3, false, "UTC"
        ).join();

        // Assert
        assertThat(result).containsKey("forecast");
        assertThat(result).doesNotContainKey("interpretation");
    }

    @Test
    void shouldHandleNullAqiValues() {
        // Arrange
        var current = new CurrentAirQuality(
                "2024-01-30T12:00",
                null,   // Null European AQI
                null,   // Null US AQI
                20.0, 10.0, 300.0, 15.0, 5.0, 80.0,
                4.5
        );

        var mockForecast = new AirQualityForecast(
                47.3769, 8.5417, null, "UTC", null, null,
                current, null, 1.0
        );

        when(client.getAirQuality(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act
        var result = service.getAirQualityWithInterpretation(
                47.3769, 8.5417, 3, false, "UTC"
        ).join();

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> interpretation =
                (Map<String, Object>) result.get("interpretation");

        assertThat(interpretation).doesNotContainKey("european_aqi_level");
        assertThat(interpretation).doesNotContainKey("us_aqi_level");
        assertThat(interpretation).containsKey("uv_index_level");
    }
}
