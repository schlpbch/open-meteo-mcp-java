package com.openmeteo.mcp.model.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for SnowRequest record.
 * <p>
 * Tests validation logic, factory methods, and record behavior.
 * </p>
 */
class SnowRequestTest {

    @Test
    void shouldCreateSnowRequestWithValidParameters() {
        // Arrange & Act
        var request = new SnowRequest(46.8182, 8.2275, 7, true, "Europe/Zurich");

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.latitude()).isEqualTo(46.8182);
        assertThat(request.longitude()).isEqualTo(8.2275);
        assertThat(request.forecastDays()).isEqualTo(7);
        assertThat(request.includeHourly()).isTrue();
        assertThat(request.timezone()).isEqualTo("Europe/Zurich");
    }

    @Test
    void shouldCreateSnowRequestWithDefaultTimezone() {
        // Arrange & Act
        var request = new SnowRequest(46.8182, 8.2275, 7, true, null);

        // Assert
        assertThat(request.timezone()).isEqualTo("auto");
    }

    @Test
    void shouldCreateSnowRequestWithBlankTimezone() {
        // Arrange & Act
        var request = new SnowRequest(46.8182, 8.2275, 7, true, "  ");

        // Assert
        assertThat(request.timezone()).isEqualTo("auto");
    }

    @Test
    void shouldCreateSnowRequestWithDefaults() {
        // Arrange & Act
        var request = SnowRequest.withDefaults(46.8182, 8.2275);

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.latitude()).isEqualTo(46.8182);
        assertThat(request.longitude()).isEqualTo(8.2275);
        assertThat(request.forecastDays()).isEqualTo(7);
        assertThat(request.includeHourly()).isTrue();
        assertThat(request.timezone()).isEqualTo("auto");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-91.0, -90.1, 90.1, 91.0, 100.0, -100.0})
    void shouldThrowExceptionForInvalidLatitude(double invalidLatitude) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new SnowRequest(invalidLatitude, 8.2275, 7, true, "auto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Latitude must be between -90 and 90");
    }

    @ParameterizedTest
    @ValueSource(doubles = {-181.0, -180.1, 180.1, 181.0, 200.0, -200.0})
    void shouldThrowExceptionForInvalidLongitude(double invalidLongitude) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new SnowRequest(46.8182, invalidLongitude, 7, true, "auto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Longitude must be between -180 and 180");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 17, 20, 100})
    void shouldThrowExceptionForInvalidForecastDays(int invalidDays) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new SnowRequest(46.8182, 8.2275, invalidDays, true, "auto"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Forecast days must be between 1 and 16");
    }

    @Test
    void shouldAcceptMinimumValidLatitude() {
        // Arrange & Act
        var request = new SnowRequest(-90.0, 8.2275, 7, true, "auto");

        // Assert
        assertThat(request.latitude()).isEqualTo(-90.0);
    }

    @Test
    void shouldAcceptMaximumValidLatitude() {
        // Arrange & Act
        var request = new SnowRequest(90.0, 8.2275, 7, true, "auto");

        // Assert
        assertThat(request.latitude()).isEqualTo(90.0);
    }

    @Test
    void shouldAcceptMinimumValidLongitude() {
        // Arrange & Act
        var request = new SnowRequest(46.8182, -180.0, 7, true, "auto");

        // Assert
        assertThat(request.longitude()).isEqualTo(-180.0);
    }

    @Test
    void shouldAcceptMaximumValidLongitude() {
        // Arrange & Act
        var request = new SnowRequest(46.8182, 180.0, 7, true, "auto");

        // Assert
        assertThat(request.longitude()).isEqualTo(180.0);
    }

    @Test
    void shouldAcceptMinimumValidForecastDays() {
        // Arrange & Act
        var request = new SnowRequest(46.8182, 8.2275, 1, true, "auto");

        // Assert
        assertThat(request.forecastDays()).isEqualTo(1);
    }

    @Test
    void shouldAcceptMaximumValidForecastDays() {
        // Arrange & Act
        var request = new SnowRequest(46.8182, 8.2275, 16, true, "auto");

        // Assert
        assertThat(request.forecastDays()).isEqualTo(16);
    }

    @Test
    void shouldSupportRecordEquality() {
        // Arrange
        var request1 = new SnowRequest(46.8182, 8.2275, 7, true, "auto");
        var request2 = new SnowRequest(46.8182, 8.2275, 7, true, "auto");

        // Act & Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void shouldSupportRecordInequality() {
        // Arrange
        var request1 = new SnowRequest(46.8182, 8.2275, 7, true, "auto");
        var request2 = new SnowRequest(46.8182, 8.2275, 14, true, "auto");

        // Act & Assert
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void shouldSupportRecordToString() {
        // Arrange
        var request = new SnowRequest(46.8182, 8.2275, 7, true, "Europe/Zurich");

        // Act
        String result = request.toString();

        // Assert
        assertThat(result).contains("SnowRequest");
        assertThat(result).contains("latitude=46.8182");
        assertThat(result).contains("longitude=8.2275");
        assertThat(result).contains("forecastDays=7");
        assertThat(result).contains("includeHourly=true");
        assertThat(result).contains("timezone=Europe/Zurich");
    }

    @Test
    void shouldBeSerializable() {
        // Arrange
        var request = new SnowRequest(46.8182, 8.2275, 7, true, "auto");

        // Assert
        assertThat(request).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void shouldHandleVariousTimezones() {
        // Arrange & Act
        var request1 = new SnowRequest(46.8182, 8.2275, 7, true, "Europe/Zurich");
        var request2 = new SnowRequest(46.8182, 8.2275, 7, true, "America/Denver");
        var request3 = new SnowRequest(46.8182, 8.2275, 7, true, "UTC");

        // Assert
        assertThat(request1.timezone()).isEqualTo("Europe/Zurich");
        assertThat(request2.timezone()).isEqualTo("America/Denver");
        assertThat(request3.timezone()).isEqualTo("UTC");
    }

    @Test
    void shouldHandleIncludeHourlyFalse() {
        // Arrange & Act
        var request = new SnowRequest(46.8182, 8.2275, 7, false, "auto");

        // Assert
        assertThat(request.includeHourly()).isFalse();
    }

    @Test
    void shouldHandleAlpineCoordinates() {
        // Arrange & Act - Jungfraujoch coordinates
        var request = new SnowRequest(46.5472, 7.9858, 7, true, "Europe/Zurich");

        // Assert
        assertThat(request.latitude()).isEqualTo(46.5472);
        assertThat(request.longitude()).isEqualTo(7.9858);
    }

    @Test
    void shouldHandleRockyMountainCoordinates() {
        // Arrange & Act - Aspen, Colorado coordinates
        var request = new SnowRequest(39.1911, -106.8175, 7, true, "America/Denver");

        // Assert
        assertThat(request.latitude()).isEqualTo(39.1911);
        assertThat(request.longitude()).isEqualTo(-106.8175);
    }

    @Test
    void shouldHandleVariousForecastDays() {
        // Arrange & Act
        var request1 = new SnowRequest(46.8182, 8.2275, 1, true, "auto");
        var request2 = new SnowRequest(46.8182, 8.2275, 7, true, "auto");
        var request3 = new SnowRequest(46.8182, 8.2275, 14, true, "auto");

        // Assert
        assertThat(request1.forecastDays()).isEqualTo(1);
        assertThat(request2.forecastDays()).isEqualTo(7);
        assertThat(request3.forecastDays()).isEqualTo(14);
    }
}
