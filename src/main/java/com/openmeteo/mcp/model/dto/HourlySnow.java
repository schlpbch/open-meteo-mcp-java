package com.openmeteo.mcp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Hourly snow conditions data.
 * <p>
 * Contains lists of hourly values for snow-related parameters.
 * All lists have the same length, with each index representing the same hour.
 * </p>
 *
 * @param time                ISO 8601 timestamps
 * @param temperature2m       Temperature at 2 meters in °C
 * @param snowfall            Snowfall in cm
 * @param snowDepth           Snow depth in meters
 * @param weathercode         WMO weather code
 * @param cloudCover          Cloud cover percentage (0-100)
 * @param windspeed10m        Wind speed at 10 meters in km/h
 * @param winddirection10m    Wind direction at 10 meters in degrees
 * @param windgusts10m        Wind gusts at 10 meters in km/h
 * @param freezingLevelHeight Height of freezing level in meters
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HourlySnow(
        @JsonProperty("time")
        List<String> time,

        @JsonProperty("temperature_2m")
        List<Double> temperature2m,

        @JsonProperty("snowfall")
        List<Double> snowfall,

        @JsonProperty("snow_depth")
        List<Double> snowDepth,

        @JsonProperty("weathercode")
        List<Integer> weathercode,

        @JsonProperty("cloudcover")
        List<Integer> cloudCover,

        @JsonProperty("windspeed_10m")
        List<Double> windspeed10m,

        @JsonProperty("winddirection_10m")
        List<Integer> winddirection10m,

        @JsonProperty("windgusts_10m")
        List<Double> windgusts10m,

        @JsonProperty("freezinglevel_height")
        List<Double> freezingLevelHeight
) implements Serializable {
}
