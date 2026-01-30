package com.openmeteo.mcp.config;

import com.openmeteo.mcp.prompt.PromptService;
import com.openmeteo.mcp.resource.ResourceService;
import com.openmeteo.mcp.tool.McpToolsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
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
 * When Spring AI MCP server support becomes available, these components
 * can be automatically discovered and exposed via MCP protocol (SSE/stdio/WebSocket).
 */
@Configuration
public class McpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(McpServerConfig.class);

    /**
     * Ensures MCP components are properly initialized and available.
     *
     * Injects the MCP handler, prompt service, and resource service to guarantee
     * they are instantiated and their @McpTool, @McpPrompt, @McpResource annotations
     * are processed by Spring.
     *
     * @param toolsHandler the MCP tools handler component
     * @param promptService the prompt service component
     * @param resourceService the resource service component
     * @return the tools handler (for bean registration)
     */
    @Bean
    public McpToolsHandler mcpComponents(
            McpToolsHandler toolsHandler,
            PromptService promptService,
            ResourceService resourceService
    ) {
        log.info("MCP components initialized:");
        log.info("  - MCP Tools: search_location, get_weather, get_snow_conditions, get_air_quality");
        log.info("  - MCP Prompts: ski-trip-weather, plan-outdoor-activity, weather-aware-travel");
        log.info("  - MCP Resources: weather://codes, weather://parameters, weather://aqi-reference, weather://swiss-locations");
        log.info("Available via REST API at /api/tools/* endpoints");

        return toolsHandler;
    }
}
