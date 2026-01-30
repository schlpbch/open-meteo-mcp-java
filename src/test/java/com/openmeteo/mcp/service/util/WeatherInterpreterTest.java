package com.openmeteo.mcp.service.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for WeatherInterpreter.
 */
class WeatherInterpreterTest {

    @Test
    void shouldInterpretClearSky() {
        var info = WeatherInterpreter.interpretWeatherCode(0);

        assertThat(info.code()).isEqualTo(0);
        assertThat(info.description()).isEqualTo("Clear sky");
        assertThat(info.category()).isEqualTo("Clear");
        assertThat(info.severity()).isEqualTo("none");
    }

    @Test
    void shouldInterpretPartlyCloudy() {
        var info = WeatherInterpreter.interpretWeatherCode(2);

        assertThat(info.code()).isEqualTo(2);
        assertThat(info.description()).isEqualTo("Partly cloudy");
        assertThat(info.category()).isEqualTo("Cloudy");
        assertThat(info.severity()).isEqualTo("none");
    }

    @Test
    void shouldInterpretFog() {
        var info = WeatherInterpreter.interpretWeatherCode(45);

        assertThat(info.description()).isEqualTo("Fog");
        assertThat(info.category()).isEqualTo("Fog");
        assertThat(info.severity()).isEqualTo("low");
    }

    @Test
    void shouldInterpretDrizzle() {
        var info = WeatherInterpreter.interpretWeatherCode(53);

        assertThat(info.description()).isEqualTo("Moderate drizzle");
        assertThat(info.category()).isEqualTo("Drizzle");
        assertThat(info.severity()).isEqualTo("low");
    }

    @Test
    void shouldInterpretRain() {
        var info = WeatherInterpreter.interpretWeatherCode(63);

        assertThat(info.description()).isEqualTo("Moderate rain");
        assertThat(info.category()).isEqualTo("Rain");
        assertThat(info.severity()).isEqualTo("medium");
    }

    @Test
    void shouldInterpretSnow() {
        var info = WeatherInterpreter.interpretWeatherCode(73);

        assertThat(info.description()).isEqualTo("Moderate snowfall");
        assertThat(info.category()).isEqualTo("Snow");
        assertThat(info.severity()).isEqualTo("medium");
    }

    @Test
    void shouldInterpretThunderstorm() {
        var info = WeatherInterpreter.interpretWeatherCode(95);

        assertThat(info.description()).isEqualTo("Thunderstorm");
        assertThat(info.category()).isEqualTo("Thunderstorm");
        assertThat(info.severity()).isEqualTo("high");
    }

    @Test
    void shouldInterpretThunderstormWithHail() {
        var info = WeatherInterpreter.interpretWeatherCode(99);

        assertThat(info.description()).isEqualTo("Thunderstorm with heavy hail");
        assertThat(info.category()).isEqualTo("Thunderstorm");
        assertThat(info.severity()).isEqualTo("extreme");
    }

    @Test
    void shouldInterpretUnknownCode() {
        var info = WeatherInterpreter.interpretWeatherCode(999);

        assertThat(info.code()).isEqualTo(999);
        assertThat(info.description()).isEqualTo("Unknown");
        assertThat(info.category()).isEqualTo("Unknown");
        assertThat(info.severity()).isEqualTo("none");
    }

    @Test
    void shouldGetWeatherCategory() {
        assertThat(WeatherInterpreter.getWeatherCategory(0)).isEqualTo("Clear");
        assertThat(WeatherInterpreter.getWeatherCategory(2)).isEqualTo("Cloudy");
        assertThat(WeatherInterpreter.getWeatherCategory(61)).isEqualTo("Rain");
        assertThat(WeatherInterpreter.getWeatherCategory(71)).isEqualTo("Snow");
    }

    @Test
    void shouldGetTravelImpact() {
        assertThat(WeatherInterpreter.getTravelImpact(0)).isEqualTo("none");
        assertThat(WeatherInterpreter.getTravelImpact(51)).isEqualTo("minor");
        assertThat(WeatherInterpreter.getTravelImpact(63)).isEqualTo("moderate");
        assertThat(WeatherInterpreter.getTravelImpact(95)).isEqualTo("significant");
        assertThat(WeatherInterpreter.getTravelImpact(99)).isEqualTo("severe");
    }
}
