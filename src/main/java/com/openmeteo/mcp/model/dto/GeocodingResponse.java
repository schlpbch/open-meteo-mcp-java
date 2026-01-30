package com.openmeteo.mcp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Geocoding response from Open-Meteo API.
 * <p>
 * Contains search results from the geocoding/location search API.
 * </p>
 *
 * @param results          List of location search results
 * @param generationtimeMs API generation time in milliseconds
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeocodingResponse(
        @JsonProperty("results")
        List<GeocodingResult> results,

        @JsonProperty("generationtime_ms")
        Double generationtimeMs
) implements Serializable {
}
