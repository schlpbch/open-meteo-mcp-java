package com.openmeteo.mcp.client;

import com.openmeteo.mcp.exception.OpenMeteoException;
import com.openmeteo.mcp.model.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Client for the Open-Meteo API.
 * <p>
 * Provides async methods for fetching weather, snow conditions,
 * air quality, and geocoding data using CompletableFuture.
 * </p>
 */
@Component
public class OpenMeteoClient {

    private static final Logger log = LoggerFactory.getLogger(OpenMeteoClient.class);

    private final WebClient weatherWebClient;
    private final WebClient airQualityWebClient;
    private final WebClient geocodingWebClient;
    private final WebClient marineWebClient;

    /**
     * Constructor with dependency injection of WebClient beans.
     *
     * @param weatherWebClient     WebClient for weather API
     * @param airQualityWebClient  WebClient for air quality API
     * @param geocodingWebClient   WebClient for geocoding API
     * @param marineWebClient      WebClient for marine API
     */
    public OpenMeteoClient(
            @Qualifier("weatherWebClient") WebClient weatherWebClient,
            @Qualifier("airQualityWebClient") WebClient airQualityWebClient,
            @Qualifier("geocodingWebClient") WebClient geocodingWebClient,
            @Qualifier("marineWebClient") WebClient marineWebClient
    ) {
        this.weatherWebClient = weatherWebClient;
        this.airQualityWebClient = airQualityWebClient;
        this.geocodingWebClient = geocodingWebClient;
        this.marineWebClient = marineWebClient;
    }

    /**
     * Fetches weather forecast for the specified location.
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-16)
     * @param includeHourly Whether to include hourly forecast data
     * @param timezone      Timezone identifier
     * @return CompletableFuture with WeatherForecast
     */
    public CompletableFuture<WeatherForecast> getWeather(
            double latitude,
            double longitude,
            int forecastDays,
            boolean includeHourly,
            String timezone
    ) {
        // Clamp forecast days to 1-16
        int clampedDays = Math.max(1, Math.min(forecastDays, 16));

        // Build query parameters
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("latitude", String.valueOf(latitude));
        params.add("longitude", String.valueOf(longitude));
        params.add("forecast_days", String.valueOf(clampedDays));
        params.add("timezone", timezone);
        params.add("current_weather", "true");

        // Add daily parameters (always included)
        params.add("daily", String.join(",", List.of(
                "temperature_2m_max", "temperature_2m_min",
                "apparent_temperature_max", "apparent_temperature_min",
                "precipitation_sum", "rain_sum", "snowfall_sum",
                "weathercode", "sunrise", "sunset",
                "windspeed_10m_max", "windgusts_10m_max", "winddirection_10m_dominant",
                "precipitation_probability_max", "uv_index_max"
        )));

        // Add hourly parameters (conditional)
        if (includeHourly) {
            params.add("hourly", String.join(",", List.of(
                    "temperature_2m", "apparent_temperature", "precipitation", "rain", "snowfall", "snow_depth",
                    "weathercode", "cloudcover", "visibility",
                    "windspeed_10m", "winddirection_10m", "windgusts_10m",
                    "surface_pressure", "relativehumidity_2m", "dewpoint_2m",
                    "precipitation_probability", "uv_index"
            )));
        }

        log.debug("Fetching weather for lat={}, lon={}, days={}, includeHourly={}",
                latitude, longitude, clampedDays, includeHourly);

        return weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParams(params)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToMono(WeatherForecast.class)
                .toFuture();
    }

    /**
     * Fetches snow conditions and mountain weather for the specified location.
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-16)
     * @param includeHourly Whether to include hourly snow data
     * @param timezone      Timezone identifier
     * @return CompletableFuture with SnowConditions
     */
    public CompletableFuture<SnowConditions> getSnowConditions(
            double latitude,
            double longitude,
            int forecastDays,
            boolean includeHourly,
            String timezone
    ) {
        // Clamp forecast days to 1-16
        int clampedDays = Math.max(1, Math.min(forecastDays, 16));

        // Build query parameters
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("latitude", String.valueOf(latitude));
        params.add("longitude", String.valueOf(longitude));
        params.add("forecast_days", String.valueOf(clampedDays));
        params.add("timezone", timezone);
        params.add("current_weather", "true");

        // Add daily parameters
        params.add("daily", String.join(",", List.of(
                "temperature_2m_max", "temperature_2m_min",
                "snowfall_sum", "weathercode",
                "windspeed_10m_max", "windgusts_10m_max", "winddirection_10m_dominant"
        )));

        // Add hourly parameters (conditional)
        if (includeHourly) {
            params.add("hourly", String.join(",", List.of(
                    "temperature_2m", "snowfall", "snow_depth",
                    "weathercode", "cloudcover",
                    "windspeed_10m", "winddirection_10m", "windgusts_10m",
                    "freezinglevel_height"
            )));
        }

        log.debug("Fetching snow conditions for lat={}, lon={}, days={}", latitude, longitude, clampedDays);

        return weatherWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParams(params)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToMono(SnowConditions.class)
                .toFuture();
    }

    /**
     * Fetches air quality forecast for the specified location.
     *
     * @param latitude      Latitude in decimal degrees
     * @param longitude     Longitude in decimal degrees
     * @param forecastDays  Number of forecast days (1-5)
     * @param includePollen Whether to include pollen data (Europe only)
     * @param timezone      Timezone identifier
     * @return CompletableFuture with AirQualityForecast
     */
    public CompletableFuture<AirQualityForecast> getAirQuality(
            double latitude,
            double longitude,
            int forecastDays,
            boolean includePollen,
            String timezone
    ) {
        // Clamp forecast days to 1-5 for air quality
        int clampedDays = Math.max(1, Math.min(forecastDays, 5));

        // Build query parameters
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("latitude", String.valueOf(latitude));
        params.add("longitude", String.valueOf(longitude));
        params.add("forecast_days", String.valueOf(clampedDays));
        params.add("timezone", timezone);

        // Build current parameters
        List<String> currentParams = List.of(
                "european_aqi", "us_aqi",
                "pm10", "pm2_5",
                "carbon_monoxide", "nitrogen_dioxide", "sulphur_dioxide", "ozone",
                "uv_index"
        );
        params.add("current", String.join(",", currentParams));

        // Build hourly parameters
        List<String> hourlyParamsList = new java.util.ArrayList<>(List.of(
                "european_aqi", "us_aqi",
                "pm10", "pm2_5",
                "carbon_monoxide", "nitrogen_dioxide", "sulphur_dioxide", "ozone",
                "uv_index"
        ));

        // Add pollen parameters if requested
        if (includePollen) {
            hourlyParamsList.addAll(List.of(
                    "alder_pollen", "birch_pollen", "grass_pollen",
                    "mugwort_pollen", "olive_pollen", "ragweed_pollen"
            ));
        }

        params.add("hourly", String.join(",", hourlyParamsList));

        log.debug("Fetching air quality for lat={}, lon={}, days={}, includePollen={}",
                latitude, longitude, clampedDays, includePollen);

        return airQualityWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/air-quality")
                        .queryParams(params)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToMono(AirQualityForecast.class)
                .toFuture();
    }

    /**
     * Searches for locations by name using the geocoding API.
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
        // Clamp count to 1-100
        int clampedCount = Math.max(1, Math.min(count, 100));

        log.debug("Searching location: name={}, count={}, language={}, country={}",
                name, clampedCount, language, country);

        return geocodingWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                            .path("/search")
                            .queryParam("name", name)
                            .queryParam("count", clampedCount)
                            .queryParam("language", language);

                    // Only add country parameter if provided
                    if (country != null && !country.isEmpty()) {
                        builder.queryParam("country", country);
                    }

                    return builder.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleErrorResponse)
                .bodyToMono(GeocodingResponse.class)
                .map(response -> filterByCountry(response, country))
                .toFuture();
    }

    /**
     * Filters geocoding results by country code (client-side filtering).
     * <p>
     * The API only provides "bias" via the country parameter, so we perform
     * additional client-side filtering to ensure results match the country.
     * </p>
     *
     * @param response GeocodingResponse to filter
     * @param country  Country code to filter by
     * @return Filtered GeocodingResponse
     */
    private GeocodingResponse filterByCountry(GeocodingResponse response, String country) {
        if (country == null || country.isEmpty() || response.results() == null) {
            return response;
        }

        List<GeocodingResult> filtered = response.results().stream()
                .filter(r -> r.countryCode() != null &&
                        r.countryCode().equalsIgnoreCase(country))
                .collect(Collectors.toList());

        return new GeocodingResponse(filtered, response.generationtimeMs());
    }

    /**
     * Handles error responses from the API.
     *
     * @param response ClientResponse with error status
     * @return Mono with OpenMeteoException
     */
    private Mono<? extends Throwable> handleErrorResponse(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.error("API error: status={}, body={}", response.statusCode(), body);
                    return Mono.error(new OpenMeteoException(
                            "API request failed: " + response.statusCode(),
                            response.statusCode().value()
                    ));
                });
    }
}
