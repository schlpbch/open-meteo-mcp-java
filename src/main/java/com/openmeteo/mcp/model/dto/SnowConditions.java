package com.openmeteo.mcp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Snow conditions response from Open-Meteo API.
 * <p>
 * Contains snow forecast data including current conditions,
 * hourly snow data, and daily summaries for mountain weather.
 * </p>
 *
 * @param latitude             Latitude of the location in decimal degrees
 * @param longitude            Longitude of the location in decimal degrees
 * @param elevation            Elevation of the location in meters
 * @param timezone             Timezone identifier (e.g., "Europe/Zurich")
 * @param timezoneAbbreviation Timezone abbreviation (e.g., "CET")
 * @param utcOffsetSeconds     UTC offset in seconds
 * @param currentWeather       Current weather conditions
 * @param hourly               Hourly snow forecast
 * @param daily                Daily snow forecast
 * @param generationtimeMs     API generation time in milliseconds
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SnowConditions(
        @JsonProperty("latitude")
        double latitude,

        @JsonProperty("longitude")
        double longitude,

        @JsonProperty("elevation")
        Double elevation,

        @JsonProperty("timezone")
        String timezone,

        @JsonProperty("timezone_abbreviation")
        String timezoneAbbreviation,

        @JsonProperty("utc_offset_seconds")
        Integer utcOffsetSeconds,

        @JsonProperty("current_weather")
        CurrentWeather currentWeather,

        @JsonProperty("hourly")
        HourlySnow hourly,

        @JsonProperty("daily")
        DailySnow daily,

        @JsonProperty("generationtime_ms")
        Double generationtimeMs
) implements Serializable {

    /**
     * Compact constructor with validation.
     */
    public SnowConditions {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }
}
