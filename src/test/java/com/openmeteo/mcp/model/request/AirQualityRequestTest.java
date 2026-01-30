package com.openmeteo.mcp.model.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for AirQualityRequest record.
 * <p>
 * Tests validation logic, factory methods, and record behavior.
 * </p>
 */
class AirQualityRequestTest {

    @Test
    void shouldCreateAirQualityRequestWithValidParameters() {
        // Arrange & Act
        var request = new AirQualityRequest(47.3769, 8.5417, 3, true, "Europe/Zurich");

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.latitude()).isEqualTo(47.3769);
        assertThat(request.longitude()).isEqualTo(8.5417);
        assertThat(request.forecastDays()).isEqualTo(3);
        assertThat(request.includePollen()).isTrue();
        assertThat(request.timezone()).isEqualTo("Europe/Zurich");
    }

    @Test
    void shouldCreateAirQualityRequestWithDefaultTimezone() {
        // Arrange & Act
        var request = new AirQualityRequest(47.3769, 8.5417, 3, true, null);

        // Assert
        assertThat(request.timezone()).isEqualTo("auto");
    }

    @Test
    void shouldCreateAirQualityRequestWithBlankTimezone() {
        // Arrange & Act
        var request = new AirQualityRequest(47.3769, 8.5417, 3, true, "  ");

        // Assert
        assertThat(request.timezone()).isEqualTo("auto");
    }

    @Test
    void shouldCreateAirQualityRequestWithDefaults() {
        // Arrange & Act
        var request = AirQualityRequest.withDefaults(47.3769, 8.5417);

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.latitude()).isEqualTo(47.3769);
        assertThat(request.longitude()).isEqualTo(8.5417);
        assertThat(request.forecastDays()).isEqualTo(3);
        assertThat(request.includePollen()).isTrue();
        assertThat(request.timezone()).isEqualTo("auto");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-91.0, -90.1, 90.1, 91.0, 100.0, -100.0})
    void shouldThrowExceptionForInvalidLatitude(double invalidLatitude) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new AirQualityRequest(invalidLatitude, 8.5417, 3, true, "auto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Latitude must be between -90 and 90");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-181.0, -180.1, 180.1, 181.0, 200.0, -200.0})
    void shouldThrowExceptionForInvalidLongitude(double invalidLongitude) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new AirQualityRequest(47.3769, invalidLongitude, 3, true, "auto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Longitude must be between -180 and 180");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 6, 10, 100})
    void shouldThrowExceptionForInvalidForecastDays(int invalidDays) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new AirQualityRequest(47.3769, 8.5417, invalidDays, true, "auto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Air quality forecast days must be between 1 and 5");
    }

    @Test
    void shouldAcceptMinimumValidLatitude() {
        // Arrange & Act
        var request = new AirQualityRequest(-90.0, 8.5417, 3, true, "auto");

        // Assert
        assertThat(request.latitude()).isEqualTo(-90.0);
    }

    @Test
    void shouldAcceptMaximumValidLatitude() {
        // Arrange & Act
        var request = new AirQualityRequest(90.0, 8.5417, 3, true, "auto");

        // Assert
        assertThat(request.latitude()).isEqualTo(90.0);
    }

    @Test
    void shouldAcceptMinimumValidLongitude() {
        // Arrange & Act
        var request = new AirQualityRequest(47.3769, -180.0, 3, true, "auto");

        // Assert
        assertThat(request.longitude()).isEqualTo(-180.0);
    }

    @Test
    void shouldAcceptMaximumValidLongitude() {
        // Arrange & Act
        var request = new AirQualityRequest(47.3769, 180.0, 3, true, "auto");

        // Assert
        assertThat(request.longitude()).isEqualTo(180.0);
    }

    @Test
    void shouldAcceptMinimumValidForecastDays() {
        // Arrange & Act
        var request = new AirQualityRequest(47.3769, 8.5417, 1, true, "auto");

        // Assert
        assertThat(request.forecastDays()).isEqualTo(1);
    }

    @Test
    void shouldAcceptMaximumValidForecastDays() {
        // Arrange & Act
        var request = new AirQualityRequest(47.3769, 8.5417, 5, true, "auto");

        // Assert
        assertThat(request.forecastDays()).isEqualTo(5);
    }

    @Test
    void shouldSupportRecordEquality() {
        // Arrange
        var request1 = new AirQualityRequest(47.3769, 8.5417, 3, true, "auto");
        var request2 = new AirQualityRequest(47.3769, 8.5417, 3, true, "auto");

        // Act & Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void shouldSupportRecordInequality() {
        // Arrange
        var request1 = new AirQualityRequest(47.3769, 8.5417, 3, true, "auto");
        var request2 = new AirQualityRequest(47.3769, 8.5417, 5, true, "auto");

        // Act & Assert
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void shouldSupportRecordToString() {
        // Arrange
        var request = new AirQualityRequest(47.3769, 8.5417, 3, true, "Europe/Zurich");

        // Act
        String result = request.toString();

        // Assert
        assertThat(result).contains("AirQualityRequest");
        assertThat(result).contains("latitude=47.3769");
        assertThat(result).contains("longitude=8.5417");
        assertThat(result).contains("forecastDays=3");
        assertThat(result).contains("includePollen=true");
        assertThat(result).contains("timezone=Europe/Zurich");
    }

    @Test
    void shouldBeSerializable() {
        // Arrange
        var request = new AirQualityRequest(47.3769, 8.5417, 3, true, "auto");

        // Assert
        assertThat(request).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void shouldHandleVariousTimezones() {
        // Arrange & Act
        var request1 = new AirQualityRequest(47.3769, 8.5417, 3, true, "Europe/Zurich");
        var request2 = new AirQualityRequest(47.3769, 8.5417, 3, true, "America/New_York");
        var request3 = new AirQualityRequest(47.3769, 8.5417, 3, true, "Asia/Tokyo");

        // Assert
        assertThat(request1.timezone()).isEqualTo("Europe/Zurich");
        assertThat(request2.timezone()).isEqualTo("America/New_York");
        assertThat(request3.timezone()).isEqualTo("Asia/Tokyo");
    }

    @Test
    void shouldHandleIncludePollenFalse() {
        // Arrange & Act
        var request = new AirQualityRequest(47.3769, 8.5417, 3, false, "auto");

        // Assert
        assertThat(request.includePollen()).isFalse();
    }

    @Test
    void shouldHandleEuropeanCityCoordinates() {
        // Arrange & Act - Berlin coordinates
        var request = new AirQualityRequest(52.5200, 13.4050, 3, true, "Europe/Berlin");

        // Assert
        assertThat(request.latitude()).isEqualTo(52.5200);
        assertThat(request.longitude()).isEqualTo(13.4050);
        assertThat(request.includePollen()).isTrue(); // Pollen data available in Europe
    }

    @Test
    void shouldHandleNonEuropeanCityCoordinates() {
        // Arrange & Act - New York coordinates
        var request = new AirQualityRequest(40.7128, -74.0060, 3, false, "America/New_York");

        // Assert
        assertThat(request.latitude()).isEqualTo(40.7128);
        assertThat(request.longitude()).isEqualTo(-74.0060);
        assertThat(request.includePollen()).isFalse(); // Pollen data not available outside Europe
    }

    @Test
    void shouldHandleVariousForecastDays() {
        // Arrange & Act
        var request1 = new AirQualityRequest(47.3769, 8.5417, 1, true, "auto");
        var request2 = new AirQualityRequest(47.3769, 8.5417, 3, true, "auto");
        var request3 = new AirQualityRequest(47.3769, 8.5417, 5, true, "auto");

        // Assert
        assertThat(request1.forecastDays()).isEqualTo(1);
        assertThat(request2.forecastDays()).isEqualTo(3);
        assertThat(request3.forecastDays()).isEqualTo(5);
    }

    @Test
    void shouldHandleAsianCityCoordinates() {
        // Arrange & Act - Tokyo coordinates
        var request = new AirQualityRequest(35.6762, 139.6503, 3, false, "Asia/Tokyo");

        // Assert
        assertThat(request.latitude()).isEqualTo(35.6762);
        assertThat(request.longitude()).isEqualTo(139.6503);
    }

    @Test
    void shouldHandleSouthernHemisphereCoordinates() {
        // Arrange & Act - Sydney coordinates
        var request = new AirQualityRequest(-33.8688, 151.2093, 3, false, "Australia/Sydney");

        // Assert
        assertThat(request.latitude()).isEqualTo(-33.8688);
        assertThat(request.longitude()).isEqualTo(151.2093);
    }
}
