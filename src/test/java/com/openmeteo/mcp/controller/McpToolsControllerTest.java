package com.openmeteo.mcp.controller;

import com.openmeteo.mcp.model.dto.GeocodingResponse;
import com.openmeteo.mcp.tool.McpToolsController;
import com.openmeteo.mcp.tool.McpToolsHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpToolsControllerTest {

    @Mock McpToolsHandler toolsHandler;

    McpToolsController controller;

    @BeforeEach
    void setUp() {
        controller = new McpToolsController(toolsHandler);
    }

    @Nested
    class SearchLocationTests {
        @Test
        void returnsResults() {
            var response = mock(GeocodingResponse.class);
            when(toolsHandler.searchLocation("Zurich", 10, "en", "CH"))
                    .thenReturn(CompletableFuture.completedFuture(response));

            var result = controller.searchLocation("Zurich", 10, "en", "CH").join();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(response, result.getBody());
        }

        @Test
        void returns500OnError() {
            when(toolsHandler.searchLocation(any(), anyInt(), any(), any()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("API error")));

            var result = controller.searchLocation("Zurich", 10, "en", "").join();

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        }
    }

    @Nested
    class GetWeatherTests {
        @Test
        void returnsWeatherData() {
            var data = Map.<String, Object>of("temperature", 20.5);
            when(toolsHandler.getWeather(47.3, 8.5, 7, "UTC"))
                    .thenReturn(CompletableFuture.completedFuture(data));

            var result = controller.getWeather(47.3, 8.5, 7, "UTC").join();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(20.5, result.getBody().get("temperature"));
        }

        @Test
        void returns500OnError() {
            when(toolsHandler.getWeather(anyDouble(), anyDouble(), anyInt(), any()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("timeout")));

            var result = controller.getWeather(0, 0, 7, "UTC").join();

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        }
    }

    @Nested
    class GetSnowConditionsTests {
        @Test
        void returnsSnowData() {
            var data = Map.<String, Object>of("snowDepth", 50.0);
            when(toolsHandler.getSnowConditions(46.0, 8.0, 7, "UTC"))
                    .thenReturn(CompletableFuture.completedFuture(data));

            var result = controller.getSnowConditions(46.0, 8.0, 7, "UTC").join();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(50.0, result.getBody().get("snowDepth"));
        }

        @Test
        void returns500OnError() {
            when(toolsHandler.getSnowConditions(anyDouble(), anyDouble(), anyInt(), any()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("error")));

            var result = controller.getSnowConditions(46.0, 8.0, 7, "UTC").join();

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        }
    }

    @Nested
    class GetAirQualityTests {
        @Test
        void returnsAirQualityData() {
            var data = Map.<String, Object>of("aqi", 42);
            when(toolsHandler.getAirQuality(52.5, 13.4, 3, true, "UTC"))
                    .thenReturn(CompletableFuture.completedFuture(data));

            var result = controller.getAirQuality(52.5, 13.4, 3, true, "UTC").join();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(42, result.getBody().get("aqi"));
        }

        @Test
        void returns500OnError() {
            when(toolsHandler.getAirQuality(anyDouble(), anyDouble(), anyInt(), anyBoolean(), any()))
                    .thenReturn(CompletableFuture.failedFuture(new RuntimeException("error")));

            var result = controller.getAirQuality(52.5, 13.4, 3, false, "UTC").join();

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        }
    }

    @Nested
    class HealthTests {
        @Test
        void returnsOk() {
            var result = controller.health();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("OK", result.getBody().get("status"));
        }
    }
}
