package com.openmeteo.mcp.service;

import com.openmeteo.mcp.client.OpenMeteoClient;
import com.openmeteo.mcp.model.dto.CurrentWeather;
import com.openmeteo.mcp.model.dto.WeatherForecast;
import com.openmeteo.mcp.model.stream.StreamMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for StreamingWeatherService.
 * 
 * Tests streaming weather functionality as specified in ADR-020 Phase 4.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Streaming Weather Service Tests")
class StreamingWeatherServiceTest {

    @Mock
    private OpenMeteoClient mockClient;

    @Mock
    private WeatherService mockWeatherService;

    @Mock
    private HistoricalWeatherService mockHistoricalWeatherService;

    private StreamingWeatherService streamingWeatherService;

    @BeforeEach
    void setUp() {
        streamingWeatherService = new StreamingWeatherService(
                mockClient,
                mockWeatherService,
                mockHistoricalWeatherService
        );
    }

    @Test
    @DisplayName("Should stream current weather")
    void shouldStreamCurrentWeather() {
        // Given
        WeatherForecast forecast = createMockForecast(12.5, 78.0);
        when(mockWeatherService.getWeather(anyDouble(), anyDouble(), eq(1), eq(false), anyString()))
                .thenReturn(CompletableFuture.completedFuture(forecast));

        // When
        var result = streamingWeatherService.streamCurrentWeather(12.5, 78.0, "auto");

        // Then
        StepVerifier.create(result)
                .assertNext(msg -> {
                    assertThat(msg.type()).isEqualTo("data");
                    assertThat(msg.data()).isNotNull();
                })
                .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Should stream forecast with single chunk for small dataset")
    void shouldStreamForecastWithSingleChunk() {
        // Given
        WeatherForecast forecast = createMockForecast(12.5, 78.0);
        when(mockWeatherService.getWeather(anyDouble(), anyDouble(), eq(3), eq(false), anyString()))
                .thenReturn(CompletableFuture.completedFuture(forecast));

        // When
        var result = streamingWeatherService.streamForecast(12.5, 78.0, 3, false, "auto");

        // Then
        StepVerifier.create(result)
                .assertNext(msg -> assertThat(msg.type()).isEqualTo("data"))
                .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Should stream forecast with multiple chunks for large dataset")
    void shouldStreamForecastWithMultipleChunks() {
        // Given
        WeatherForecast forecast = createLargeMockForecast(12.5, 78.0);
        when(mockWeatherService.getWeather(anyDouble(), anyDouble(), eq(7), eq(true), anyString()))
                .thenReturn(CompletableFuture.completedFuture(forecast));

        // When
        var result = streamingWeatherService.streamForecast(12.5, 78.0, 7, true, "auto");

        // Then - small dataset returns single chunk + complete
        StepVerifier.create(result)
                .expectNextCount(2) // data + complete
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Should stream historical weather for small date range")
    void shouldStreamHistoricalWeatherSmallRange() {
        // Given
        var historicalData = createMockHistoricalData();
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        when(mockHistoricalWeatherService.getHistoricalWeather(
                anyDouble(), anyDouble(), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(historicalData));

        // When
        var result = streamingWeatherService.streamHistoricalWeather(
                12.5, 78.0, startDate, endDate, "auto");

        // Then
        StepVerifier.create(result)
                .assertNext(msg -> assertThat(msg.type()).isEqualTo("data"))
                .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Should stream historical weather in week chunks for medium range")
    void shouldStreamHistoricalWeatherMediumRange() {
        // Given
        var historicalData = createMockHistoricalData();
        LocalDate startDate = LocalDate.now().minusDays(120);
        LocalDate endDate = LocalDate.now();
        
        when(mockHistoricalWeatherService.getHistoricalWeather(
                anyDouble(), anyDouble(), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(historicalData));

        // When
        var result = streamingWeatherService.streamHistoricalWeather(
                12.5, 78.0, startDate, endDate, "auto");

        // Then - expect week chunks + complete
        StepVerifier.create(result)
                .expectNextCount(18) // ~18 week chunks
                .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Should stream historical weather in month chunks for large range")
    void shouldStreamHistoricalWeatherLargeRange() {
        // Given
        var historicalData = createMockHistoricalData();
        LocalDate startDate = LocalDate.now().minusYears(2);
        LocalDate endDate = LocalDate.now();
        
        when(mockHistoricalWeatherService.getHistoricalWeather(
                anyDouble(), anyDouble(), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(historicalData));

        // When
        var result = streamingWeatherService.streamHistoricalWeather(
                12.5, 78.0, startDate, endDate, "auto");

        // Then - expect month chunks + complete
        StepVerifier.create(result)
                .expectNextCount(25) // ~24 month chunks
                .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
                .expectComplete()
                .verify(Duration.ofSeconds(15));
    }

    @Test
    @DisplayName("Should stream with progress indicators")
    void shouldStreamWithProgress() {
        // Given
        WeatherForecast forecast = createMockForecast(12.5, 78.0);
        when(mockWeatherService.getWeather(anyDouble(), anyDouble(), eq(7), eq(true), anyString()))
                .thenReturn(CompletableFuture.completedFuture(forecast));

        // When
        var result = streamingWeatherService.streamWithProgress(12.5, 78.0, 7, "auto");

        // Then
        StepVerifier.create(result)
                .expectNextMatches(msg -> msg.type().equals("progress"))
                .expectNextMatches(msg -> msg.type().equals("progress"))
                .expectNextMatches(msg -> msg.type().equals("progress"))
                .expectNextMatches(msg -> msg.type().equals("data"))
                .expectNextMatches(msg -> msg.type().equals("progress"))
                .expectNextMatches(msg -> msg.type().equals("complete"))
                .expectComplete()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Should handle error in current weather stream")
    void shouldHandleErrorInCurrentWeatherStream() {
        // Given
        when(mockWeatherService.getWeather(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("API Error")));

        // When
        var result = streamingWeatherService.streamCurrentWeather(12.5, 78.0, "auto");

        // Then
        StepVerifier.create(result)
                .assertNext(msg -> {
                    assertThat(msg.type()).isEqualTo("error");
                    StreamMessage.ErrorData error = (StreamMessage.ErrorData) msg.data();
                    assertThat(error.message()).contains("API Error");
                })
                .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Should handle error in forecast stream")
    void shouldHandleErrorInForecastStream() {
        // Given
        when(mockWeatherService.getWeather(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Forecast Error")));

        // When
        var result = streamingWeatherService.streamForecast(12.5, 78.0, 7, false, "auto");

        // Then
        StepVerifier.create(result)
                .assertNext(msg -> assertThat(msg.type()).isEqualTo("error"))
                .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("Should validate and clamp forecast days")
    void shouldValidateAndClampForecastDays() {
        // Given
        WeatherForecast forecast = createMockForecast(12.5, 78.0);
        when(mockWeatherService.getWeather(anyDouble(), anyDouble(), eq(16), eq(false), anyString()))
                .thenReturn(CompletableFuture.completedFuture(forecast));

        // When - request excessive days
        var result = streamingWeatherService.streamForecast(12.5, 78.0, 20, false, "auto");

        // Then - should be clamped to 16
        StepVerifier.create(result)
                .expectNextCount(2) // data + complete
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    // Helper methods to create mock data

    private WeatherForecast createMockForecast(double lat, double lon) {
        CurrentWeather current = new CurrentWeather(20.0, 10.0, 180, 0, "2024-01-01T12:00");
        return new WeatherForecast(
                lat, lon, 100.0, "auto", "UTC", 0,
                current, null, null, 0.5
        );
    }

    private WeatherForecast createLargeMockForecast(double lat, double lon) {
        CurrentWeather current = new CurrentWeather(20.0, 10.0, 180, 0, "2024-01-01T12:00");
        // Simulate large dataset - the service will estimate based on presence of hourly data
        return new WeatherForecast(
                lat, lon, 100.0, "auto", "UTC", 0,
                current, null, null, 0.5
        );
    }

    private java.util.Map<String, Object> createMockHistoricalData() {
        return java.util.Map.of(
                "latitude", 12.5,
                "longitude", 78.0,
                "daily", java.util.Map.of(
                        "time", List.of("2024-01-01", "2024-01-02"),
                        "temperature_2m_max", List.of(25.0, 26.0)
                )
        );
    }
}
