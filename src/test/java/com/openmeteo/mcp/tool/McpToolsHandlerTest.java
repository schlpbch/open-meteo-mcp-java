package com.openmeteo.mcp.tool;

import com.openmeteo.mcp.model.dto.GeocodingResponse;
import com.openmeteo.mcp.model.dto.GeocodingResult;
import com.openmeteo.mcp.service.AirQualityService;
import com.openmeteo.mcp.service.LocationService;
import com.openmeteo.mcp.service.SnowConditionsService;
import com.openmeteo.mcp.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for McpToolsHandler.
 * Tests MCP tool annotations and method invocations.
 */
@ExtendWith(MockitoExtension.class)
class McpToolsHandlerTest {

    @Mock
    private LocationService locationService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private SnowConditionsService snowConditionsService;

    @Mock
    private AirQualityService airQualityService;

    private McpToolsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new McpToolsHandler(locationService, weatherService, snowConditionsService, airQualityService);
    }

    // ========== searchLocation Tests ==========

    @Nested
    class SearchLocationTests {

        @Test
        void shouldSearchLocation_successfully() throws ExecutionException, InterruptedException {
            // Arrange
            var result = new GeocodingResult(
                    1L, "Zürich", 47.3769, 8.5417, 408.0, "PPLA", "CH",
                    "Zurich", null, null, null, "Europe/Zurich", 400000L, "Switzerland", 756L
            );
            var response = new GeocodingResponse(List.of(result), 1.5);

            when(locationService.searchLocation("Zürich", 10, "en", "CH"))
                    .thenReturn(CompletableFuture.completedFuture(response));

            // Act
            var actual = handler.searchLocation("Zürich", 10, "en", "CH").get();

            // Assert
            assertThat(actual.results()).hasSize(1);
            assertThat(actual.results().get(0).name()).isEqualTo("Zürich");
            verify(locationService).searchLocation("Zürich", 10, "en", "CH");
        }

        @Test
        void shouldUseDefaultLanguage_whenNull() throws ExecutionException, InterruptedException {
            // Arrange
            var response = new GeocodingResponse(List.of(), 1.0);
            when(locationService.searchLocation(anyString(), anyInt(), eq("en"), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(response));

            // Act
            handler.searchLocation("Test", 5, null, "").get();

            // Assert
            verify(locationService).searchLocation("Test", 5, "en", "");
        }

        @Test
        void shouldUseDefaultCount_whenZeroOrNegative() throws ExecutionException, InterruptedException {
            // Arrange
            var response = new GeocodingResponse(List.of(), 1.0);
            when(locationService.searchLocation(anyString(), eq(10), anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(response));

            // Act
            handler.searchLocation("Test", 0, "en", "").get();

            // Assert
            verify(locationService).searchLocation("Test", 10, "en", "");
        }

        @Test
        void shouldPassCountryFilter_correctly() throws ExecutionException, InterruptedException {
            // Arrange
            var response = new GeocodingResponse(List.of(), 1.0);
            when(locationService.searchLocation(anyString(), anyInt(), anyString(), eq("DE")))
                    .thenReturn(CompletableFuture.completedFuture(response));

            // Act
            handler.searchLocation("Berlin", 5, "de", "DE").get();

            // Assert
            verify(locationService).searchLocation("Berlin", 5, "de", "DE");
        }
    }

    // ========== getWeather Tests ==========

    @Nested
    class GetWeatherTests {

        @Test
        void shouldGetWeather_successfully() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> weatherData = Map.of(
                    "latitude", 47.3769,
                    "longitude", 8.5417,
                    "current", Map.of("temperature", 20.0, "weathercode", 1),
                    "timezone", "Europe/Zurich"
            );

            when(weatherService.getWeatherWithInterpretation(47.3769, 8.5417, 7, true, "Europe/Zurich"))
                    .thenReturn(CompletableFuture.completedFuture(weatherData));

            // Act
            var result = handler.getWeather(47.3769, 8.5417, 7, "Europe/Zurich").get();

            // Assert
            assertThat(result.get("latitude")).isEqualTo(47.3769);
            assertThat(result).containsKey("current");
            verify(weatherService).getWeatherWithInterpretation(47.3769, 8.5417, 7, true, "Europe/Zurich");
        }

        @Test
        void shouldUseDefaultTimezone_whenNull() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> weatherData = Map.of("latitude", 47.3769);
            when(weatherService.getWeatherWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), eq("UTC")))
                    .thenReturn(CompletableFuture.completedFuture(weatherData));

            // Act
            handler.getWeather(47.3769, 8.5417, 7, null).get();

            // Assert
            verify(weatherService).getWeatherWithInterpretation(47.3769, 8.5417, 7, true, "UTC");
        }

        @Test
        void shouldUseDefaultForecastDays_whenZeroOrNegative() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> weatherData = Map.of("latitude", 47.3769);
            when(weatherService.getWeatherWithInterpretation(anyDouble(), anyDouble(), eq(7), anyBoolean(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(weatherData));

            // Act
            handler.getWeather(47.3769, 8.5417, 0, "UTC").get();

            // Assert
            verify(weatherService).getWeatherWithInterpretation(47.3769, 8.5417, 7, true, "UTC");
        }

        @Test
        void shouldIncludeHourlyData_always() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> weatherData = Map.of("hourly", Map.of());
            when(weatherService.getWeatherWithInterpretation(anyDouble(), anyDouble(), anyInt(), eq(true), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(weatherData));

            // Act
            handler.getWeather(47.3769, 8.5417, 7, "UTC").get();

            // Assert - includeHourly should always be true
            verify(weatherService).getWeatherWithInterpretation(47.3769, 8.5417, 7, true, "UTC");
        }
    }

    // ========== getSnowConditions Tests ==========

    @Nested
    class GetSnowConditionsTests {

        @Test
        void shouldGetSnowConditions_successfully() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> snowData = Map.of(
                    "latitude", 45.9763,
                    "longitude", 7.6586,
                    "snow_depth", 120.0,
                    "assessment", Map.of("condition", "Excellent")
            );

            when(snowConditionsService.getSnowConditionsWithAssessment(45.9763, 7.6586, 7, true, "Europe/Zurich"))
                    .thenReturn(CompletableFuture.completedFuture(snowData));

            // Act
            var result = handler.getSnowConditions(45.9763, 7.6586, 7, "Europe/Zurich").get();

            // Assert
            assertThat(result.get("latitude")).isEqualTo(45.9763);
            assertThat(result).containsKey("snow_depth");
            verify(snowConditionsService).getSnowConditionsWithAssessment(45.9763, 7.6586, 7, true, "Europe/Zurich");
        }

        @Test
        void shouldUseDefaultTimezone_whenEmpty() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> snowData = Map.of("latitude", 45.9763);
            when(snowConditionsService.getSnowConditionsWithAssessment(anyDouble(), anyDouble(), anyInt(), anyBoolean(), eq("UTC")))
                    .thenReturn(CompletableFuture.completedFuture(snowData));

            // Act
            handler.getSnowConditions(45.9763, 7.6586, 7, "").get();

            // Assert
            verify(snowConditionsService).getSnowConditionsWithAssessment(45.9763, 7.6586, 7, true, "UTC");
        }

        @Test
        void shouldUseDefaultForecastDays_whenNegative() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> snowData = Map.of("latitude", 45.9763);
            when(snowConditionsService.getSnowConditionsWithAssessment(anyDouble(), anyDouble(), eq(7), anyBoolean(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(snowData));

            // Act
            handler.getSnowConditions(45.9763, 7.6586, -1, "UTC").get();

            // Assert
            verify(snowConditionsService).getSnowConditionsWithAssessment(45.9763, 7.6586, 7, true, "UTC");
        }

        @Test
        void shouldIncludeHourlyData_always() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> snowData = Map.of("hourly", Map.of());
            when(snowConditionsService.getSnowConditionsWithAssessment(anyDouble(), anyDouble(), anyInt(), eq(true), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(snowData));

            // Act
            handler.getSnowConditions(45.9763, 7.6586, 7, "UTC").get();

            // Assert
            verify(snowConditionsService).getSnowConditionsWithAssessment(45.9763, 7.6586, 7, true, "UTC");
        }
    }

    // ========== getAirQuality Tests ==========

    @Nested
    class GetAirQualityTests {

        @Test
        void shouldGetAirQuality_successfully() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> airQualityData = Map.of(
                    "latitude", 47.3769,
                    "longitude", 8.5417,
                    "current", Map.of("european_aqi", 25.0, "pm2_5", 10.0),
                    "interpretation", Map.of("level", "Good")
            );

            when(airQualityService.getAirQualityWithInterpretation(47.3769, 8.5417, 3, true, "Europe/Zurich"))
                    .thenReturn(CompletableFuture.completedFuture(airQualityData));

            // Act
            var result = handler.getAirQuality(47.3769, 8.5417, 3, true, "Europe/Zurich").get();

            // Assert
            assertThat(result.get("latitude")).isEqualTo(47.3769);
            assertThat(result).containsKey("current");
            verify(airQualityService).getAirQualityWithInterpretation(47.3769, 8.5417, 3, true, "Europe/Zurich");
        }

        @Test
        void shouldGetAirQuality_withoutPollen() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> airQualityData = Map.of("latitude", 47.3769);
            when(airQualityService.getAirQualityWithInterpretation(anyDouble(), anyDouble(), anyInt(), eq(false), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(airQualityData));

            // Act
            handler.getAirQuality(47.3769, 8.5417, 5, false, "UTC").get();

            // Assert
            verify(airQualityService).getAirQualityWithInterpretation(47.3769, 8.5417, 5, false, "UTC");
        }

        @Test
        void shouldUseDefaultTimezone_whenNull() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> airQualityData = Map.of("latitude", 47.3769);
            when(airQualityService.getAirQualityWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), eq("UTC")))
                    .thenReturn(CompletableFuture.completedFuture(airQualityData));

            // Act
            handler.getAirQuality(47.3769, 8.5417, 5, true, null).get();

            // Assert
            verify(airQualityService).getAirQualityWithInterpretation(47.3769, 8.5417, 5, true, "UTC");
        }

        @Test
        void shouldUseDefaultForecastDays_whenZero() throws ExecutionException, InterruptedException {
            // Arrange
            Map<String, Object> airQualityData = Map.of("latitude", 47.3769);
            when(airQualityService.getAirQualityWithInterpretation(anyDouble(), anyDouble(), eq(3), anyBoolean(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture(airQualityData));

            // Act
            handler.getAirQuality(47.3769, 8.5417, 0, true, "UTC").get();

            // Assert - Default should be 3
            verify(airQualityService).getAirQualityWithInterpretation(47.3769, 8.5417, 3, true, "UTC");
        }
    }
}
