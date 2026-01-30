package com.openmeteo.mcp.resource;

import com.openmeteo.mcp.resource.util.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for providing MCP resources.
 * <p>
 * Serves JSON data files as MCP resources for reference data lookup.
 * Resources include weather codes, parameters, AQI scales, and location data.
 * </p>
 */
@Service
public class ResourceService {

    private final ResourceLoader resourceLoader;

    public ResourceService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Get weather codes resource.
     * <p>
     * URI: weather://codes
     * </p>
     * <p>
     * Contains WMO weather code reference with descriptions, categories,
     * icons, and travel impact assessments.
     * </p>
     *
     * @return JSON string containing weather codes data
     */
    public String getWeatherCodes() {
        return resourceLoader.loadResource("data/weather-codes.json");
    }

    /**
     * Get weather parameters resource.
     * <p>
     * URI: weather://parameters
     * </p>
     * <p>
     * Documents available weather and snow parameters from Open-Meteo API
     * including hourly and daily parameters with units and descriptions.
     * </p>
     *
     * @return JSON string containing weather parameters data
     */
    public String getWeatherParameters() {
        return resourceLoader.loadResource("data/weather-parameters.json");
    }

    /**
     * Get AQI reference resource.
     * <p>
     * URI: weather://aqi-reference
     * </p>
     * <p>
     * Contains European and US AQI scales with health implications,
     * UV index guidance, and pollen level information.
     * </p>
     *
     * @return JSON string containing AQI reference data
     */
    public String getAqiReference() {
        return resourceLoader.loadResource("data/aqi-reference.json");
    }

    /**
     * Get Swiss locations resource.
     * <p>
     * URI: weather://swiss-locations
     * </p>
     * <p>
     * Contains major Swiss cities, mountains, passes, and lakes
     * with GPS coordinates and elevation data.
     * </p>
     *
     * @return JSON string containing Swiss locations data
     */
    public String getSwissLocations() {
        return resourceLoader.loadResource("data/swiss-locations.json");
    }

    /**
     * Get all available resources as a map.
     * <p>
     * Useful for MCP protocol registration.
     * </p>
     *
     * @return Map of URI to resource content
     */
    public Map<String, String> getAllResources() {
        return Map.of(
                "weather://codes", getWeatherCodes(),
                "weather://parameters", getWeatherParameters(),
                "weather://aqi-reference", getAqiReference(),
                "weather://swiss-locations", getSwissLocations()
        );
    }

    /**
     * Get resource by URI.
     * <p>
     * Returns null if URI is not recognized.
     * </p>
     *
     * @param uri Resource URI (e.g., "weather://codes")
     * @return Resource content as JSON string, or null if URI not found
     */
    public String getResourceByUri(String uri) {
        return switch (uri) {
            case "weather://codes" -> getWeatherCodes();
            case "weather://parameters" -> getWeatherParameters();
            case "weather://aqi-reference" -> getAqiReference();
            case "weather://swiss-locations" -> getSwissLocations();
            default -> null;
        };
    }
}
