package com.openmeteo.mcp.model;

import com.openmeteo.mcp.model.dto.CurrentWeather;
import com.openmeteo.mcp.model.dto.WeatherForecast;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for WeatherForecast and related model validation.
 */
class WeatherForecastTest {

    @Test
    void shouldCreateWeatherForecastWithValidCoordinates() {
        // Arrange & Act
        WeatherForecast forecast = new WeatherForecast(
                47.3769,
                8.5417,
                408.0,
                "Europe/Zurich",
                "CET",
                3600,
                null,
                null,
                null,
                1.5
        );

        // Assert
        assertThat(forecast).isNotNull();
        assertThat(forecast.latitude()).isEqualTo(47.3769);
        assertThat(forecast.longitude()).isEqualTo(8.5417);
        assertThat(forecast.elevation()).isEqualTo(408.0);
        assertThat(forecast.timezone()).isEqualTo("Europe/Zurich");
    }

    @Test
    void shouldRejectLatitudeAbove90() {
        // Act & Assert
        assertThatThrownBy(() -> new WeatherForecast(
                91.0,  // Invalid latitude
                8.5417,
                null,
                "Europe/Zurich",
                null,
                null,
                null,
                null,
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude must be between -90 and 90");
    }

    @Test
    void shouldRejectLatitudeBelow90() {
        // Act & Assert
        assertThatThrownBy(() -> new WeatherForecast(
                -91.0,  // Invalid latitude
                8.5417,
                null,
                "Europe/Zurich",
                null,
                null,
                null,
                null,
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude must be between -90 and 90");
    }

    @Test
    void shouldRejectLongitudeAbove180() {
        // Act & Assert
        assertThatThrownBy(() -> new WeatherForecast(
                47.3769,
                181.0,  // Invalid longitude
                null,
                "Europe/Zurich",
                null,
                null,
                null,
                null,
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude must be between -180 and 180");
    }

    @Test
    void shouldRejectLongitudeBelow180() {
        // Act & Assert
        assertThatThrownBy(() -> new WeatherForecast(
                47.3769,
                -181.0,  // Invalid longitude
                null,
                "Europe/Zurich",
                null,
                null,
                null,
                null,
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude must be between -180 and 180");
    }

    @Test
    void shouldCreateCurrentWeatherWithValidWindDirection() {
        // Arrange & Act
        CurrentWeather currentWeather = new CurrentWeather(
                15.5,
                10.0,
                180,  // Valid wind direction
                1,
                "2024-01-30T12:00:00Z"
        );

        // Assert
        assertThat(currentWeather).isNotNull();
        assertThat(currentWeather.temperature()).isEqualTo(15.5);
        assertThat(currentWeather.windspeed()).isEqualTo(10.0);
        assertThat(currentWeather.winddirection()).isEqualTo(180);
    }

    @Test
    void shouldRejectWindDirectionAbove360() {
        // Act & Assert
        assertThatThrownBy(() -> new CurrentWeather(
                15.5,
                10.0,
                361,  // Invalid wind direction
                1,
                "2024-01-30T12:00:00Z"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wind direction must be between 0 and 360");
    }

    @Test
    void shouldRejectWindDirectionBelowZero() {
        // Act & Assert
        assertThatThrownBy(() -> new CurrentWeather(
                15.5,
                10.0,
                -1,  // Invalid wind direction
                1,
                "2024-01-30T12:00:00Z"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Wind direction must be between 0 and 360");
    }

    @Test
    void shouldAllowNullOptionalFields() {
        // Arrange & Act
        WeatherForecast forecast = new WeatherForecast(
                47.3769,
                8.5417,
                null,  // elevation is optional
                "Europe/Zurich",
                null,  // timezoneAbbreviation is optional
                null,  // utcOffsetSeconds is optional
                null,  // currentWeather is optional
                null,  // hourly is optional
                null,  // daily is optional
                null   // generationtimeMs is optional
        );

        // Assert
        assertThat(forecast).isNotNull();
        assertThat(forecast.elevation()).isNull();
        assertThat(forecast.currentWeather()).isNull();
    }
}
