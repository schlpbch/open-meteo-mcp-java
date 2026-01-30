package com.openmeteo.mcp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Current air quality data.
 * <p>
 * Contains real-time air quality measurements including AQI and pollutants.
 * </p>
 *
 * @param time           ISO 8601 timestamp
 * @param europeanAqi    European Air Quality Index (0-500)
 * @param usAqi          US Air Quality Index (0-500)
 * @param pm10           Particulate matter < 10 μm (μg/m³)
 * @param pm25           Particulate matter < 2.5 μm (μg/m³)
 * @param carbonMonoxide Carbon monoxide (μg/m³)
 * @param nitrogenDioxide Nitrogen dioxide (μg/m³)
 * @param sulphurDioxide Sulphur dioxide (μg/m³)
 * @param ozone          Ozone (μg/m³)
 * @param uvIndex        UV index
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CurrentAirQuality(
        @JsonProperty("time")
        String time,

        @JsonProperty("european_aqi")
        Integer europeanAqi,

        @JsonProperty("us_aqi")
        Integer usAqi,

        @JsonProperty("pm10")
        Double pm10,

        @JsonProperty("pm2_5")
        Double pm25,

        @JsonProperty("carbon_monoxide")
        Double carbonMonoxide,

        @JsonProperty("nitrogen_dioxide")
        Double nitrogenDioxide,

        @JsonProperty("sulphur_dioxide")
        Double sulphurDioxide,

        @JsonProperty("ozone")
        Double ozone,

        @JsonProperty("uv_index")
        Double uvIndex
) implements Serializable {
}
