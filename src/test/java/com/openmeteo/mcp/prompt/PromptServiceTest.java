package com.openmeteo.mcp.prompt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PromptService.
 */
class PromptServiceTest {

    private PromptService promptService;

    @BeforeEach
    void setUp() {
        promptService = new PromptService();
    }

    @Test
    void shouldGenerateSkiTripPromptWithParameters() {
        String prompt = promptService.skiTripWeatherPrompt("Zermatt", "this weekend");

        assertThat(prompt).contains("Ski Trip Weather Planning");
        assertThat(prompt).contains("for Zermatt");
        assertThat(prompt).contains("on this weekend");
        assertThat(prompt).contains("Step 1");
        assertThat(prompt).contains("Step 2");
        assertThat(prompt).contains("get_snow_conditions");
        assertThat(prompt).contains("get_weather");
        assertThat(prompt).contains("weather://codes");
    }

    @Test
    void shouldGenerateSkiTripPromptWithoutParameters() {
        String prompt = promptService.skiTripWeatherPrompt(null, null);

        assertThat(prompt).contains("Ski Trip Weather Planning");
        assertThat(prompt).doesNotContain("null");
        assertThat(prompt).contains("Step 1");
        assertThat(prompt).contains("get_snow_conditions");
    }

    @Test
    void shouldGenerateSkiTripPromptWithEmptyStrings() {
        String prompt = promptService.skiTripWeatherPrompt("", "");

        assertThat(prompt).contains("Ski Trip Weather Planning");
        assertThat(prompt).startsWith("# Ski Trip Weather Planning");
        assertThat(prompt).contains("the requested dates");
        // Verify header doesn't have trailing parameter text when empty
        assertThat(prompt).doesNotStartWith("# Ski Trip Weather Planning for ");
        assertThat(prompt).doesNotStartWith("# Ski Trip Weather Planning on ");
    }

    @Test
    void shouldGenerateOutdoorActivityPromptWithParameters() {
        String prompt = promptService.planOutdoorActivityPrompt(
                "hiking", "Swiss Alps", "next week"
        );

        assertThat(prompt).contains("Outdoor Activity Planning");
        assertThat(prompt).contains("hiking");
        assertThat(prompt).contains("in Swiss Alps");
        assertThat(prompt).contains("next week");
        assertThat(prompt).contains("Weather Sensitivity");
        assertThat(prompt).contains("High Sensitivity");
        assertThat(prompt).contains("get_weather");
        assertThat(prompt).contains("weather://codes");
    }

    @Test
    void shouldGenerateOutdoorActivityPromptWithoutParameters() {
        String prompt = promptService.planOutdoorActivityPrompt(null, null, null);

        assertThat(prompt).contains("Outdoor Activity Planning");
        assertThat(prompt).doesNotContain("null");
        assertThat(prompt).contains("the requested activity");
        assertThat(prompt).contains("the requested timeframe");
    }

    @Test
    void shouldGenerateOutdoorActivityPromptForCycling() {
        String prompt = promptService.planOutdoorActivityPrompt(
                "cycling", "Lake Geneva", "tomorrow"
        );

        assertThat(prompt).contains("cycling");
        assertThat(prompt).contains("in Lake Geneva");
        assertThat(prompt).contains("tomorrow");
        assertThat(prompt).contains("Weather Sensitivity");
    }

    @Test
    void shouldGenerateTravelPromptWithParameters() {
        String prompt = promptService.weatherAwareTravelPrompt(
                "Paris", "June 10-15", "vacation"
        );

        assertThat(prompt).contains("Weather-Aware Travel Planning");
        assertThat(prompt).contains("Paris");
        assertThat(prompt).contains("June 10-15");
        assertThat(prompt).contains("vacation");
        assertThat(prompt).contains("Packing List");
        assertThat(prompt).contains("Temperature Guidance");
        assertThat(prompt).contains("get_weather");
        assertThat(prompt).contains("weather://codes");
    }

    @Test
    void shouldGenerateTravelPromptWithoutParameters() {
        String prompt = promptService.weatherAwareTravelPrompt(null, null, null);

        assertThat(prompt).contains("Weather-Aware Travel Planning");
        assertThat(prompt).doesNotContain("null");
        assertThat(prompt).contains("the destination");
        assertThat(prompt).contains("the travel dates");
        assertThat(prompt).contains("this trip");
    }

    @Test
    void shouldGenerateTravelPromptForBusinessTrip() {
        String prompt = promptService.weatherAwareTravelPrompt(
                "London", "March 5-8", "business"
        );

        assertThat(prompt).contains("London");
        assertThat(prompt).contains("March 5-8");
        assertThat(prompt).contains("business");
        assertThat(prompt).contains("Professional attire");
    }

    @Test
    void shouldGetAllPromptNames() {
        Map<String, String> prompts = promptService.getAllPromptNames();

        assertThat(prompts).hasSize(3);
        assertThat(prompts).containsKeys(
                "meteo__ski-trip-weather",
                "meteo__plan-outdoor-activity",
                "meteo__weather-aware-travel"
        );
        assertThat(prompts.get("meteo__ski-trip-weather"))
                .contains("Ski trip");
        assertThat(prompts.get("meteo__plan-outdoor-activity"))
                .contains("outdoor activity");
        assertThat(prompts.get("meteo__weather-aware-travel"))
                .contains("Travel");
    }

    @Test
    void shouldNotContainNullInAnyPrompt() {
        String skiPrompt = promptService.skiTripWeatherPrompt(null, null);
        String activityPrompt = promptService.planOutdoorActivityPrompt(null, null, null);
        String travelPrompt = promptService.weatherAwareTravelPrompt(null, null, null);

        assertThat(skiPrompt).doesNotContain("null");
        assertThat(activityPrompt).doesNotContain("null");
        assertThat(travelPrompt).doesNotContain("null");
    }

    @Test
    void shouldContainResourceReferencesInPrompts() {
        String skiPrompt = promptService.skiTripWeatherPrompt("Zermatt", "weekend");
        String activityPrompt = promptService.planOutdoorActivityPrompt("hiking", "Alps", "tomorrow");
        String travelPrompt = promptService.weatherAwareTravelPrompt("Paris", "June", "vacation");

        // All prompts should reference weather://codes
        assertThat(skiPrompt).contains("weather://codes");
        assertThat(activityPrompt).contains("weather://codes");
        assertThat(travelPrompt).contains("weather://codes");

        // Ski prompt should reference swiss-locations
        assertThat(skiPrompt).contains("weather://swiss-locations");

        // Activity prompt should reference aqi-reference
        assertThat(activityPrompt).contains("weather://aqi-reference");
    }
}
