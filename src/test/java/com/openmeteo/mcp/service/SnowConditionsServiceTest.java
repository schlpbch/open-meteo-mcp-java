package com.openmeteo.mcp.service;

import com.openmeteo.mcp.client.OpenMeteoClient;
import com.openmeteo.mcp.model.dto.DailySnow;
import com.openmeteo.mcp.model.dto.HourlySnow;
import com.openmeteo.mcp.model.dto.SnowConditions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SnowConditionsService.
 */
@ExtendWith(MockitoExtension.class)
class SnowConditionsServiceTest {

    @Mock
    private OpenMeteoClient client;

    @InjectMocks
    private SnowConditionsService service;

    @Test
    void shouldGetSnowConditionsSuccessfully() {
        // Arrange
        var mockConditions = new SnowConditions(
                47.3769, 8.5417, 408.0, "Europe/Zurich", "CET", 3600,
                null, null, null, 1.5
        );

        when(client.getSnowConditions(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockConditions));

        // Act
        var result = service.getSnowConditions(47.3769, 8.5417, 7, true,
                "Europe/Zurich").join();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.latitude()).isEqualTo(47.3769);
        assertThat(result.longitude()).isEqualTo(8.5417);

        verify(client).getSnowConditions(47.3769, 8.5417, 7, true, "Europe/Zurich");
    }

    @Test
    void shouldValidateLatitude() {
        // Act & Assert
        assertThatThrownBy(() ->
                service.getSnowConditions(91.0, 8.5417, 7, true, "UTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude");
    }

    @Test
    void shouldValidateLongitude() {
        // Act & Assert
        assertThatThrownBy(() ->
                service.getSnowConditions(47.3769, 181.0, 7, true, "UTC"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude");
    }

    @Test
    void shouldClampForecastDaysToMaximum() {
        // Arrange
        var mockConditions = new SnowConditions(47.3769, 8.5417, null,
                "UTC", null, null, null, null, null, null);
        when(client.getSnowConditions(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockConditions));

        // Act - Request 20 days (should be clamped to 16)
        service.getSnowConditions(47.3769, 8.5417, 20, false, "UTC").join();

        // Assert - Verify clamped to 16
        verify(client).getSnowConditions(47.3769, 8.5417, 16, false, "UTC");
    }

    @Test
    void shouldClampForecastDaysToMinimum() {
        // Arrange
        var mockConditions = new SnowConditions(47.3769, 8.5417, null,
                "UTC", null, null, null, null, null, null);
        when(client.getSnowConditions(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockConditions));

        // Act - Request 0 days (should be clamped to 1)
        service.getSnowConditions(47.3769, 8.5417, 0, false, "UTC").join();

        // Assert - Verify clamped to 1
        verify(client).getSnowConditions(47.3769, 8.5417, 1, false, "UTC");
    }

    @Test
    void shouldGetSnowConditionsWithAssessment() {
        // Arrange
        var hourly = new HourlySnow(
                List.of("2024-01-30T00:00"),
                List.of(-8.0),      // temperature
                List.of(0.5),       // snowfall
                List.of(0.8),       // snow depth
                List.of(1),         // weather code
                List.of(50),        // cloud cover
                List.of(10.0),      // wind speed
                List.of(180),       // wind direction
                List.of(15.0),      // wind gusts
                List.of(2500.0)     // freezing level
        );

        var daily = new DailySnow(
                List.of("2024-01-30"),
                List.of(-5.0),      // temp max
                List.of(-15.0),     // temp min
                List.of(15.0),      // snowfall sum
                List.of(1),         // weather code
                List.of(20.0),      // wind speed max
                List.of(30.0),      // wind gusts max
                List.of(180)        // wind direction
        );

        var mockConditions = new SnowConditions(
                47.3769, 8.5417, 2000.0, "UTC", null, null,
                null, hourly, daily, 1.0
        );

        when(client.getSnowConditions(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockConditions));

        // Act
        var result = service.getSnowConditionsWithAssessment(
                47.3769, 8.5417, 7, true, "UTC"
        ).join();

        // Assert
        assertThat(result).containsKey("conditions");
        assertThat(result).containsKey("ski_assessment");

        @SuppressWarnings("unchecked")
        Map<String, Object> assessment =
                (Map<String, Object>) result.get("ski_assessment");

        assertThat(assessment).containsKey("assessment");
        assertThat(assessment).containsKey("snow_depth_m");
        assertThat(assessment).containsKey("recent_snowfall_cm");
        assertThat(assessment).containsKey("temperature_c");

        assertThat(assessment.get("assessment")).isEqualTo("Excellent");
        assertThat(assessment.get("snow_depth_m")).isEqualTo(0.8);
        assertThat(assessment.get("recent_snowfall_cm")).isEqualTo(15.0);
        assertThat(assessment.get("temperature_c")).isEqualTo(-8.0);
    }

    @Test
    void shouldGetSnowConditionsWithoutAssessmentWhenNoData() {
        // Arrange
        var mockConditions = new SnowConditions(
                47.3769, 8.5417, null, "UTC", null, null,
                null, null, null, null  // No hourly/daily data
        );

        when(client.getSnowConditions(anyDouble(), anyDouble(), anyInt(),
                anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockConditions));

        // Act
        var result = service.getSnowConditionsWithAssessment(
                47.3769, 8.5417, 7, true, "UTC"
        ).join();

        // Assert
        assertThat(result).containsKey("conditions");
        assertThat(result).doesNotContainKey("ski_assessment");
    }
}
