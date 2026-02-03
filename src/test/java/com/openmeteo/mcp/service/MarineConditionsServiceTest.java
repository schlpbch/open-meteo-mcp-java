package com.openmeteo.mcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MarineConditionsService.
 * Tests cover marine data retrieval, condition assessment, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class MarineConditionsServiceTest {

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

    private MarineConditionsService service;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(any(String.class))).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        service = new MarineConditionsService(webClientBuilder);
    }

    @Nested
    class GetMarineConditionsTests {

        @Test
        void shouldReturnMarineConditions_withAssessment() {
            // Arrange
            Map<String, Object> apiResponse = Map.of(
                    "hourly", Map.of(
                            "time", List.of("2023-07-01T12:00", "2023-07-01T13:00"),
                            "wave_height", List.of(0.4, 0.5),
                            "wave_direction", List.of(180.0, 185.0),
                            "wave_period", List.of(5.0, 5.2),
                            "wind_wave_height", List.of(0.2, 0.3),
                            "wind_wave_direction", List.of(175.0, 180.0),
                            "swell_wave_height", List.of(0.2, 0.2),
                            "swell_wave_direction", List.of(190.0, 195.0)
                    )
            );

            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 7, "Europe/Zurich").join();

            // Assert
            assertThat(result.get("latitude")).isEqualTo(46.4544);
            assertThat(result.get("longitude")).isEqualTo(6.5886);
            assertThat(result.get("timezone")).isEqualTo("Europe/Zurich");
            assertThat(result).containsKey("current");
            assertThat(result).containsKey("hourly");
            assertThat(result).containsKey("assessment");

            @SuppressWarnings("unchecked")
            Map<String, Object> current = (Map<String, Object>) result.get("current");
            assertThat(current.get("wave_height")).isEqualTo(0.4);
        }

        @Test
        void shouldClampForecastDays_whenTooLow() {
            // Arrange
            Map<String, Object> apiResponse = Map.of("hourly", Map.of());
            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 0, "UTC").join();

            // Assert - Should still work with default days
            assertThat(result).isNotNull();
            assertThat(result.get("latitude")).isEqualTo(46.4544);
        }

        @Test
        void shouldClampForecastDays_whenTooHigh() {
            // Arrange
            Map<String, Object> apiResponse = Map.of("hourly", Map.of());
            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 30, "UTC").join();

            // Assert - Should still work with clamped days
            assertThat(result).isNotNull();
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
            var result = service.getMarineConditions(46.4544, 6.5886, 7, "UTC").join();

            // Assert
            assertThat(result).containsKey("error");
            assertThat(result.get("error")).isEqualTo("Failed to fetch marine conditions");
        }
    }

    @Nested
    class AssessMarineConditionsTests {

        @Test
        void shouldAssess_excellent_whenWavesBelowPoint3m() {
            // Arrange
            Map<String, Object> apiResponse = createApiResponseWithWaveHeight(0.2);
            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 1, "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> assessment = (Map<String, Object>) result.get("assessment");
            assertThat(assessment.get("suitability")).isEqualTo("Excellent");
            assertThat(assessment.get("recommendation")).isEqualTo("Ideal conditions for all water activities");

            @SuppressWarnings("unchecked")
            List<String> activities = (List<String>) assessment.get("suitable_activities");
            assertThat(activities).contains("Swimming", "Kayaking", "Paddleboarding", "Sailing", "Fishing");
        }

        @Test
        void shouldAssess_good_whenWavesBetweenPoint3AndPoint6m() {
            // Arrange
            Map<String, Object> apiResponse = createApiResponseWithWaveHeight(0.5);
            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 1, "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> assessment = (Map<String, Object>) result.get("assessment");
            assertThat(assessment.get("suitability")).isEqualTo("Good");
            assertThat(assessment.get("recommendation")).isEqualTo("Good conditions for most water activities");

            @SuppressWarnings("unchecked")
            List<String> activities = (List<String>) assessment.get("suitable_activities");
            assertThat(activities).contains("Swimming", "Kayaking", "Sailing", "Fishing");
            assertThat(activities).doesNotContain("Paddleboarding");
        }

        @Test
        void shouldAssess_moderate_whenWavesBetweenPoint6And1m() {
            // Arrange
            Map<String, Object> apiResponse = createApiResponseWithWaveHeight(0.8);
            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 1, "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> assessment = (Map<String, Object>) result.get("assessment");
            assertThat(assessment.get("suitability")).isEqualTo("Moderate");
            assertThat(assessment.get("recommendation")).isEqualTo("Suitable for experienced water sports enthusiasts");

            @SuppressWarnings("unchecked")
            List<String> activities = (List<String>) assessment.get("suitable_activities");
            assertThat(activities).contains("Sailing", "Windsurfing", "Fishing");
            assertThat(activities).doesNotContain("Swimming", "Kayaking");
        }

        @Test
        void shouldAssess_challenging_whenWavesBetween1And1Point5m() {
            // Arrange
            Map<String, Object> apiResponse = createApiResponseWithWaveHeight(1.2);
            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 1, "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> assessment = (Map<String, Object>) result.get("assessment");
            assertThat(assessment.get("suitability")).isEqualTo("Challenging");
            assertThat(assessment.get("recommendation")).isEqualTo("Only for experienced sailors and water sports athletes");

            @SuppressWarnings("unchecked")
            List<String> activities = (List<String>) assessment.get("suitable_activities");
            assertThat(activities).hasSize(2);
            assertThat(activities).contains("Sailing (experienced)", "Windsurfing");
        }

        @Test
        void shouldAssess_dangerous_whenWavesAbove1Point5m() {
            // Arrange
            Map<String, Object> apiResponse = createApiResponseWithWaveHeight(2.0);
            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 1, "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> assessment = (Map<String, Object>) result.get("assessment");
            assertThat(assessment.get("suitability")).isEqualTo("Dangerous");
            assertThat(assessment.get("recommendation")).isEqualTo("Not recommended for recreational water activities");

            @SuppressWarnings("unchecked")
            List<String> activities = (List<String>) assessment.get("suitable_activities");
            assertThat(activities).isEmpty();
        }
    }

    @Nested
    class ExtractCurrentConditionsTests {

        @Test
        void shouldExtractFirstValues_fromHourlyData() {
            // Arrange
            Map<String, Object> apiResponse = Map.of(
                    "hourly", Map.of(
                            "time", List.of("2023-07-01T12:00", "2023-07-01T13:00"),
                            "wave_height", List.of(0.6, 0.7),
                            "wave_direction", List.of(180.0, 185.0),
                            "wave_period", List.of(4.5, 4.8),
                            "swell_wave_height", List.of(0.3, 0.4),
                            "wind_wave_height", List.of(0.3, 0.35)
                    )
            );

            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 1, "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> current = (Map<String, Object>) result.get("current");

            assertThat(current.get("wave_height")).isEqualTo(0.6);
            assertThat(current.get("wave_direction")).isEqualTo(180.0);
            assertThat(current.get("wave_period")).isEqualTo(4.5);
            assertThat(current.get("swell_wave_height")).isEqualTo(0.3);
            assertThat(current.get("wind_wave_height")).isEqualTo(0.3);
            // Time should not be in current
            assertThat(current).doesNotContainKey("time");
        }

        @Test
        void shouldReturnEmptyCurrent_whenHourlyIsEmpty() {
            // Arrange
            Map<String, Object> apiResponse = Map.of("hourly", Map.of());
            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 1, "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> current = (Map<String, Object>) result.get("current");
            assertThat(current).isEmpty();
        }
    }

    @Nested
    class EdgeCaseTests {

        @Test
        void shouldHandleMissingWaveData_inAssessment() {
            // Arrange - No wave data in response
            Map<String, Object> apiResponse = Map.of(
                    "hourly", Map.of(
                            "time", List.of("2023-07-01T12:00")
                    )
            );

            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 1, "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> assessment = (Map<String, Object>) result.get("assessment");
            // Should assess based on 0.0 wave height (default)
            assertThat(assessment.get("suitability")).isEqualTo("Excellent");
            assertThat(assessment.get("wave_height_m")).isEqualTo(0.0);
        }

        @Test
        void shouldHandleNullValues_inHourlyData() {
            // Arrange
            Map<String, Object> apiResponse = Map.of(
                    "hourly", Map.of(
                            "wave_height", List.of(0.5),
                            "wave_direction", List.of(180.0)
                            // Missing swell and wind wave data
                    )
            );

            setupWebClientMock(apiResponse);

            // Act
            var result = service.getMarineConditions(46.4544, 6.5886, 1, "UTC").join();

            // Assert
            @SuppressWarnings("unchecked")
            Map<String, Object> assessment = (Map<String, Object>) result.get("assessment");
            assertThat(assessment.get("swell_height_m")).isEqualTo(0.0);
            assertThat(assessment.get("wind_wave_height_m")).isEqualTo(0.0);
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

    private Map<String, Object> createApiResponseWithWaveHeight(double waveHeight) {
        return Map.of(
                "hourly", Map.of(
                        "time", List.of("2023-07-01T12:00"),
                        "wave_height", List.of(waveHeight),
                        "wave_direction", List.of(180.0),
                        "swell_wave_height", List.of(waveHeight * 0.5),
                        "wind_wave_height", List.of(waveHeight * 0.5)
                )
        );
    }
}
