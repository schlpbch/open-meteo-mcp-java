package com.openmeteo.mcp.resource;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ResourceService MCP resource annotations.
 */
class McpResourceAnnotationTest {

    @Test
    void shouldHaveMcpResourceAnnotationOnGetWeatherCodes() throws NoSuchMethodException {
        var method = ResourceService.class.getMethod("getWeatherCodes");

        assertThat(method.getAnnotation(McpResource.class)).isNotNull();
        McpResource annotation = method.getAnnotation(McpResource.class);
        assertThat(annotation.uri()).isEqualTo("weather://codes");
        assertThat(annotation.description()).contains("weather code");
    }

    @Test
    void shouldHaveMcpResourceAnnotationOnGetWeatherParameters() throws NoSuchMethodException {
        var method = ResourceService.class.getMethod("getWeatherParameters");

        assertThat(method.getAnnotation(McpResource.class)).isNotNull();
        McpResource annotation = method.getAnnotation(McpResource.class);
        assertThat(annotation.uri()).isEqualTo("weather://parameters");
        assertThat(annotation.description()).contains("parameters");
    }

    @Test
    void shouldHaveMcpResourceAnnotationOnGetAqiReference() throws NoSuchMethodException {
        var method = ResourceService.class.getMethod("getAqiReference");

        assertThat(method.getAnnotation(McpResource.class)).isNotNull();
        McpResource annotation = method.getAnnotation(McpResource.class);
        assertThat(annotation.uri()).isEqualTo("weather://aqi-reference");
        assertThat(annotation.description()).contains("AQI");
    }

    @Test
    void shouldHaveMcpResourceAnnotationOnGetSwissLocations() throws NoSuchMethodException {
        var method = ResourceService.class.getMethod("getSwissLocations");

        assertThat(method.getAnnotation(McpResource.class)).isNotNull();
        McpResource annotation = method.getAnnotation(McpResource.class);
        assertThat(annotation.uri()).isEqualTo("weather://swiss-locations");
        assertThat(annotation.description()).contains("Swiss");
    }

    @Test
    void shouldHaveAllMcpResourceAnnotationsWithValidUrisAndDescriptions() throws NoSuchMethodException {
        McpResource codesAnnotation = ResourceService.class
                .getMethod("getWeatherCodes")
                .getAnnotation(McpResource.class);

        McpResource parametersAnnotation = ResourceService.class
                .getMethod("getWeatherParameters")
                .getAnnotation(McpResource.class);

        McpResource aqiAnnotation = ResourceService.class
                .getMethod("getAqiReference")
                .getAnnotation(McpResource.class);

        McpResource locationsAnnotation = ResourceService.class
                .getMethod("getSwissLocations")
                .getAnnotation(McpResource.class);

        assertThat(codesAnnotation.uri()).isNotBlank();
        assertThat(codesAnnotation.description()).isNotBlank();

        assertThat(parametersAnnotation.uri()).isNotBlank();
        assertThat(parametersAnnotation.description()).isNotBlank();

        assertThat(aqiAnnotation.uri()).isNotBlank();
        assertThat(aqiAnnotation.description()).isNotBlank();

        assertThat(locationsAnnotation.uri()).isNotBlank();
        assertThat(locationsAnnotation.description()).isNotBlank();
    }

    @Test
    void shouldHaveCorrectResourceUris() throws NoSuchMethodException {
        McpResource codesAnnotation = ResourceService.class
                .getMethod("getWeatherCodes")
                .getAnnotation(McpResource.class);

        McpResource parametersAnnotation = ResourceService.class
                .getMethod("getWeatherParameters")
                .getAnnotation(McpResource.class);

        McpResource aqiAnnotation = ResourceService.class
                .getMethod("getAqiReference")
                .getAnnotation(McpResource.class);

        McpResource locationsAnnotation = ResourceService.class
                .getMethod("getSwissLocations")
                .getAnnotation(McpResource.class);

        assertThat(codesAnnotation.uri()).startsWith("weather://");
        assertThat(parametersAnnotation.uri()).startsWith("weather://");
        assertThat(aqiAnnotation.uri()).startsWith("weather://");
        assertThat(locationsAnnotation.uri()).startsWith("weather://");
    }
}
