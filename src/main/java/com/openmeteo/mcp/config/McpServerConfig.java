package com.openmeteo.mcp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.server.McpServer;
import org.springframework.ai.mcp.server.McpServerBuilder;
import org.springframework.ai.mcp.server.transport.SseTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * MCP (Model Context Protocol) Server configuration for Spring Boot.
 *
 * Configures the MCP server to expose:
 * - 4 MCP Tools (search_location, get_weather, get_snow_conditions, get_air_quality)
 * - 3 MCP Prompts (ski-trip-weather, plan-outdoor-activity, weather-aware-travel)
 * - 4 MCP Resources (weather://codes, weather://parameters, weather://aqi-reference, weather://swiss-locations)
 *
 * The MCP server is exposed via HTTP/SSE transport at /mcp/sse endpoint.
 * This allows MCP clients and inspectors to connect and discover available tools, prompts, and resources.
 */
@Configuration
public class McpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(McpServerConfig.class);

    /**
     * Configures the MCP Server bean with HTTP/SSE transport.
     *
     * The server automatically discovers:
     * - @McpTool annotated methods from components
     * - @McpPrompt annotated methods from components
     * - @McpResource annotated methods from components
     *
     * @param mcpServerBuilder McpServerBuilder for building the MCP server
     * @return configured McpServer instance
     */
    @Bean
    public McpServer mcpServer(McpServerBuilder mcpServerBuilder) {
        log.info("Configuring MCP Server with HTTP/SSE transport");

        McpServer server = mcpServerBuilder
                .name("open-meteo-mcp")
                .version("1.0.0-alpha")
                .description("Model Context Protocol server providing weather, snow conditions, and air quality tools via Open-Meteo API")
                .build();

        log.info("MCP Server configured and ready for connections");
        return server;
    }

    /**
     * Configures the HTTP/SSE transport endpoint for the MCP server.
     *
     * The MCP server is exposed at /mcp/sse endpoint using Server-Sent Events transport.
     * This allows MCP clients to connect via HTTP/SSE.
     *
     * @param mcpServer the MCP server instance
     * @return router function for the MCP SSE endpoint
     */
    @Bean
    public RouterFunction<ServerResponse> mcpRoutes(McpServer mcpServer) {
        log.info("Configuring MCP SSE endpoint at /mcp/sse");

        SseTransport transport = new SseTransport(mcpServer);

        return RouterFunctions.route()
                .GET("/mcp/sse", transport::handle)
                .build();
    }
}
