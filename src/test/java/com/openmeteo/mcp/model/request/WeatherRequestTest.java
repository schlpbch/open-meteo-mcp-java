package com.openmeteo.mcp.model.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for WeatherRequest record.
 * <p>
 * Tests validation logic, factory methods, and record behavior.
 * </p>
 */
class WeatherRequestTest {

    @Test
    void shouldCreateWeatherRequestWithValidParameters() {
        // Arrange & Act
        var request = new WeatherRequest(47.3769, 8.5417, 7, true, "Europe/Zurich");

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.latitude()).isEqualTo(47.3769);
        assertThat(request.longitude()).isEqualTo(8.5417);
        assertThat(request.forecastDays()).isEqualTo(7);
        assertThat(request.includeHourly()).isTrue();
        assertThat(request.timezone()).isEqualTo("Europe/Zurich");
    }

    @Test
    void shouldCreateWeatherRequestWithDefaultTimezone() {
        // Arrange & Act
        var request = new WeatherRequest(47.3769, 8.5417, 7, true, null);

        // Assert
        assertThat(request.timezone()).isEqualTo("auto");
    }

    @Test
    void shouldCreateWeatherRequestWithBlankTimezone() {
        // Arrange & Act
        var request = new WeatherRequest(47.3769, 8.5417, 7, true, "  ");

        // Assert
        assertThat(request.timezone()).isEqualTo("auto");
    }

    @Test
    void shouldCreateWeatherRequestWithDefaults() {
        // Arrange & Act
        var request = WeatherRequest.withDefaults(47.3769, 8.5417);

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.latitude()).isEqualTo(47.3769);
        assertThat(request.longitude()).isEqualTo(8.5417);
        assertThat(request.forecastDays()).isEqualTo(7);
        assertThat(request.includeHourly()).isTrue();
        assertThat(request.timezone()).isEqualTo("auto");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-91.0, -90.1, 90.1, 91.0, 100.0, -100.0})
    void shouldThrowExceptionForInvalidLatitude(double invalidLatitude) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new WeatherRequest(invalidLatitude, 8.5417, 7, true, "auto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Latitude must be between -90 and 90");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-181.0, -180.1, 180.1, 181.0, 200.0, -200.0})
    void shouldThrowExceptionForInvalidLongitude(double invalidLongitude) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new WeatherRequest(47.3769, invalidLongitude, 7, true, "auto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Longitude must be between -180 and 180");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 17, 20, 100})
    void shouldThrowExceptionForInvalidForecastDays(int invalidDays) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new WeatherRequest(47.3769, 8.5417, invalidDays, true, "auto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Forecast days must be between 1 and 16");
    }

    @Test
    void shouldAcceptMinimumValidLatitude() {
        // Arrange & Act
        var request = new WeatherRequest(-90.0, 8.5417, 7, true, "auto");

        // Assert
        assertThat(request.latitude()).isEqualTo(-90.0);
    }

    @Test
    void shouldAcceptMaximumValidLatitude() {
        // Arrange & Act
        var request = new WeatherRequest(90.0, 8.5417, 7, true, "auto");

        // Assert
        assertThat(request.latitude()).isEqualTo(90.0);
    }

    @Test
    void shouldAcceptMinimumValidLongitude() {
        // Arrange & Act
        var request = new WeatherRequest(47.3769, -180.0, 7, true, "auto");

        // Assert
        assertThat(request.longitude()).isEqualTo(-180.0);
    }

    @Test
    void shouldAcceptMaximumValidLongitude() {
        // Arrange & Act
        var request = new WeatherRequest(47.3769, 180.0, 7, true, "auto");

        // Assert
        assertThat(request.longitude()).isEqualTo(180.0);
    }

    @Test
    void shouldAcceptMinimumValidForecastDays() {
        // Arrange & Act
        var request = new WeatherRequest(47.3769, 8.5417, 1, true, "auto");

        // Assert
        assertThat(request.forecastDays()).isEqualTo(1);
    }

    @Test
    void shouldAcceptMaximumValidForecastDays() {
        // Arrange & Act
        var request = new WeatherRequest(47.3769, 8.5417, 16, true, "auto");

        // Assert
        assertThat(request.forecastDays()).isEqualTo(16);
    }

    @Test
    void shouldSupportRecordEquality() {
        // Arrange
        var request1 = new WeatherRequest(47.3769, 8.5417, 7, true, "auto");
        var request2 = new WeatherRequest(47.3769, 8.5417, 7, true, "auto");

        // Act & Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void shouldSupportRecordToString() {
        // Arrange
        var request = new WeatherRequest(47.3769, 8.5417, 7, true, "Europe/Zurich");

        // Act
        String result = request.toString();

        // Assert
        assertThat(result).contains("WeatherRequest");
        assertThat(result).contains("latitude=47.3769");
        assertThat(result).contains("longitude=8.5417");
        assertThat(result).contains("forecastDays=7");
        assertThat(result).contains("includeHourly=true");
        assertThat(result).contains("timezone=Europe/Zurich");
    }

    @Test
    void shouldBeSerializable() {
        // Arrange
        var request = new WeatherRequest(47.3769, 8.5417, 7, true, "auto");

        // Assert
        assertThat(request).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void shouldHandleVariousTimezones() {
        // Arrange & Act
        var request1 = new WeatherRequest(47.3769, 8.5417, 7, true, "Europe/Zurich");
        var request2 = new WeatherRequest(47.3769, 8.5417, 7, true, "America/New_York");
        var request3 = new WeatherRequest(47.3769, 8.5417, 7, true, "UTC");

        // Assert
        assertThat(request1.timezone()).isEqualTo("Europe/Zurich");
        assertThat(request2.timezone()).isEqualTo("America/New_York");
        assertThat(request3.timezone()).isEqualTo("UTC");
    }

    @Test
    void shouldHandleIncludeHourlyFalse() {
        // Arrange & Act
        var request = new WeatherRequest(47.3769, 8.5417, 7, false, "auto");

        // Assert
        assertThat(request.includeHourly()).isFalse();
    }
}
