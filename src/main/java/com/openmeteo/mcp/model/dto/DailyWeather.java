package com.openmeteo.mcp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Daily weather forecast data.
 * <p>
 * Contains lists of daily values for various weather parameters.
 * All lists have the same length, with each index representing the same day.
 * </p>
 *
 * @param time                      List of ISO 8601 date strings
 * @param temperature2mMax          Maximum temperature at 2 meters in °C
 * @param temperature2mMin          Minimum temperature at 2 meters in °C
 * @param apparentTemperatureMax    Maximum apparent temperature in °C
 * @param apparentTemperatureMin    Minimum apparent temperature in °C
 * @param precipitationSum          Total precipitation in mm
 * @param rainSum                   Total rain in mm
 * @param snowfallSum               Total snowfall in cm
 * @param weathercode               Dominant WMO weather code for the day
 * @param sunrise                   Sunrise time (ISO 8601)
 * @param sunset                    Sunset time (ISO 8601)
 * @param windspeed10mMax           Maximum wind speed at 10 meters in km/h
 * @param windgusts10mMax           Maximum wind gusts at 10 meters in km/h
 * @param winddirection10mDominant  Dominant wind direction at 10 meters in degrees
 * @param precipitationProbabilityMax Maximum probability of precipitation in %
 * @param uvIndexMax                Maximum UV index
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DailyWeather(
        @JsonProperty("time")
        List<String> time,

        @JsonProperty("temperature_2m_max")
        List<Double> temperature2mMax,

        @JsonProperty("temperature_2m_min")
        List<Double> temperature2mMin,

        @JsonProperty("apparent_temperature_max")
        List<Double> apparentTemperatureMax,

        @JsonProperty("apparent_temperature_min")
        List<Double> apparentTemperatureMin,

        @JsonProperty("precipitation_sum")
        List<Double> precipitationSum,

        @JsonProperty("rain_sum")
        List<Double> rainSum,

        @JsonProperty("snowfall_sum")
        List<Double> snowfallSum,

        @JsonProperty("weathercode")
        List<Integer> weathercode,

        @JsonProperty("sunrise")
        List<String> sunrise,

        @JsonProperty("sunset")
        List<String> sunset,

        @JsonProperty("windspeed_10m_max")
        List<Double> windspeed10mMax,

        @JsonProperty("windgusts_10m_max")
        List<Double> windgusts10mMax,

        @JsonProperty("winddirection_10m_dominant")
        List<Integer> winddirection10mDominant,

        @JsonProperty("precipitation_probability_max")
        List<Integer> precipitationProbabilityMax,

        @JsonProperty("uv_index_max")
        List<Double> uvIndexMax
) implements Serializable {
}
