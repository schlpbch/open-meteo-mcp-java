package com.openmeteo.mcp.model.request;

import java.io.Serializable;

/**
 * Request parameters for location search (geocoding) API.
 * <p>
 * Contains input parameters for searching locations by name.
 * </p>
 *
 * @param name     Location name to search for
 * @param count    Maximum number of results (1-100)
 * @param language Language code for results (default: "en")
 * @param country  Optional country code filter (ISO 3166-1 alpha-2)
 */
public record LocationSearchRequest(
        String name,
        int count,
        String language,
        String country
) implements Serializable {

    /**
     * Compact constructor with validation and defaults.
     */
    public LocationSearchRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Location name cannot be null or blank");
        }
        if (count < 1 || count > 100) {
            throw new IllegalArgumentException("Result count must be between 1 and 100");
        }
        if (language == null || language.isBlank()) {
            language = "en";
        }
    }

    /**
     * Creates a default LocationSearchRequest with 10 results in English.
     *
     * @param name Location name to search for
     * @return LocationSearchRequest with defaults
     */
    public static LocationSearchRequest withDefaults(String name) {
        return new LocationSearchRequest(name, 10, "en", null);
    }

    /**
     * Creates a LocationSearchRequest with country filter.
     *
     * @param name    Location name to search for
     * @param country ISO 3166-1 alpha-2 country code
     * @return LocationSearchRequest with country filter
     */
    public static LocationSearchRequest withCountry(String name, String country) {
        return new LocationSearchRequest(name, 10, "en", country);
    }
}
