package com.openmeteo.mcp.client;

import com.openmeteo.mcp.exception.OpenMeteoException;
import com.openmeteo.mcp.model.dto.GeocodingResponse;
import com.openmeteo.mcp.model.dto.WeatherForecast;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for OpenMeteoClient.
 * <p>
 * Uses MockWebServer to test HTTP interactions without hitting the real API.
 * </p>
 */
class OpenMeteoClientTest {

    private MockWebServer mockServer;
    private OpenMeteoClient client;

    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();

        // Create WebClient pointed at mock server
        String baseUrl = mockServer.url("/").toString();

        WebClient weatherClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .build();

        // Reuse same client for all endpoints in tests
        client = new OpenMeteoClient(
                weatherClient,
                weatherClient,
                weatherClient,
                weatherClient
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    void shouldGetWeatherSuccessfully() throws Exception {
        // Arrange
        String mockResponse = """
                {
                    "latitude": 47.3769,
                    "longitude": 8.5417,
                    "timezone": "Europe/Zurich",
                    "elevation": 408.0,
                    "current_weather": {
                        "temperature": 15.5,
                        "windspeed": 10.0,
                        "winddirection": 180,
                        "weathercode": 1,
                        "time": "2024-01-30T12:00:00Z"
                    }
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockResponse));

        // Act
        CompletableFuture<WeatherForecast> future = client.getWeather(
                47.3769, 8.5417, 7, true, "Europe/Zurich"
        );
        WeatherForecast result = future.get(5, TimeUnit.SECONDS);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.latitude()).isEqualTo(47.3769);
        assertThat(result.longitude()).isEqualTo(8.5417);
        assertThat(result.timezone()).isEqualTo("Europe/Zurich");
        assertThat(result.currentWeather()).isNotNull();
        assertThat(result.currentWeather().temperature()).isEqualTo(15.5);
        assertThat(result.currentWeather().windspeed()).isEqualTo(10.0);
        assertThat(result.currentWeather().weathercode()).isEqualTo(1);

        // Verify request
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).contains("/forecast");
        assertThat(request.getPath()).contains("latitude=47.3769");
        assertThat(request.getPath()).contains("longitude=8.5417");
        assertThat(request.getPath()).contains("forecast_days=7");
        assertThat(request.getPath()).contains("timezone=Europe");
    }

    @Test
    void shouldHandleApiError() {
        // Arrange
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        CompletableFuture<WeatherForecast> future = client.getWeather(
                47.3769, 8.5417, 7, true, "Europe/Zurich"
        );

        assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(OpenMeteoException.class)
                .hasMessageContaining("500");
    }

    @Test
    void shouldClampForecastDaysToMaximum() throws Exception {
        // Arrange
        String mockResponse = """
                {
                    "latitude": 47.3769,
                    "longitude": 8.5417,
                    "timezone": "auto",
                    "current_weather": {
                        "temperature": 15.5,
                        "windspeed": 10.0,
                        "winddirection": 180,
                        "weathercode": 1,
                        "time": "2024-01-30T12:00:00Z"
                    }
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockResponse));

        // Act - Request 20 days (should be clamped to 16)
        CompletableFuture<WeatherForecast> future = client.getWeather(
                47.3769, 8.5417, 20, false, "auto"
        );
        future.get(5, TimeUnit.SECONDS);

        // Assert - Verify clamped to 16
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).contains("forecast_days=16");
    }

    @Test
    void shouldClampForecastDaysToMinimum() throws Exception {
        // Arrange
        String mockResponse = """
                {
                    "latitude": 47.3769,
                    "longitude": 8.5417,
                    "timezone": "auto",
                    "current_weather": {
                        "temperature": 15.5,
                        "windspeed": 10.0,
                        "winddirection": 180,
                        "weathercode": 1,
                        "time": "2024-01-30T12:00:00Z"
                    }
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockResponse));

        // Act - Request 0 days (should be clamped to 1)
        CompletableFuture<WeatherForecast> future = client.getWeather(
                47.3769, 8.5417, 0, false, "auto"
        );
        future.get(5, TimeUnit.SECONDS);

        // Assert - Verify clamped to 1
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).contains("forecast_days=1");
    }

    @Test
    void shouldSearchLocationSuccessfully() throws Exception {
        // Arrange
        String mockResponse = """
                {
                    "results": [
                        {
                            "id": 2657896,
                            "name": "Zurich",
                            "latitude": 47.3769,
                            "longitude": 8.5417,
                            "elevation": 408.0,
                            "timezone": "Europe/Zurich",
                            "country_code": "CH",
                            "country": "Switzerland"
                        }
                    ],
                    "generationtime_ms": 1.5
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockResponse));

        // Act
        CompletableFuture<GeocodingResponse> future = client.searchLocation(
                "Zurich", 10, "en", null
        );
        GeocodingResponse result = future.get(5, TimeUnit.SECONDS);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.results()).hasSize(1);
        assertThat(result.results().get(0).name()).isEqualTo("Zurich");
        assertThat(result.results().get(0).latitude()).isEqualTo(47.3769);
        assertThat(result.results().get(0).longitude()).isEqualTo(8.5417);
        assertThat(result.results().get(0).countryCode()).isEqualTo("CH");

        // Verify request
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).contains("/search");
        assertThat(request.getPath()).contains("name=Zurich");
        assertThat(request.getPath()).contains("count=10");
        assertThat(request.getPath()).contains("language=en");
    }

    @Test
    void shouldFilterLocationsByCountry() throws Exception {
        // Arrange
        String mockResponse = """
                {
                    "results": [
                        {
                            "id": 2657896,
                            "name": "Zurich",
                            "latitude": 47.3769,
                            "longitude": 8.5417,
                            "country_code": "CH",
                            "country": "Switzerland"
                        },
                        {
                            "id": 1234567,
                            "name": "Zurich",
                            "latitude": 40.7128,
                            "longitude": -74.0060,
                            "country_code": "US",
                            "country": "United States"
                        }
                    ],
                    "generationtime_ms": 1.5
                }
                """;

        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockResponse));

        // Act - Search with country filter
        CompletableFuture<GeocodingResponse> future = client.searchLocation(
                "Zurich", 10, "en", "CH"
        );
        GeocodingResponse result = future.get(5, TimeUnit.SECONDS);

        // Assert - Only Swiss result should remain after client-side filtering
        assertThat(result).isNotNull();
        assertThat(result.results()).hasSize(1);
        assertThat(result.results().get(0).countryCode()).isEqualTo("CH");
    }
}
