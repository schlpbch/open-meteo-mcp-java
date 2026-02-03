package com.openmeteo.mcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for HistoricalWeatherService.
 * Tests cover date validation, data processing, and statistics calculations.
 */
@ExtendWith(MockitoExtension.class)
class HistoricalWeatherServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private HistoricalWeatherService service;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(any(String.class))).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        service = new HistoricalWeatherService(webClientBuilder);
    }

    @Nested
    class GetHistoricalWeatherTests {

        @Test
        void shouldReturnHistoricalData_withStatistics() {
            // Arrange
            Map<String, Object> apiResponse = Map.of(
                    "daily", Map.of(
                            "time", List.of("2023-01-01", "2023-01-02", "2023-01-03"),
                            "temperature_2m_max", List.of(5.0, 6.0, 7.0),
                            "temperature_2m_min", List.of(-2.0, -1.0, 0.0),
                            "temperature_2m_mean", List.of(1.5, 2.5, 3.5),
                            "precipitation_sum", List.of(0.5, 1.0, 0.0),
                            "windspeed_10m_max", List.of(20.0, 15.0, 25.0)
                    )
            );

            setupWebClientMock(apiResponse);

            // Act
            var result = service.getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-01-03", "Europe/Zurich").join();

            // Assert
            assertThat(result.get("latitude")).isEqualTo(47.3769);
            assertThat(result.get("longitude")).isEqualTo(8.5417);
            assertThat(result.get("start_date")).isEqualTo("2023-01-01");
            assertThat(result.get("end_date")).isEqualTo("2023-01-03");
            assertThat(result).containsKey("daily");
            assertThat(result).containsKey("statistics");

            @SuppressWarnings("unchecked")
            Map<String, Object> statistics = (Map<String, Object>) result.get("statistics");
            assertThat(statistics).containsKey("temperature");
            assertThat(statistics).containsKey("precipitation");
            assertThat(statistics).containsKey("wind");
        }

        @Test
        void shouldReturnError_whenApiFails() {
            // Arrange
            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(Map.class))
                    .thenReturn(Mono.error(new RuntimeException("API Error")));

            // Act
            var result = service.getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-01-03", "UTC").join();

            // Assert
            assertThat(result).containsKey("error");
            assertThat(result.get("error")).isEqualTo("Failed to fetch historical weather data");
        }
    }

    @Nested
    class DateValidationTests {

        @Test
        void shouldThrow_whenStartDateBefore1940() {
            // Act & Assert
            // The validation catches IllegalArgumentException and wraps it with a generic message
            assertThatThrownBy(() ->
                    service.getHistoricalWeather(47.3769, 8.5417, "1939-12-31", "1940-01-31", "UTC").join())
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldThrow_whenEndDateInFuture() {
            // Arrange
            String futureDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            String pastDate = LocalDate.now().minusDays(10).format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Act & Assert
            assertThatThrownBy(() ->
                    service.getHistoricalWeather(47.3769, 8.5417, pastDate, futureDate, "UTC").join())
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldThrow_whenStartDateAfterEndDate() {
            // Act & Assert
            assertThatThrownBy(() ->
                    service.getHistoricalWeather(47.3769, 8.5417, "2023-12-31", "2023-01-01", "UTC").join())
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldThrow_whenInvalidDateFormat() {
            // Act & Assert
            assertThatThrownBy(() ->
                    service.getHistoricalWeather(47.3769, 8.5417, "01-01-2023", "31-01-2023", "UTC").join())
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldAccept_validDateRange() {
            // Arrange
            Map<String, Object> apiResponse = Map.of("daily", Map.of());
            setupWebClientMock(apiResponse);

            // Act - Should not throw
            var result = service.getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-12-31", "UTC").join();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.get("start_date")).isEqualTo("2023-01-01");
            assertThat(result.get("end_date")).isEqualTo("2023-12-31");
        }

        @Test
        void shouldAccept_earliestDate1940() {
            // Arrange
            Map<String, Object> apiResponse = Map.of("daily", Map.of());
            setupWebClientMock(apiResponse);

            // Act - Should not throw
            var result = service.getHistoricalWeather(47.3769, 8.5417, "1940-01-01", "1940-12-31", "UTC").join();

            // Assert
            assertThat(result).isNotNull();
        }
    }

    @Nested
    class DataProcessingTests {

        @Test
        void shouldProcessFullData_withAllFields() {
            // Arrange
            Map<String, Object> apiResponse = Map.of(
                    "daily", Map.of(
                            "time", List.of("2023-06-01", "2023-06-02"),
                            "temperature_2m_max", List.of(25.0, 28.0),
                            "temperature_2m_min", List.of(15.0, 18.0),
                            "temperature_2m_mean", List.of(20.0, 23.0),
                            "precipitation_sum", List.of(2.5, 0.0),
                            "rain_sum", List.of(2.5, 0.0),
                            "snowfall_sum", List.of(0.0, 0.0),
                            "weathercode", List.of(61, 0),
                            "windspeed_10m_max", List.of(30.0, 15.0),
                            "windgusts_10m_max", List.of(45.0, 22.0)
                    )
            );

            setupWebClientMock(apiResponse);

            // Act
            var result = service.getHistoricalWeather(47.3769, 8.5417, "2023-06-01", "2023-06-02", "Europe/Zurich").join();

            // Assert
            assertThat(result).containsKey("daily");
            assertThat(result).containsKey("statistics");

            @SuppressWarnings("unchecked")
            Map<String, Object> daily = (Map<String, Object>) result.get("daily");
            assertThat(daily).containsKeys("time", "temperature_2m_max", "temperature_2m_min");
        }

        @Test
        void shouldHandleEmptyData() {
            // Arrange
            Map<String, Object> apiResponse = Map.of("daily", Map.of());
            setupWebClientMock(apiResponse);

            // Act
            var result = service.getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-01-02", "UTC").join();

            // Assert
            assertThat(result).containsKey("daily");
            assertThat(result).containsKey("statistics");

            @SuppressWarnings("unchecked")
            Map<String, Object> statistics = (Map<String, Object>) result.get("statistics");
            assertThat(statistics).isEmpty();
        }
    }

    @Nested
    class StatisticsCalculationTests {

        @Test
        void shouldCalculateTemperatureStatistics() {
            // Arrange
            Map<String, Object> apiResponse = Map.of(
                    "daily", Map.of(
                            "temperature_2m_max", List.of(10.0, 15.0, 20.0),
                            "temperature_2m_min", List.of(-5.0, 0.0, 5.0),
                            "temperature_2m_mean", List.of(2.5, 7.5, 12.5)
                    )
            );

            setupWebClientMock(apiResponse);

            // Act
            var result = service.getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-01-03", "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> statistics = (Map<String, Object>) result.get("statistics");
            @SuppressWarnings("unchecked")
            Map<String, Object> tempStats = (Map<String, Object>) statistics.get("temperature");

            assertThat(tempStats.get("max")).isEqualTo(20.0);
            assertThat(tempStats.get("min")).isEqualTo(-5.0);
            assertThat(((Number) tempStats.get("mean")).doubleValue()).isCloseTo(7.5, org.assertj.core.data.Offset.offset(0.01));
        }

        @Test
        void shouldCalculatePrecipitationStatistics() {
            // Arrange
            Map<String, Object> apiResponse = Map.of(
                    "daily", Map.of(
                            "precipitation_sum", List.of(0.0, 5.0, 10.0, 0.2, 0.0)
                    )
            );

            setupWebClientMock(apiResponse);

            // Act
            var result = service.getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-01-05", "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> statistics = (Map<String, Object>) result.get("statistics");
            @SuppressWarnings("unchecked")
            Map<String, Object> precipStats = (Map<String, Object>) statistics.get("precipitation");

            assertThat(precipStats.get("total_mm")).isEqualTo(15.2);
            assertThat(precipStats.get("rainy_days")).isEqualTo(3L); // Days with > 0.1mm
        }

        @Test
        void shouldCalculateWindStatistics() {
            // Arrange
            Map<String, Object> apiResponse = Map.of(
                    "daily", Map.of(
                            "windspeed_10m_max", List.of(10.0, 25.0, 40.0, 15.0)
                    )
            );

            setupWebClientMock(apiResponse);

            // Act
            var result = service.getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-01-04", "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> statistics = (Map<String, Object>) result.get("statistics");
            @SuppressWarnings("unchecked")
            Map<String, Object> windStats = (Map<String, Object>) statistics.get("wind");

            assertThat(windStats.get("max_speed_kmh")).isEqualTo(40.0);
            assertThat(((Number) windStats.get("average_speed_kmh")).doubleValue()).isCloseTo(22.5, org.assertj.core.data.Offset.offset(0.01));
        }
    }

    // ========== Helper Methods ==========

    @SuppressWarnings("unchecked")
    private void setupWebClientMock(Map<String, Object> apiResponse) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(apiResponse));
    }
}
