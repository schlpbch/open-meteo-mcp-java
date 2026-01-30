package com.openmeteo.mcp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Current weather conditions.
 * <p>
 * Contains real-time weather data including temperature, wind, and weather code.
 * </p>
 *
 * @param temperature    Current temperature in °C
 * @param windspeed      Wind speed in km/h
 * @param winddirection  Wind direction in degrees (0-360)
 * @param weathercode    WMO weather code
 * @param time           ISO 8601 timestamp
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CurrentWeather(
        @JsonProperty("temperature")
        Double temperature,

        @JsonProperty("windspeed")
        Double windspeed,

        @JsonProperty("winddirection")
        Integer winddirection,

        @JsonProperty("weathercode")
        Integer weathercode,

        @JsonProperty("time")
        String time
) implements Serializable {

    /**
     * Compact constructor with validation.
     */
    public CurrentWeather {
        if (winddirection != null && (winddirection < 0 || winddirection > 360)) {
            throw new IllegalArgumentException("Wind direction must be between 0 and 360 degrees");
        }
    }
}
