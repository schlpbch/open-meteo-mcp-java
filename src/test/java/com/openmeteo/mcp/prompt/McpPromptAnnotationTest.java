package com.openmeteo.mcp.prompt;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpPrompt;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PromptService MCP prompt annotations.
 */
class McpPromptAnnotationTest {

    @Test
    void shouldHaveMcpPromptAnnotationOnSkiTripWeatherPrompt() throws NoSuchMethodException {
        var method = PromptService.class.getMethod("skiTripWeatherPrompt", String.class, String.class);

        assertThat(method.getAnnotation(McpPrompt.class)).isNotNull();
        McpPrompt annotation = method.getAnnotation(McpPrompt.class);
        assertThat(annotation.name()).isEqualTo("ski-trip-weather");
        assertThat(annotation.description()).containsIgnoringCase("ski");
    }

    @Test
    void shouldHaveMcpPromptAnnotationOnPlanOutdoorActivityPrompt() throws NoSuchMethodException {
        var method = PromptService.class.getMethod(
                "planOutdoorActivityPrompt", String.class, String.class, String.class
        );

        assertThat(method.getAnnotation(McpPrompt.class)).isNotNull();
        McpPrompt annotation = method.getAnnotation(McpPrompt.class);
        assertThat(annotation.name()).isEqualTo("plan-outdoor-activity");
        assertThat(annotation.description()).contains("outdoor activity");
    }

    @Test
    void shouldHaveMcpPromptAnnotationOnWeatherAwareTravelPrompt() throws NoSuchMethodException {
        var method = PromptService.class.getMethod(
                "weatherAwareTravelPrompt", String.class, String.class, String.class
        );

        assertThat(method.getAnnotation(McpPrompt.class)).isNotNull();
        McpPrompt annotation = method.getAnnotation(McpPrompt.class);
        assertThat(annotation.name()).isEqualTo("weather-aware-travel");
        assertThat(annotation.description()).contains("Travel");
    }

    @Test
    void shouldHaveAllMcpPromptAnnotationsWithValidNamesAndDescriptions() throws NoSuchMethodException {
        McpPrompt skiAnnotation = PromptService.class
                .getMethod("skiTripWeatherPrompt", String.class, String.class)
                .getAnnotation(McpPrompt.class);

        McpPrompt activityAnnotation = PromptService.class
                .getMethod("planOutdoorActivityPrompt", String.class, String.class, String.class)
                .getAnnotation(McpPrompt.class);

        McpPrompt travelAnnotation = PromptService.class
                .getMethod("weatherAwareTravelPrompt", String.class, String.class, String.class)
                .getAnnotation(McpPrompt.class);

        assertThat(skiAnnotation.name()).isNotBlank();
        assertThat(skiAnnotation.description()).isNotBlank();

        assertThat(activityAnnotation.name()).isNotBlank();
        assertThat(activityAnnotation.description()).isNotBlank();

        assertThat(travelAnnotation.name()).isNotBlank();
        assertThat(travelAnnotation.description()).isNotBlank();
    }
}
