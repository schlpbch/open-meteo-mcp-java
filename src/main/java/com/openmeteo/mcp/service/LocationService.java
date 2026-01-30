package com.openmeteo.mcp.service;

import com.openmeteo.mcp.client.OpenMeteoClient;
import com.openmeteo.mcp.model.dto.GeocodingResponse;
import com.openmeteo.mcp.model.dto.GeocodingResult;
import com.openmeteo.mcp.service.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for geocoding and location search operations.
 * <p>
 * Provides location search and best-match finding functionality.
 * </p>
 */
@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final OpenMeteoClient client;

    public LocationService(OpenMeteoClient client) {
        this.client = client;
    }

    /**
     * Searches for locations by name.
     *
     * @param name     Location name to search for
     * @param count    Maximum number of results (1-100)
     * @param language Language code for results
     * @param country  Optional country code filter (ISO 3166-1 alpha-2)
     * @return CompletableFuture with GeocodingResponse
     */
    public CompletableFuture<GeocodingResponse> searchLocation(
            String name,
            int count,
            String language,
            String country
    ) {
        // Validate inputs
        ValidationUtil.validateNotBlank(name, "Location name");

        // Clamp count to API limits (1-100)
        int clampedCount = ValidationUtil.clampForecastDays(count, 1, 100);

        log.info("Searching location: name='{}', count={}, country='{}'",
                name, clampedCount, country);

        // Delegate to client
        return client.searchLocation(name, clampedCount, language, country);
    }

    /**
     * Searches for a single best-match location.
     *
     * @param name    Location name to search for
     * @param country ISO 3166-1 alpha-2 country code
     * @return CompletableFuture with Optional GeocodingResult
     */
    public CompletableFuture<Optional<GeocodingResult>> findBestMatch(
            String name,
            String country
    ) {
        return searchLocation(name, 1, "en", country)
                .thenApply(response -> {
                    if (response.results() == null || response.results().isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of(response.results().get(0));
                });
    }
}
