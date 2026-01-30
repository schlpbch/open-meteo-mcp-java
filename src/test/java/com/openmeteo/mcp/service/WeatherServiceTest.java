package com.openmeteo.mcp.service;

import com.openmeteo.mcp.client.OpenMeteoClient;
import com.openmeteo.mcp.model.dto.CurrentWeather;
import com.openmeteo.mcp.model.dto.WeatherForecast;
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
 * Unit tests for WeatherService.
 */
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private OpenMeteoClient client;

    @InjectMocks
    private WeatherService service;

    @Test
    void shouldGetWeatherSuccessfully() {
        // Arrange
        var mockForecast = new WeatherForecast(
                47.3769, 8.5417, 408.0, "Europe/Zurich", "CET", 3600,
                new CurrentWeather(15.5, 10.0, 180, 1, "2024-01-30T12:00Z"),
                null, null, 1.5
        );

        when(client.getWeather(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act
        var result = service.getWeather(47.3769, 8.5417, 7, true,
                "Europe/Zurich").join();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.latitude()).isEqualTo(47.3769);
        assertThat(result.longitude()).isEqualTo(8.5417);
        assertThat(result.currentWeather()).isNotNull();
        assertThat(result.currentWeather().temperature()).isEqualTo(15.5);

        verify(client).getWeather(47.3769, 8.5417, 7, true, "Europe/Zurich");
    }

    @Test
    void shouldValidateLatitude() {
        // Act & Assert
        assertThatThrownBy(() ->
                service.getWeather(91.0, 8.5417, 7, true, "UTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude");
    }

    @Test
    void shouldValidateLongitude() {
        // Act & Assert
        assertThatThrownBy(() ->
                service.getWeather(47.3769, 181.0, 7, true, "UTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude");
    }

    @Test
    void shouldClampForecastDaysToMaximum() {
        // Arrange
        var mockForecast = new WeatherForecast(47.3769, 8.5417, null,
                "UTC", null, null, null,
                null, null, null);
        when(client.getWeather(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act - Request 20 days (should be clamped to 16)
        service.getWeather(47.3769, 8.5417, 20, false, "UTC").join();

        // Assert - Verify clamped to 16
        verify(client).getWeather(47.3769, 8.5417, 16, false, "UTC");
    }

    @Test
    void shouldClampForecastDaysToMinimum() {
        // Arrange
        var mockForecast = new WeatherForecast(47.3769, 8.5417, null,
                "UTC", null, null, null,
                null, null, null);
        when(client.getWeather(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act - Request 0 days (should be clamped to 1)
        service.getWeather(47.3769, 8.5417, 0, false, "UTC").join();

        // Assert - Verify clamped to 1
        verify(client).getWeather(47.3769, 8.5417, 1, false, "UTC");
    }

    @Test
    void shouldGetWeatherWithInterpretation() {
        // Arrange
        var mockForecast = new WeatherForecast(
                47.3769, 8.5417, null, "UTC", null, null,
                new CurrentWeather(15.5, 10.0, 180, 1, "2024-01-30T12:00Z"),
                null, null, null
        );

        when(client.getWeather(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act
        var result = service.getWeatherWithInterpretation(
                47.3769, 8.5417, 7, true, "UTC"
        ).join();

        // Assert
        assertThat(result).containsKey("forecast");
        assertThat(result).containsKey("interpretation");

        @SuppressWarnings("unchecked")
        Map<String, Object> interpretation =
                (Map<String, Object>) result.get("interpretation");

        assertThat(interpretation).containsKey("description");
        assertThat(interpretation).containsKey("category");
        assertThat(interpretation).containsKey("severity");
        assertThat(interpretation).containsKey("travel_impact");
        assertThat(interpretation).containsKey("formatted_temperature");
        assertThat(interpretation).containsKey("formatted_wind");

        assertThat(interpretation.get("description")).isEqualTo("Mainly clear");
        assertThat(interpretation.get("category")).isEqualTo("Cloudy");
        assertThat(interpretation.get("formatted_temperature")).isEqualTo("15.5°C");
    }

    @Test
    void shouldGetWeatherWithoutInterpretationWhenNoCurrentWeather() {
        // Arrange
        var mockForecast = new WeatherForecast(
                47.3769, 8.5417, null, "UTC", null, null,
                null,  // No current weather
                null, null, null
        );

        when(client.getWeather(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockForecast));

        // Act
        var result = service.getWeatherWithInterpretation(
                47.3769, 8.5417, 7, true, "UTC"
        ).join();

        // Assert
        assertThat(result).containsKey("forecast");
        assertThat(result).doesNotContainKey("interpretation");
    }
}
