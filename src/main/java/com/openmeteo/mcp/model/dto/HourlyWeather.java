package com.openmeteo.mcp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Hourly weather forecast data.
 * <p>
 * Contains lists of hourly values for various weather parameters.
 * All lists have the same length, with each index representing the same hour.
 * </p>
 *
 * @param time                      List of ISO 8601 timestamps
 * @param temperature2m             Temperature at 2 meters in °C
 * @param apparentTemperature       Apparent/feels-like temperature in °C
 * @param precipitation             Precipitation in mm
 * @param rain                      Rain in mm
 * @param snowfall                  Snowfall in cm
 * @param snowDepth                 Snow depth in meters
 * @param weathercode               WMO weather code
 * @param cloudCover                Cloud cover percentage (0-100)
 * @param visibility                Visibility in meters
 * @param windspeed10m              Wind speed at 10 meters in km/h
 * @param winddirection10m          Wind direction at 10 meters in degrees
 * @param windgusts10m              Wind gusts at 10 meters in km/h
 * @param surfacePressure           Surface pressure in hPa
 * @param relativehumidity2m        Relative humidity at 2 meters in %
 * @param dewpoint2m                Dew point at 2 meters in °C
 * @param precipitationProbability  Probability of precipitation in %
 * @param uvIndex                   UV index
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HourlyWeather(
        @JsonProperty("time")
        List<String> time,

        @JsonProperty("temperature_2m")
        List<Double> temperature2m,

        @JsonProperty("apparent_temperature")
        List<Double> apparentTemperature,

        @JsonProperty("precipitation")
        List<Double> precipitation,

        @JsonProperty("rain")
        List<Double> rain,

        @JsonProperty("snowfall")
        List<Double> snowfall,

        @JsonProperty("snow_depth")
        List<Double> snowDepth,

        @JsonProperty("weathercode")
        List<Integer> weathercode,

        @JsonProperty("cloudcover")
        List<Integer> cloudCover,

        @JsonProperty("visibility")
        List<Double> visibility,

        @JsonProperty("windspeed_10m")
        List<Double> windspeed10m,

        @JsonProperty("winddirection_10m")
        List<Integer> winddirection10m,

        @JsonProperty("windgusts_10m")
        List<Double> windgusts10m,

        @JsonProperty("surface_pressure")
        List<Double> surfacePressure,

        @JsonProperty("relativehumidity_2m")
        List<Integer> relativehumidity2m,

        @JsonProperty("dewpoint_2m")
        List<Double> dewpoint2m,

        @JsonProperty("precipitation_probability")
        List<Integer> precipitationProbability,

        @JsonProperty("uv_index")
        List<Double> uvIndex
) implements Serializable {
}
