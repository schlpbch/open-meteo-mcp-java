package com.openmeteo.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Open Meteo MCP Server - Java/Spring Boot Implementation
 * <p>
 * Model Context Protocol (MCP) server providing weather, snow conditions,
 * and air quality data via the Open-Meteo API.
 * </p>
 * <p>
 * Migration from Python/FastMCP v3.2.0 to Java/Spring Boot with Spring AI 2.0
 * </p>
 *
 * @version 1.0.0-alpha
 * @see <a href="https://open-meteo.com/">Open-Meteo API</a>
 * @see <a href="https://github.com/schlpbch/open-meteo-mcp">Python Reference Implementation</a>
 */
@SpringBootApplication
public class OpenMeteoMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenMeteoMcpApplication.class, args);
    }
}
