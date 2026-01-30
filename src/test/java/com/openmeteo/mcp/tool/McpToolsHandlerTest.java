package com.openmeteo.mcp.tool;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpTool;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for McpToolsHandler MCP tool annotations.
 */
class McpToolsHandlerTest {

    @Test
    void shouldHaveMcpToolAnnotationOnSearchLocation() throws NoSuchMethodException {
        var method = McpToolsHandler.class.getMethod(
                "searchLocation", String.class, int.class, String.class, String.class
        );

        assertThat(method.getAnnotation(McpTool.class)).isNotNull();
        McpTool annotation = method.getAnnotation(McpTool.class);
        assertThat(annotation.description()).contains("geocoding");
        assertThat(annotation.description()).contains("location");
    }

    @Test
    void shouldHaveMcpToolAnnotationOnGetWeather() throws NoSuchMethodException {
        var method = McpToolsHandler.class.getMethod(
                "getWeather", double.class, double.class, int.class, String.class
        );

        assertThat(method.getAnnotation(McpTool.class)).isNotNull();
        McpTool annotation = method.getAnnotation(McpTool.class);
        assertThat(annotation.description()).contains("weather");
        assertThat(annotation.description()).contains("forecast");
    }

    @Test
    void shouldHaveMcpToolAnnotationOnGetSnowConditions() throws NoSuchMethodException {
        var method = McpToolsHandler.class.getMethod(
                "getSnowConditions", double.class, double.class, int.class, String.class
        );

        assertThat(method.getAnnotation(McpTool.class)).isNotNull();
        McpTool annotation = method.getAnnotation(McpTool.class);
        assertThat(annotation.description()).contains("snow");
        assertThat(annotation.description()).contains("ski");
    }

    @Test
    void shouldHaveMcpToolAnnotationOnGetAirQuality() throws NoSuchMethodException {
        var method = McpToolsHandler.class.getMethod(
                "getAirQuality", double.class, double.class, int.class, boolean.class, String.class
        );

        assertThat(method.getAnnotation(McpTool.class)).isNotNull();
        McpTool annotation = method.getAnnotation(McpTool.class);
        assertThat(annotation.description()).contains("air quality");
        assertThat(annotation.description()).contains("AQI");
    }

    @Test
    void shouldHaveAllMcpToolAnnotationsWithValidDescriptions() throws NoSuchMethodException {
        McpTool searchLocationAnnotation = McpToolsHandler.class
                .getMethod("searchLocation", String.class, int.class, String.class, String.class)
                .getAnnotation(McpTool.class);

        McpTool getWeatherAnnotation = McpToolsHandler.class
                .getMethod("getWeather", double.class, double.class, int.class, String.class)
                .getAnnotation(McpTool.class);

        McpTool getSnowConditionsAnnotation = McpToolsHandler.class
                .getMethod("getSnowConditions", double.class, double.class, int.class, String.class)
                .getAnnotation(McpTool.class);

        McpTool getAirQualityAnnotation = McpToolsHandler.class
                .getMethod("getAirQuality", double.class, double.class, int.class, boolean.class, String.class)
                .getAnnotation(McpTool.class);

        assertThat(searchLocationAnnotation.description()).isNotBlank();
        assertThat(getWeatherAnnotation.description()).isNotBlank();
        assertThat(getSnowConditionsAnnotation.description()).isNotBlank();
        assertThat(getAirQualityAnnotation.description()).isNotBlank();
    }
}
