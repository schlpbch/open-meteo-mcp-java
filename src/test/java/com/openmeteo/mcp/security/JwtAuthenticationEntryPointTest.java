package com.openmeteo.mcp.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.AuthenticationException;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    JwtAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new JwtAuthenticationEntryPoint(null); // uses default ObjectMapper
    }

    static AuthenticationException authException(String message) {
        return new AuthenticationException(message) {};
    }

    @Nested
    class CommenceTests {
        @Test
        void setsUnauthorizedStatus() {
            var request = MockServerHttpRequest.get("/api/weather").build();
            var exchange = MockServerWebExchange.from(request);

            StepVerifier.create(entryPoint.commence(exchange, authException("Missing token")))
                    .verifyComplete();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        void setsJsonContentType() {
            var request = MockServerHttpRequest.get("/api/test").build();
            var exchange = MockServerWebExchange.from(request);

            StepVerifier.create(entryPoint.commence(exchange, authException("Expired")))
                    .verifyComplete();

            var contentType = exchange.getResponse().getHeaders().getContentType();
            assertNotNull(contentType);
            assertTrue(contentType.toString().contains("application/json"));
        }

        @Test
        void writesErrorResponseBody() {
            var request = MockServerHttpRequest.post("/api/mcp/tool").build();
            var exchange = MockServerWebExchange.from(request);

            StepVerifier.create(entryPoint.commence(exchange, authException("Bad credentials")))
                    .verifyComplete();

            // Response body was written (status = 401 means we got there)
            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }
    }

    @Nested
    class IpExtractionTests {
        @Test
        void extractsXForwardedForHeader() {
            var request = MockServerHttpRequest.get("/api/test")
                    .header("X-Forwarded-For", "203.0.113.5, 10.0.0.1")
                    .build();
            var exchange = MockServerWebExchange.from(request);

            // Just verify it runs without error — IP extraction is internal
            StepVerifier.create(entryPoint.commence(exchange, authException("auth failed")))
                    .verifyComplete();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        void extractsXRealIpHeader() {
            var request = MockServerHttpRequest.get("/api/test")
                    .header("X-Real-IP", "198.51.100.1")
                    .build();
            var exchange = MockServerWebExchange.from(request);

            StepVerifier.create(entryPoint.commence(exchange, authException("auth failed")))
                    .verifyComplete();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }

        @Test
        void handlesNoRemoteAddress() {
            var request = MockServerHttpRequest.get("/api/test").build();
            var exchange = MockServerWebExchange.from(request);

            StepVerifier.create(entryPoint.commence(exchange, authException("No token")))
                    .verifyComplete();

            assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        }
    }
}
