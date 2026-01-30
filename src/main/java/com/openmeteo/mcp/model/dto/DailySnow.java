package com.openmeteo.mcp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Daily snow conditions data.
 * <p>
 * Contains lists of daily values for snow-related parameters.
 * All lists have the same length, with each index representing the same day.
 * </p>
 *
 * @param time                 ISO 8601 date strings
 * @param temperature2mMax     Maximum temperature at 2 meters in °C
 * @param temperature2mMin     Minimum temperature at 2 meters in °C
 * @param snowfallSum          Total snowfall in cm
 * @param weathercode          Dominant WMO weather code for the day
 * @param windspeed10mMax      Maximum wind speed at 10 meters in km/h
 * @param windgusts10mMax      Maximum wind gusts at 10 meters in km/h
 * @param winddirection10mDominant Dominant wind direction at 10 meters in degrees
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DailySnow(
        @JsonProperty("time")
        List<String> time,

        @JsonProperty("temperature_2m_max")
        List<Double> temperature2mMax,

        @JsonProperty("temperature_2m_min")
        List<Double> temperature2mMin,

        @JsonProperty("snowfall_sum")
        List<Double> snowfallSum,

        @JsonProperty("weathercode")
        List<Integer> weathercode,

        @JsonProperty("windspeed_10m_max")
        List<Double> windspeed10mMax,

        @JsonProperty("windgusts_10m_max")
        List<Double> windgusts10mMax,

        @JsonProperty("winddirection_10m_dominant")
        List<Integer> winddirection10mDominant
) implements Serializable {
}
