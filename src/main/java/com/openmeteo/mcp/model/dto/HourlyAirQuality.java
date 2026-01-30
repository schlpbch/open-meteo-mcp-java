package com.openmeteo.mcp.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Hourly air quality forecast data.
 * <p>
 * Contains lists of hourly values for air quality parameters including
 * AQI, pollutants, and pollen data (Europe only).
 * All lists have the same length, with each index representing the same hour.
 * </p>
 *
 * @param time            ISO 8601 timestamps
 * @param europeanAqi     European Air Quality Index (0-500)
 * @param usAqi           US Air Quality Index (0-500)
 * @param pm10            Particulate matter < 10 μm (μg/m³)
 * @param pm25            Particulate matter < 2.5 μm (μg/m³)
 * @param carbonMonoxide  Carbon monoxide (μg/m³)
 * @param nitrogenDioxide Nitrogen dioxide (μg/m³)
 * @param sulphurDioxide  Sulphur dioxide (μg/m³)
 * @param ozone           Ozone (μg/m³)
 * @param uvIndex         UV index
 * @param alderPollen     Alder pollen concentration (grains/m³) - Europe only
 * @param birchPollen     Birch pollen concentration (grains/m³) - Europe only
 * @param grassPollen     Grass pollen concentration (grains/m³) - Europe only
 * @param mugwortPollen   Mugwort pollen concentration (grains/m³) - Europe only
 * @param olivePollen     Olive pollen concentration (grains/m³) - Europe only
 * @param ragweedPollen   Ragweed pollen concentration (grains/m³) - Europe only
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record HourlyAirQuality(
        @JsonProperty("time")
        List<String> time,

        @JsonProperty("european_aqi")
        List<Integer> europeanAqi,

        @JsonProperty("us_aqi")
        List<Integer> usAqi,

        @JsonProperty("pm10")
        List<Double> pm10,

        @JsonProperty("pm2_5")
        List<Double> pm25,

        @JsonProperty("carbon_monoxide")
        List<Double> carbonMonoxide,

        @JsonProperty("nitrogen_dioxide")
        List<Double> nitrogenDioxide,

        @JsonProperty("sulphur_dioxide")
        List<Double> sulphurDioxide,

        @JsonProperty("ozone")
        List<Double> ozone,

        @JsonProperty("uv_index")
        List<Double> uvIndex,

        @JsonProperty("alder_pollen")
        List<Double> alderPollen,

        @JsonProperty("birch_pollen")
        List<Double> birchPollen,

        @JsonProperty("grass_pollen")
        List<Double> grassPollen,

        @JsonProperty("mugwort_pollen")
        List<Double> mugwortPollen,

        @JsonProperty("olive_pollen")
        List<Double> olivePollen,

        @JsonProperty("ragweed_pollen")
        List<Double> ragweedPollen
) implements Serializable {
}
