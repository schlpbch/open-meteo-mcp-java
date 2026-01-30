package com.openmeteo.mcp.service.util;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SkiConditionAssessor.
 */
class SkiConditionAssessorTest {

    @Test
    void shouldAssessExcellentConditions() {
        String assessment = SkiConditionAssessor.assessSkiConditions(
                0.8,    // 80cm snow depth
                15.0,   // 15cm fresh snowfall
                -8.0,   // -8°C temp
                1       // Mainly clear
        );

        assertThat(assessment).isEqualTo("Excellent");
    }

    @Test
    void shouldAssessExcellentConditionsWithClearSky() {
        String assessment = SkiConditionAssessor.assessSkiConditions(
                1.0,    // 100cm snow depth
                20.0,   // 20cm fresh snowfall
                -10.0,  // -10°C temp
                0       // Clear sky
        );

        assertThat(assessment).isEqualTo("Excellent");
    }

    @Test
    void shouldAssessGoodConditions() {
        String assessment = SkiConditionAssessor.assessSkiConditions(
                0.6,    // 60cm snow depth
                5.0,    // 5cm fresh snowfall (not enough for excellent)
                -5.0,   // -5°C temp
                2       // Partly cloudy
        );

        assertThat(assessment).isEqualTo("Good");
    }

    @Test
    void shouldAssessFairConditions() {
        String assessment = SkiConditionAssessor.assessSkiConditions(
                0.3,    // 30cm snow depth
                2.0,    // 2cm fresh snowfall
                -2.0,   // -2°C temp
                3       // Overcast
        );

        assertThat(assessment).isEqualTo("Fair");
    }

    @Test
    void shouldAssessPoorConditions() {
        String assessment = SkiConditionAssessor.assessSkiConditions(
                0.1,    // 10cm snow depth (too little)
                0.0,    // No fresh snow
                5.0,    // 5°C (too warm)
                61      // Rain
        );

        assertThat(assessment).isEqualTo("Poor");
    }

    @Test
    void shouldAssessPoorConditionsWithNoSnow() {
        String assessment = SkiConditionAssessor.assessSkiConditions(
                0.0,    // No snow depth
                0.0,    // No fresh snow
                10.0,   // 10°C (too warm)
                0       // Clear sky
        );

        assertThat(assessment).isEqualTo("Poor");
    }

    @Test
    void shouldCalculateWindChill() {
        double windChill = SkiConditionAssessor.calculateWindChill(-10.0, 30.0);

        // Wind chill makes it feel colder
        assertThat(windChill).isLessThan(-10.0);
        assertThat(windChill).isCloseTo(-18.0, Offset.offset(3.0));
    }

    @Test
    void shouldCalculateWindChillAtZeroTemp() {
        double windChill = SkiConditionAssessor.calculateWindChill(0.0, 20.0);

        // Wind chill makes it feel colder
        assertThat(windChill).isLessThan(0.0);
        assertThat(windChill).isCloseTo(-7.0, Offset.offset(2.0));
    }

    @Test
    void shouldNotCalculateWindChillAtLowSpeed() {
        double windChill = SkiConditionAssessor.calculateWindChill(-10.0, 4.0);

        // No wind chill at low speeds
        assertThat(windChill).isEqualTo(-10.0);
    }

    @Test
    void shouldNotCalculateWindChillAtNoWind() {
        double windChill = SkiConditionAssessor.calculateWindChill(15.0, 0.0);

        // No wind chill with no wind
        assertThat(windChill).isEqualTo(15.0);
    }

    @Test
    void shouldCalculateWindChillAtHighSpeed() {
        double windChill = SkiConditionAssessor.calculateWindChill(-15.0, 50.0);

        // Strong wind makes it feel much colder
        assertThat(windChill).isLessThan(-15.0);
        assertThat(windChill).isCloseTo(-25.0, Offset.offset(5.0));
    }
}
