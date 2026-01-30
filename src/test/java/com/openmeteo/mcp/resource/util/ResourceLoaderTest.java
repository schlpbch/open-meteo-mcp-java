package com.openmeteo.mcp.resource.util;

import com.openmeteo.mcp.exception.ResourceLoadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ResourceLoader.
 */
class ResourceLoaderTest {

    private ResourceLoader resourceLoader;

    @BeforeEach
    void setUp() {
        resourceLoader = new ResourceLoader();
    }

    @Test
    void shouldLoadWeatherCodes() {
        String content = resourceLoader.loadResource("data/weather-codes.json");

        assertThat(content).isNotBlank();
        assertThat(content).contains("\"codes\"");
        assertThat(content).contains("Clear sky");
    }

    @Test
    void shouldLoadWeatherParameters() {
        String content = resourceLoader.loadResource("data/weather-parameters.json");

        assertThat(content).isNotBlank();
        assertThat(content).contains("\"weatherParameters\"");
        assertThat(content).contains("temperature_2m");
    }

    @Test
    void shouldLoadAqiReference() {
        String content = resourceLoader.loadResource("data/aqi-reference.json");

        assertThat(content).isNotBlank();
        assertThat(content).contains("\"european_aqi\"");
        assertThat(content).contains("\"us_aqi\"");
    }

    @Test
    void shouldLoadSwissLocations() {
        String content = resourceLoader.loadResource("data/swiss-locations.json");

        assertThat(content).isNotBlank();
        assertThat(content).contains("\"cities\"");
        assertThat(content).contains("Zurich");
    }

    @Test
    void shouldThrowExceptionForMissingResource() {
        assertThatThrownBy(() ->
                resourceLoader.loadResource("data/nonexistent.json"))
                .isInstanceOf(ResourceLoadException.class)
                .hasMessageContaining("Resource not found");
    }

    @Test
    void shouldCheckResourceExists() {
        assertThat(resourceLoader.resourceExists("data/weather-codes.json")).isTrue();
        assertThat(resourceLoader.resourceExists("data/nonexistent.json")).isFalse();
    }
}
