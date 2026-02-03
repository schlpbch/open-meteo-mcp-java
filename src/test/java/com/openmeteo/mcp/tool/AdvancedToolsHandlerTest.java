package com.openmeteo.mcp.tool;

import com.openmeteo.mcp.model.dto.GeocodingResponse;
import com.openmeteo.mcp.model.dto.GeocodingResult;
import com.openmeteo.mcp.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdvancedToolsHandler.
 * Tests the 7 advanced MCP tools with mocked service dependencies.
 */
@ExtendWith(MockitoExtension.class)
class AdvancedToolsHandlerTest {

    @Mock
    private WeatherService weatherService;

    @Mock
    private AirQualityService airQualityService;

    @Mock
    private LocationService locationService;

    @Mock
    private HistoricalWeatherService historicalWeatherService;

    @Mock
    private MarineConditionsService marineConditionsService;

    private AdvancedToolsHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AdvancedToolsHandler(
                weatherService,
                airQualityService,
                locationService,
                historicalWeatherService,
                marineConditionsService
        );
    }

    // ========== getWeatherAlerts Tests ==========

    @Test
    void getWeatherAlerts_shouldReturnAlerts_whenConditionsAreExtreme() {
        // Arrange
        Map<String, Object> weatherData = Map.of(
                "current", Map.of(
                        "temperature", 35.0,
                        "windspeed", 90.0,
                        "weathercode", 95  // Thunderstorm
                ),
                "hourly", Map.of(
                        "temperature_2m", List.of(35.0, 36.0, 37.0),
                        "windspeed_10m", List.of(90.0, 85.0, 80.0)
                ),
                "daily", Map.of(
                        "uv_index_max", List.of(9.0)
                ),
                "timezone", "Europe/Zurich"
        );

        when(weatherService.getWeatherWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(weatherData));

        // Act
        var result = handler.getWeatherAlerts(47.3769, 8.5417, 24, "Europe/Zurich").join();

        // Assert
        assertThat(result).containsKey("alerts");
        assertThat(result.get("latitude")).isEqualTo(47.3769);
        assertThat(result.get("longitude")).isEqualTo(8.5417);
        assertThat(result.get("timezone")).isEqualTo("Europe/Zurich");
        verify(weatherService).getWeatherWithInterpretation(47.3769, 8.5417, 2, true, "Europe/Zurich");
    }

    @Test
    void getWeatherAlerts_shouldReturnEmptyAlerts_whenConditionsAreNormal() {
        // Arrange
        Map<String, Object> weatherData = Map.of(
                "current", Map.of(
                        "temperature", 20.0,
                        "windspeed", 10.0,
                        "weathercode", 0  // Clear
                ),
                "hourly", Map.of(
                        "temperature_2m", List.of(20.0, 21.0, 22.0),
                        "windspeed_10m", List.of(10.0, 12.0, 11.0)
                ),
                "daily", Map.of(
                        "uv_index_max", List.of(5.0)
                ),
                "timezone", "UTC"
        );

        when(weatherService.getWeatherWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(weatherData));

        // Act
        var result = handler.getWeatherAlerts(47.3769, 8.5417, 0, null).join();

        // Assert
        assertThat(result).containsKey("alerts");
        assertThat(result.get("timezone")).isEqualTo("UTC");
        // Default forecastHours should be 24, so forecastDays should be 2
        verify(weatherService).getWeatherWithInterpretation(47.3769, 8.5417, 2, true, "auto");
    }

    // ========== getComfortIndex Tests ==========

    @Test
    void getComfortIndex_shouldReturnComfortScore() {
        // Arrange
        Map<String, Object> weatherData = Map.of(
                "current", Map.of(
                        "temperature", 22.0,
                        "windspeed", 10.0,
                        "humidity", 50,
                        "weathercode", 0
                ),
                "timezone", "Europe/Zurich"
        );
        Map<String, Object> airQualityData = Map.of(
                "current", Map.of(
                        "european_aqi", 25.0,
                        "pm2_5", 10.0,
                        "pm10", 15.0,
                        "uv_index", 4.0
                )
        );

        when(weatherService.getWeatherWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(weatherData));
        when(airQualityService.getAirQualityWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(airQualityData));

        // Act
        var result = handler.getComfortIndex(47.3769, 8.5417, "Europe/Zurich").join();

        // Assert
        assertThat(result).containsKey("comfort_index");
        assertThat(result.get("latitude")).isEqualTo(47.3769);
        assertThat(result.get("longitude")).isEqualTo(8.5417);
        assertThat(result.get("timezone")).isEqualTo("Europe/Zurich");

        @SuppressWarnings("unchecked")
        Map<String, Object> comfortIndex = (Map<String, Object>) result.get("comfort_index");
        assertThat(comfortIndex).containsKey("overall");
        assertThat(comfortIndex).containsKey("recommendation");
    }

    @Test
    void getComfortIndex_shouldUseDefaultTimezone_whenNull() {
        // Arrange
        Map<String, Object> weatherData = Map.of(
                "current", Map.of("temperature", 20.0),
                "timezone", "UTC"
        );
        Map<String, Object> airQualityData = Map.of(
                "current", Map.of("european_aqi", 20.0)
        );

        when(weatherService.getWeatherWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(weatherData));
        when(airQualityService.getAirQualityWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(airQualityData));

        // Act
        var result = handler.getComfortIndex(47.3769, 8.5417, null).join();

        // Assert
        verify(weatherService).getWeatherWithInterpretation(47.3769, 8.5417, 1, false, "auto");
    }

    // ========== getAstronomy Tests ==========

    @Test
    void getAstronomy_shouldReturnAstronomyData_whenTimezoneProvided() {
        // Act
        var result = handler.getAstronomy(47.3769, 8.5417, "Europe/Zurich").join();

        // Assert
        assertThat(result).containsKey("astronomy");
        assertThat(result.get("latitude")).isEqualTo(47.3769);
        assertThat(result.get("longitude")).isEqualTo(8.5417);
        assertThat(result.get("timezone")).isEqualTo("Europe/Zurich");

        @SuppressWarnings("unchecked")
        Map<String, Object> astronomy = (Map<String, Object>) result.get("astronomy");
        assertThat(astronomy).containsKey("sunrise");
        assertThat(astronomy).containsKey("sunset");
    }

    @Test
    void getAstronomy_shouldFetchTimezone_whenAuto() {
        // Arrange
        Map<String, Object> weatherData = Map.of("timezone", "Europe/Zurich");

        when(weatherService.getWeatherWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(weatherData));

        // Act
        var result = handler.getAstronomy(47.3769, 8.5417, "auto").join();

        // Assert
        assertThat(result).containsKey("astronomy");
        assertThat(result.get("timezone")).isEqualTo("Europe/Zurich");
        verify(weatherService).getWeatherWithInterpretation(47.3769, 8.5417, 1, false, "auto");
    }

    @Test
    void getAstronomy_shouldFetchTimezone_whenNull() {
        // Arrange
        Map<String, Object> weatherData = Map.of("timezone", "UTC");

        when(weatherService.getWeatherWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(weatherData));

        // Act
        var result = handler.getAstronomy(47.3769, 8.5417, null).join();

        // Assert
        assertThat(result).containsKey("astronomy");
        verify(weatherService).getWeatherWithInterpretation(47.3769, 8.5417, 1, false, "auto");
    }

    // ========== searchLocationSwiss Tests ==========

    @Test
    void searchLocationSwiss_shouldReturnResults_whenFound() {
        // Arrange
        var geocodingResult = new GeocodingResult(
                1L, "Zürich", 47.3769, 8.5417, 408.0, "PPLA", "CH",
                "Zurich", null, null, null, "Europe/Zurich", 400000L, "Switzerland", 756L
        );
        var geocodingResponse = new GeocodingResponse(List.of(geocodingResult), 1.5);

        when(locationService.searchLocation(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(geocodingResponse));

        // Act
        var result = handler.searchLocationSwiss("Zürich", false, "en", 10).join();

        // Assert
        assertThat(result.get("query")).isEqualTo("Zürich");
        assertThat(result.get("country")).isEqualTo("CH");
        assertThat(result.get("total")).isEqualTo(1);

        @SuppressWarnings("unchecked")
        List<GeocodingResult> results = (List<GeocodingResult>) result.get("results");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).name()).isEqualTo("Zürich");

        verify(locationService).searchLocation("Zürich", 20, "en", "CH");
    }

    @Test
    void searchLocationSwiss_shouldReturnEmpty_whenNoResults() {
        // Arrange
        var geocodingResponse = new GeocodingResponse(null, 0.5);

        when(locationService.searchLocation(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(geocodingResponse));

        // Act
        var result = handler.searchLocationSwiss("NonExistentPlace", true, null, 0).join();

        // Assert
        assertThat(result.get("query")).isEqualTo("NonExistentPlace");
        assertThat(result.get("total")).isEqualTo(0);

        @SuppressWarnings("unchecked")
        List<GeocodingResult> results = (List<GeocodingResult>) result.get("results");
        assertThat(results).isEmpty();

        // Default language should be "en", default count should be 10 (doubled to 20)
        verify(locationService).searchLocation("NonExistentPlace", 20, "en", "CH");
    }

    @Test
    void searchLocationSwiss_shouldFilterFeatures_whenIncludeFeaturesIsFalse() {
        // Arrange
        var city = new GeocodingResult(
                1L, "Bern", 46.9479, 7.4474, 540.0, "PPLC", "CH",
                "Bern", null, null, null, "Europe/Zurich", 130000L, "Switzerland", 756L
        );
        var mountain = new GeocodingResult(
                2L, "Matterhorn", 45.9763, 7.6586, 4478.0, "MT", "CH",
                "Valais", null, null, null, "Europe/Zurich", null, "Switzerland", 756L
        );
        var geocodingResponse = new GeocodingResponse(List.of(city, mountain), 1.5);

        when(locationService.searchLocation(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(geocodingResponse));

        // Act - includeFeatures = false should filter out mountains
        var result = handler.searchLocationSwiss("Bern", false, "en", 10).join();

        // Assert
        @SuppressWarnings("unchecked")
        List<GeocodingResult> results = (List<GeocodingResult>) result.get("results");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).name()).isEqualTo("Bern");
    }

    // ========== compareLocations Tests ==========

    @Test
    void compareLocations_shouldRankByWarmest() {
        // Arrange
        setupCompareLocationsWeatherMocks();

        List<Map<String, Object>> locations = new ArrayList<>();
        locations.add(createLocationMap("Zurich", 47.3769, 8.5417));
        locations.add(createLocationMap("Geneva", 46.2044, 6.1432));

        // Act
        var result = handler.compareLocations(locations, "warmest", 1).join();

        // Assert
        assertThat(result.get("criteria")).isEqualTo("warmest");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> locationResults = (List<Map<String, Object>>) result.get("locations");
        assertThat(locationResults).hasSize(2);
        // First should be warmest
        assertThat(result).containsKey("winner");
    }

    @Test
    void compareLocations_shouldRankByCalmest() {
        // Arrange
        setupCompareLocationsWeatherMocks();

        List<Map<String, Object>> locations = new ArrayList<>();
        locations.add(createLocationMap("Zurich", 47.3769, 8.5417));

        // Act
        var result = handler.compareLocations(locations, "calmest", 1).join();

        // Assert
        assertThat(result.get("criteria")).isEqualTo("calmest");
        assertThat(result).containsKey("comparison_timestamp");
    }

    @Test
    void compareLocations_shouldRankBySunniest() {
        // Arrange
        setupCompareLocationsWeatherMocks();

        List<Map<String, Object>> locations = new ArrayList<>();
        locations.add(createLocationMap("Lugano", 46.0037, 8.9511));

        // Act
        var result = handler.compareLocations(locations, "sunniest", 1).join();

        // Assert
        assertThat(result.get("criteria")).isEqualTo("sunniest");
    }

    @Test
    void compareLocations_shouldRankByBestAirQuality() {
        // Arrange
        setupCompareLocationsWeatherMocks();

        List<Map<String, Object>> locations = new ArrayList<>();
        locations.add(createLocationMap("Interlaken", 46.6863, 7.8632));

        // Act
        var result = handler.compareLocations(locations, "best_air_quality", 1).join();

        // Assert
        assertThat(result.get("criteria")).isEqualTo("best_air_quality");
    }

    @Test
    void compareLocations_shouldRankByBestOverall_asDefault() {
        // Arrange
        setupCompareLocationsWeatherMocks();

        List<Map<String, Object>> locations = new ArrayList<>();
        locations.add(createLocationMap("Basel", 47.5596, 7.5886));

        // Act - null criteria should default to best_overall
        var result = handler.compareLocations(locations, null, 0).join();

        // Assert
        assertThat(result.get("criteria")).isEqualTo("best_overall");
    }

    // ========== getHistoricalWeather Tests ==========

    @Test
    void getHistoricalWeather_shouldReturnData() {
        // Arrange
        Map<String, Object> historicalData = Map.of(
                "latitude", 47.3769,
                "longitude", 8.5417,
                "start_date", "2023-01-01",
                "end_date", "2023-01-31",
                "daily", Map.of(
                        "temperature_2m_max", List.of(5.0, 6.0, 7.0),
                        "temperature_2m_min", List.of(-2.0, -1.0, 0.0)
                ),
                "statistics", Map.of(
                        "temperature", Map.of("max", 7.0, "min", -2.0)
                )
        );

        when(historicalWeatherService.getHistoricalWeather(anyDouble(), anyDouble(), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(historicalData));

        // Act
        var result = handler.getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-01-31", "Europe/Zurich").join();

        // Assert
        assertThat(result.get("latitude")).isEqualTo(47.3769);
        assertThat(result).containsKey("statistics");
        verify(historicalWeatherService).getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-01-31", "Europe/Zurich");
    }

    @Test
    void getHistoricalWeather_shouldUseAutoTimezone_whenNull() {
        // Arrange
        Map<String, Object> historicalData = Map.of("latitude", 47.3769, "longitude", 8.5417);

        when(historicalWeatherService.getHistoricalWeather(anyDouble(), anyDouble(), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(historicalData));

        // Act
        handler.getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-01-31", null).join();

        // Assert
        verify(historicalWeatherService).getHistoricalWeather(47.3769, 8.5417, "2023-01-01", "2023-01-31", "auto");
    }

    // ========== getMarineConditions Tests ==========

    @Test
    void getMarineConditions_shouldReturnData() {
        // Arrange
        Map<String, Object> marineData = Map.of(
                "latitude", 46.4544,
                "longitude", 6.5886,
                "current", Map.of(
                        "wave_height", 0.3,
                        "wave_direction", 180.0
                ),
                "assessment", Map.of(
                        "suitability", "Good",
                        "suitable_activities", List.of("Swimming", "Kayaking")
                )
        );

        when(marineConditionsService.getMarineConditions(anyDouble(), anyDouble(), anyInt(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(marineData));

        // Act
        var result = handler.getMarineConditions(46.4544, 6.5886, 7, "Europe/Zurich").join();

        // Assert
        assertThat(result.get("latitude")).isEqualTo(46.4544);
        assertThat(result).containsKey("assessment");
        verify(marineConditionsService).getMarineConditions(46.4544, 6.5886, 7, "Europe/Zurich");
    }

    @Test
    void getMarineConditions_shouldUseDefaults_whenParametersInvalid() {
        // Arrange
        Map<String, Object> marineData = Map.of("latitude", 46.4544, "longitude", 6.5886);

        when(marineConditionsService.getMarineConditions(anyDouble(), anyDouble(), anyInt(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(marineData));

        // Act
        handler.getMarineConditions(46.4544, 6.5886, -1, "").join();

        // Assert
        verify(marineConditionsService).getMarineConditions(46.4544, 6.5886, 7, "auto");
    }

    // ========== Helper Methods ==========

    private void setupCompareLocationsWeatherMocks() {
        Map<String, Object> weatherData = Map.of(
                "current", Map.of(
                        "temperature", 20.0,
                        "windspeed", 15.0,
                        "weathercode", 1
                ),
                "timezone", "Europe/Zurich"
        );
        Map<String, Object> airQualityData = Map.of(
                "current", Map.of(
                        "european_aqi", 30.0
                )
        );

        when(weatherService.getWeatherWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(weatherData));
        when(airQualityService.getAirQualityWithInterpretation(anyDouble(), anyDouble(), anyInt(), anyBoolean(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(airQualityData));
    }

    private Map<String, Object> createLocationMap(String name, double latitude, double longitude) {
        Map<String, Object> location = new HashMap<>();
        location.put("name", name);
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        return location;
    }
}
