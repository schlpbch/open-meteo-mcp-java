package com.openmeteo.mcp.resource;

import com.openmeteo.mcp.resource.util.ResourceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ResourceService.
 */
@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceLoader resourceLoader;

    @InjectMocks
    private ResourceService resourceService;

    @Test
    void shouldGetWeatherCodes() {
        when(resourceLoader.loadResource("data/weather-codes.json"))
                .thenReturn("{\"codes\": []}");

        String result = resourceService.getWeatherCodes();

        assertThat(result).isEqualTo("{\"codes\": []}");
        verify(resourceLoader).loadResource("data/weather-codes.json");
    }

    @Test
    void shouldGetWeatherParameters() {
        when(resourceLoader.loadResource("data/weather-parameters.json"))
                .thenReturn("{\"weatherParameters\": {}}");

        String result = resourceService.getWeatherParameters();

        assertThat(result).isEqualTo("{\"weatherParameters\": {}}");
        verify(resourceLoader).loadResource("data/weather-parameters.json");
    }

    @Test
    void shouldGetAqiReference() {
        when(resourceLoader.loadResource("data/aqi-reference.json"))
                .thenReturn("{\"european_aqi\": {}}");

        String result = resourceService.getAqiReference();

        assertThat(result).isEqualTo("{\"european_aqi\": {}}");
        verify(resourceLoader).loadResource("data/aqi-reference.json");
    }

    @Test
    void shouldGetSwissLocations() {
        when(resourceLoader.loadResource("data/swiss-locations.json"))
                .thenReturn("{\"cities\": []}");

        String result = resourceService.getSwissLocations();

        assertThat(result).isEqualTo("{\"cities\": []}");
        verify(resourceLoader).loadResource("data/swiss-locations.json");
    }

    @Test
    void shouldGetAllResources() {
        when(resourceLoader.loadResource(anyString()))
                .thenReturn("{}");

        Map<String, String> resources = resourceService.getAllResources();

        assertThat(resources).hasSize(4);
        assertThat(resources).containsKeys(
                "weather://codes",
                "weather://parameters",
                "weather://aqi-reference",
                "weather://swiss-locations"
        );
    }

    @Test
    void shouldGetResourceByUri() {
        when(resourceLoader.loadResource("data/weather-codes.json"))
                .thenReturn("{\"codes\": []}");

        String result = resourceService.getResourceByUri("weather://codes");

        assertThat(result).isEqualTo("{\"codes\": []}");
    }

    @Test
    void shouldReturnNullForUnknownUri() {
        String result = resourceService.getResourceByUri("weather://unknown");

        assertThat(result).isNull();
    }
}
