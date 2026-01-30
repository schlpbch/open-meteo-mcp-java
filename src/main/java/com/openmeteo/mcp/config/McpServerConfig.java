package com.openmeteo.mcp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * MCP (Model Context Protocol) Server configuration for Spring Boot.
 *
 * Configures the application to expose MCP-annotated components:
 * - 4 MCP Tools (search_location, get_weather, get_snow_conditions, get_air_quality)
 * - 3 MCP Prompts (ski-trip-weather, plan-outdoor-activity, weather-aware-travel)
 * - 4 MCP Resources (weather://codes, weather://parameters, weather://aqi-reference, weather://swiss-locations)
 *
 * The MCP-annotated methods are available via:
 * - REST API endpoints at /api/tools/* for tools
 * - Spring component methods with @McpTool, @McpPrompt, @McpResource annotations
 *
 * Component Registration:
 * - McpToolsHandler (@Component) - Exposes 4 MCP tools
 * - PromptService (@Component) - Exposes 3 MCP prompts
 * - ResourceService (@Component) - Exposes 4 MCP resources
 *
 * When Spring AI MCP server support becomes available, these components
 * can be automatically discovered and exposed via MCP protocol (SSE/stdio/WebSocket).
 */
@Configuration
public class McpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(McpServerConfig.class);

    public McpServerConfig() {
        log.info("MCP Server configuration initialized");
        log.info("  - MCP Tools: search_location, get_weather, get_snow_conditions, get_air_quality");
        log.info("  - MCP Prompts: ski-trip-weather, plan-outdoor-activity, weather-aware-travel");
        log.info("  - MCP Resources: weather://codes, weather://parameters, weather://aqi-reference, weather://swiss-locations");
        log.info("  - Available via REST API at /api/tools/* endpoints");
    }
}
