package com.openmeteo.mcp.security;

import com.openmeteo.mcp.service.ApiKeyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Spring Security configuration.
 * 
 * Tests security configuration, API key authentication, 
 * and authorization as specified in ADR-019.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Configuration Integration Tests")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiKeyService apiKeyService;

    @Test
    @DisplayName("Should allow access to public health endpoint")
    void shouldAllowAccessToPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow access to public actuator endpoints")
    void shouldAllowAccessToPublicActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should deny access to MCP endpoints without authentication")
    void shouldDenyAccessToMcpEndpointsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/mcp/tools"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value("unauthorized"));
    }

    @Test
    @DisplayName("Should allow access to MCP endpoints with valid API key")
    void shouldAllowAccessToMcpEndpointsWithValidApiKey() throws Exception {
        mockMvc.perform(get("/api/mcp/tools")
                        .header("X-API-Key", "mcp-client-dev-key-12345"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but auth passed
    }

    @Test
    @DisplayName("Should deny access to MCP endpoints with invalid API key")
    void shouldDenyAccessToMcpEndpointsWithInvalidApiKey() throws Exception {
        mockMvc.perform(get("/api/mcp/tools")
                        .header("X-API-Key", "invalid-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should deny access to admin endpoints without authentication")
    void shouldDenyAccessToAdminEndpointsWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.error").value("unauthorized"));
    }

    @Test
    @DisplayName("Should allow access to admin endpoints with admin API key")
    void shouldAllowAccessToAdminEndpointsWithAdminApiKey() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-API-Key", "admin-dev-key-67890"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but auth passed
    }

    @Test
    @DisplayName("Should deny access to admin endpoints with MCP client API key")
    void shouldDenyAccessToAdminEndpointsWithMcpClientApiKey() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("X-API-Key", "mcp-client-dev-key-12345"))
                .andExpect(status().isForbidden()); // 403 Forbidden - authenticated but not authorized
    }

    @Test
    @DisplayName("Should support Authorization header with ApiKey format")
    void shouldSupportAuthorizationHeaderWithApiKeyFormat() throws Exception {
        mockMvc.perform(get("/api/mcp/tools")
                        .header("Authorization", "ApiKey mcp-client-dev-key-12345"))
                .andExpect(status().isNotFound()); // 404 because endpoint doesn't exist yet, but auth passed
    }

    @Test
    @DisplayName("Should include CORS headers for MCP endpoints")
    void shouldIncludeCorsHeadersForMcpEndpoints() throws Exception {
        mockMvc.perform(get("/api/mcp/tools")
                        .header("X-API-Key", "mcp-client-dev-key-12345")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Should handle OPTIONS preflight request")
    void shouldHandleOptionsPreflightRequest() throws Exception {
        mockMvc.perform(options("/api/mcp/tools")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "X-API-Key"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }
}