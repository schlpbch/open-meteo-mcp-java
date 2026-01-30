package com.openmeteo.mcp.service.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ValidationUtil.
 */
class ValidationUtilTest {

    @Test
    void shouldValidateValidLatitude() {
        // Should not throw
        ValidationUtil.validateLatitude(0.0);
        ValidationUtil.validateLatitude(47.3769);
        ValidationUtil.validateLatitude(-47.3769);
        ValidationUtil.validateLatitude(90.0);
        ValidationUtil.validateLatitude(-90.0);
    }

    @Test
    void shouldRejectLatitudeAbove90() {
        assertThatThrownBy(() -> ValidationUtil.validateLatitude(91.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude must be between -90 and 90");
    }

    @Test
    void shouldRejectLatitudeBelow90() {
        assertThatThrownBy(() -> ValidationUtil.validateLatitude(-91.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Latitude must be between -90 and 90");
    }

    @Test
    void shouldValidateValidLongitude() {
        // Should not throw
        ValidationUtil.validateLongitude(0.0);
        ValidationUtil.validateLongitude(8.5417);
        ValidationUtil.validateLongitude(-8.5417);
        ValidationUtil.validateLongitude(180.0);
        ValidationUtil.validateLongitude(-180.0);
    }

    @Test
    void shouldRejectLongitudeAbove180() {
        assertThatThrownBy(() -> ValidationUtil.validateLongitude(181.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude must be between -180 and 180");
    }

    @Test
    void shouldRejectLongitudeBelow180() {
        assertThatThrownBy(() -> ValidationUtil.validateLongitude(-181.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Longitude must be between -180 and 180");
    }

    @Test
    void shouldClampForecastDays() {
        assertThat(ValidationUtil.clampForecastDays(5, 1, 16)).isEqualTo(5);
        assertThat(ValidationUtil.clampForecastDays(0, 1, 16)).isEqualTo(1);
        assertThat(ValidationUtil.clampForecastDays(20, 1, 16)).isEqualTo(16);
        assertThat(ValidationUtil.clampForecastDays(-5, 1, 16)).isEqualTo(1);
        assertThat(ValidationUtil.clampForecastDays(100, 1, 16)).isEqualTo(16);
    }

    @Test
    void shouldValidateNotBlank() {
        // Should not throw
        ValidationUtil.validateNotBlank("test", "Field");
        ValidationUtil.validateNotBlank("a", "Field");
        ValidationUtil.validateNotBlank("  test  ", "Field");
    }

    @Test
    void shouldRejectNullString() {
        assertThatThrownBy(() -> ValidationUtil.validateNotBlank(null, "Field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Field cannot be null or blank");
    }

    @Test
    void shouldRejectBlankString() {
        assertThatThrownBy(() -> ValidationUtil.validateNotBlank("", "Field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Field cannot be null or blank");

        assertThatThrownBy(() -> ValidationUtil.validateNotBlank("   ", "Field"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Field cannot be null or blank");
    }
}
