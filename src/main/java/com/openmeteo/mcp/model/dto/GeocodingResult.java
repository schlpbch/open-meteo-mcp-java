package com.openmeteo.mcp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Single geocoding result from location search.
 * <p>
 * Represents a location found by the geocoding API with its coordinates
 * and metadata.
 * </p>
 *
 * @param id            Unique location identifier
 * @param name          Location name
 * @param latitude      Latitude in decimal degrees
 * @param longitude     Longitude in decimal degrees
 * @param elevation     Elevation in meters
 * @param featureCode   Geographic feature code
 * @param countryCode   ISO 3166-1 alpha-2 country code
 * @param admin1        First-level administrative division
 * @param admin2        Second-level administrative division
 * @param admin3        Third-level administrative division
 * @param admin4        Fourth-level administrative division
 * @param timezone      Timezone identifier
 * @param population    Population (if applicable)
 * @param country       Country name
 * @param countryId     Country identifier
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeocodingResult(
        @JsonProperty("id")
        Long id,

        @JsonProperty("name")
        String name,

        @JsonProperty("latitude")
        double latitude,

        @JsonProperty("longitude")
        double longitude,

        @JsonProperty("elevation")
        Double elevation,

        @JsonProperty("feature_code")
        String featureCode,

        @JsonProperty("country_code")
        String countryCode,

        @JsonProperty("admin1")
        String admin1,

        @JsonProperty("admin2")
        String admin2,

        @JsonProperty("admin3")
        String admin3,

        @JsonProperty("admin4")
        String admin4,

        @JsonProperty("timezone")
        String timezone,

        @JsonProperty("population")
        Long population,

        @JsonProperty("country")
        String country,

        @JsonProperty("country_id")
        Long countryId
) implements Serializable {

    /**
     * Compact constructor with validation.
     */
    public GeocodingResult {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }
}
