package com.openmeteo.mcp.security;

import com.openmeteo.mcp.service.ApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for Spring Security configuration.
 * 
 * Tests security configuration, API key authentication, 
 * and authorization as specified in ADR-019.
 */
@SpringBootTest
@DisplayName("Security Configuration Integration Tests")
class SecurityConfigIntegrationTest {

    private WebTestClient webTestClient;

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ApplicationContext context;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    @DisplayName("Should allow access to public health endpoint")
    void shouldAllowAccessToPublicHealthEndpoint() {
        webTestClient.get()
                .uri("/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should allow access to public actuator endpoints")
    void shouldAllowAccessToPublicActuatorEndpoints() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should deny access to MCP endpoints without authentication")
    void shouldDenyAccessToMcpEndpointsWithoutAuth() {
        webTestClient.get()
                .uri("/api/mcp/tools")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("unauthorized");
    }

    @Test
    @DisplayName("Should allow access to MCP endpoints with valid API key")
    void shouldAllowAccessToMcpEndpointsWithValidApiKey() {
        webTestClient.get()
                .uri("/api/mcp/tools")
                .header("X-API-Key", "mcp-client-dev-key-12345")
                .exchange()
                .expectStatus().isNotFound(); // 404 because endpoint doesn't exist yet, but auth passed
    }

    @Test
    @DisplayName("Should deny access to MCP endpoints with invalid API key")
    void shouldDenyAccessToMcpEndpointsWithInvalidApiKey() {
        webTestClient.get()
                .uri("/api/mcp/tools")
                .header("X-API-Key", "invalid-key")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Should deny access to admin endpoints without authentication")
    void shouldDenyAccessToAdminEndpointsWithoutAuth() {
        webTestClient.get()
                .uri("/api/admin/users")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.error").isEqualTo("unauthorized");
    }

    @Test
    @DisplayName("Should allow access to admin endpoints with admin API key")
    void shouldAllowAccessToAdminEndpointsWithAdminApiKey() {
        webTestClient.get()
                .uri("/api/admin/users")
                .header("X-API-Key", "admin-dev-key-67890")
                .exchange()
                .expectStatus().isNotFound(); // 404 because endpoint doesn't exist yet, but auth passed
    }

    @Test
    @DisplayName("Should deny access to admin endpoints with MCP client API key")
    void shouldDenyAccessToAdminEndpointsWithMcpClientApiKey() {
        webTestClient.get()
                .uri("/api/admin/users")
                .header("X-API-Key", "mcp-client-dev-key-12345")
                .exchange()
                .expectStatus().isForbidden(); // 403 Forbidden - authenticated but not authorized
    }

    @Test
    @DisplayName("Should support Authorization header with ApiKey format")
    void shouldSupportAuthorizationHeaderWithApiKeyFormat() {
        webTestClient.get()
                .uri("/api/mcp/tools")
                .header("Authorization", "ApiKey mcp-client-dev-key-12345")
                .exchange()
                .expectStatus().isNotFound(); // 404 because endpoint doesn't exist yet, but auth passed
    }

    @Test
    @DisplayName("Should include CORS headers for MCP endpoints")
    void shouldIncludeCorsHeadersForMcpEndpoints() {
        webTestClient.get()
                .uri("/api/mcp/tools")
                .header("X-API-Key", "mcp-client-dev-key-12345")
                .header("Origin", "http://localhost:3000")
                .exchange()
                .expectHeader().exists("Access-Control-Allow-Origin");
    }

    @Test
    @DisplayName("Should handle OPTIONS preflight request")
    void shouldHandleOptionsPreflightRequest() {
        webTestClient.options()
                .uri("/api/mcp/tools")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "X-API-Key")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Access-Control-Allow-Origin")
                .expectHeader().exists("Access-Control-Allow-Methods")
                .expectHeader().exists("Access-Control-Allow-Headers");
    }
}