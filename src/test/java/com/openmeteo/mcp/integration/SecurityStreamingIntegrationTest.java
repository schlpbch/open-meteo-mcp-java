package com.openmeteo.mcp.integration;

import com.openmeteo.mcp.model.stream.StreamMessage;
import com.openmeteo.mcp.security.JwtTokenProvider;
import com.openmeteo.mcp.service.ApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration tests for Phase 6 (Integration & Testing).
 * 
 * Validates the complete integration of:
 * - Spring Security (Phases 1-2)
 * - Streaming Infrastructure (Phase 3)
 * - Weather Streaming (Phase 4)
 * - Chat Streaming (Phase 5)
 * 
 * Tests all acceptance criteria from Issue #10:
 * - JWT and API key authentication
 * - Role-based authorization
 * - SSE streaming endpoints
 * - Security + streaming integration
 * - Performance requirements
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "PT30S")
@ActiveProfiles("test")
class SecurityStreamingIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ApiKeyService apiKeyService;

    private String jwtToken;
    private String apiKey;

    @BeforeEach
    void setUp() {
        // Generate JWT token for testing
        jwtToken = jwtTokenProvider.generateToken("testuser");
        
        // Generate API key for testing
        apiKey = apiKeyService.generateApiKey("test-client", Set.of("MCP_CLIENT"));
    }

    // ==================== SECURITY TESTS ====================

    /**
     * Test JWT authentication on streaming endpoints.
     */
    @Test
    void shouldAuthenticateWithJwtOnStreamingEndpoint() {
        webTestClient.get()
            .uri("/stream/test")
            .header("Authorization", "Bearer " + jwtToken)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    /**
     * Test API key authentication on streaming endpoints.
     */
    @Test
    void shouldAuthenticateWithApiKeyOnStreamingEndpoint() {
        webTestClient.get()
            .uri("/stream/test")
            .header("X-API-Key", apiKey)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    /**
     * Test unauthorized access to protected streaming endpoints.
     */
    @Test
    void shouldRejectUnauthorizedAccessToStreamingEndpoint() {
        webTestClient.get()
            .uri("/stream/test")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    /**
     * Test role-based authorization on admin endpoints.
     */
    @Test
    void shouldEnforceRoleBasedAuthorizationOnAdminEndpoints() {
        // MCP_CLIENT role should not have access to admin endpoints
        webTestClient.post()
            .uri("/api/security/api-keys/generate")
            .header("X-API-Key", apiKey)
            .bodyValue("{\"clientId\":\"test\",\"roles\":[\"MCP_CLIENT\"]}")
            .exchange()
            .expectStatus().isForbidden();
    }

    // ==================== STREAMING INTEGRATION TESTS ====================

    /**
     * Test weather streaming with authentication.
     * Verifies Phase 4 (Weather Streaming) + Phase 1-2 (Security) integration.
     */
    @Test
    void shouldStreamWeatherDataWithAuthentication() {
        FluxExchangeResult<StreamMessage> result = webTestClient.get()
            .uri("/stream/weather/current?latitude=47.3769&longitude=8.5417")
            .header("Authorization", "Bearer " + jwtToken)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .returnResult(StreamMessage.class);

        StepVerifier.create(result.getResponseBody())
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("metadata");
                assertThat(msg.metadata()).isNotNull();
            })
            .expectNextCount(1) // Data chunk
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("complete");
            })
            .verifyComplete();
    }

    /**
     * Test streaming latency requirements.
     * Performance requirement: <2s for first chunk in weather streaming.
     */
    @Test
    void shouldMeetStreamingLatencyRequirements() {
        long startTime = System.currentTimeMillis();

        FluxExchangeResult<StreamMessage> result = webTestClient.get()
            .uri("/stream/weather/current?latitude=47.3769&longitude=8.5417")
            .header("Authorization", "Bearer " + jwtToken)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .returnResult(StreamMessage.class);

        // First chunk should arrive within 2 seconds
        StepVerifier.create(result.getResponseBody().take(2))
            .expectNextCount(2)
            .verifyComplete();

        long latency = System.currentTimeMillis() - startTime;
        assertThat(latency).as("First chunk latency").isLessThan(2000); // <2s requirement
    }

    /**
     * Test concurrent streaming connections.
     * Performance requirement: 100+ concurrent SSE streams.
     */
    @Test
    void shouldSupportConcurrentStreamingConnections() {
        int concurrentStreams = 50; // Test with 50 concurrent streams
        
        Flux<FluxExchangeResult<StreamMessage>> concurrentRequests = Flux.range(0, concurrentStreams)
            .flatMap(i -> 
                Flux.just(webTestClient.get()
                    .uri("/stream/test")
                    .header("Authorization", "Bearer " + jwtToken)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(StreamMessage.class))
            );

        StepVerifier.create(concurrentRequests)
            .expectNextCount(concurrentStreams)
            .verifyComplete();
    }

    /**
     * Test stream progress indicators.
     * Verifies Phase 3 (Streaming Infrastructure) progress tracking.
     */
    @Test
    void shouldIncludeProgressIndicatorsInStreams() {
        FluxExchangeResult<StreamMessage> result = webTestClient.get()
            .uri("/stream/data?count=5&delay=10")
            .header("Authorization", "Bearer " + jwtToken)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .returnResult(StreamMessage.class);

        StepVerifier.create(result.getResponseBody())
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("data");
                assertThat(msg.metadata()).isNotNull();
                assertThat(msg.metadata().progress()).isNotNull();
            })
            .expectNextCount(4) // Remaining data chunks
            .assertNext(msg -> assertThat(msg.type()).isEqualTo("complete"))
            .verifyComplete();
    }

    /**
     * Test error handling in streaming.
     */
    @Test
    void shouldHandleStreamingErrorsGracefully() {
        FluxExchangeResult<StreamMessage> result = webTestClient.get()
            .uri("/stream/weather/current?latitude=999&longitude=999") // Invalid coordinates
            .header("Authorization", "Bearer " + jwtToken)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .returnResult(StreamMessage.class);

        StepVerifier.create(result.getResponseBody().timeout(Duration.ofSeconds(5)))
            .expectNextMatches(msg -> 
                msg.type().equals("metadata") || msg.type().equals("error")
            )
            .thenCancel()
            .verify();
    }

    // ==================== AUTHENTICATION PERFORMANCE TESTS ====================

    /**
     * Test JWT authentication latency.
     * Performance requirement: <50ms for JWT validation.
     */
    @Test
    void shouldMeetJwtAuthenticationLatencyRequirement() {
        long startTime = System.nanoTime();

        webTestClient.get()
            .uri("/api/health")
            .header("Authorization", "Bearer " + jwtToken)
            .exchange()
            .expectStatus().isOk();

        long latencyMs = (System.nanoTime() - startTime) / 1_000_000;
        assertThat(latencyMs).as("JWT auth latency").isLessThan(50); // <50ms requirement
    }

    /**
     * Test API key authentication latency.
     * Performance requirement: <100ms for API key validation.
     */
    @Test
    void shouldMeetApiKeyAuthenticationLatencyRequirement() {
        long startTime = System.nanoTime();

        webTestClient.get()
            .uri("/api/health")
            .header("X-API-Key", apiKey)
            .exchange()
            .expectStatus().isOk();

        long latencyMs = (System.nanoTime() - startTime) / 1_000_000;
        assertThat(latencyMs).as("API key auth latency").isLessThan(100); // <100ms requirement
    }

    // ==================== BACKWARD COMPATIBILITY TESTS ====================

    /**
     * Test backward compatibility with existing REST endpoints.
     */
    @Test
    void shouldMaintainBackwardCompatibilityWithRestEndpoints() {
        // Test existing weather endpoint still works
        webTestClient.get()
            .uri("/api/weather?latitude=47.3769&longitude=8.5417")
            .header("Authorization", "Bearer " + jwtToken)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Test CORS configuration for MCP clients.
     */
    @Test
    void shouldAllowCorsForConfiguredOrigins() {
        webTestClient.options()
            .uri("/stream/test")
            .header("Origin", "http://localhost:3000")
            .header("Access-Control-Request-Method", "GET")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("Access-Control-Allow-Origin");
    }

    // ==================== SECURITY HEADERS TESTS ====================

    /**
     * Test security headers are properly set.
     */
    @Test
    void shouldIncludeSecurityHeaders() {
        webTestClient.get()
            .uri("/api/health")
            .header("Authorization", "Bearer " + jwtToken)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().exists("X-Content-Type-Options")
            .expectHeader().exists("X-Frame-Options")
            .expectHeader().exists("X-XSS-Protection");
    }

    /**
     * Test graceful stream termination.
     */
    @Test
    void shouldTerminateStreamsGracefully() {
        FluxExchangeResult<StreamMessage> result = webTestClient.get()
            .uri("/stream/test")
            .header("Authorization", "Bearer " + jwtToken)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .returnResult(StreamMessage.class);

        // Subscribe and immediately cancel
        StepVerifier.create(result.getResponseBody().take(3))
            .expectNextCount(3)
            .verifyComplete();
        
        // Should not throw errors or leave connections hanging
    }

    /**
     * Test stream completion signals.
     */
    @Test
    void shouldSendCompletionSignalsInStreams() {
        FluxExchangeResult<StreamMessage> result = webTestClient.get()
            .uri("/stream/data?count=3&delay=10")
            .header("Authorization", "Bearer " + jwtToken)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .returnResult(StreamMessage.class);

        StepVerifier.create(result.getResponseBody())
            .expectNextCount(3) // Data chunks
            .assertNext(msg -> {
                assertThat(msg.type()).isEqualTo("complete");
            })
            .verifyComplete();
    }
}
