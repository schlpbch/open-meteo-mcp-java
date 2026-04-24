package com.openmeteo.mcp.controller;

import com.openmeteo.mcp.model.dto.ApiKeyRequest;
import com.openmeteo.mcp.model.dto.SecurityAuditEvent;
import com.openmeteo.mcp.service.ApiKeyService;
import com.openmeteo.mcp.service.SecurityAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityControllerTest {

    @Mock ApiKeyService apiKeyService;
    @Mock SecurityAuditService auditService;
    @Mock Authentication authentication;
    @Mock ServerHttpRequest httpRequest;
    @Mock HttpHeaders headers;

    SecurityController controller;

    @BeforeEach
    void setUp() {
        controller = new SecurityController(apiKeyService, auditService);
        lenient().when(authentication.getName()).thenReturn("admin");
    }

    @Nested
    class GetCurrentUserTests {
        @Test
        void returnsUserInfo() {
            when(authentication.getAuthorities()).thenAnswer(inv ->
                    (Collection<? extends GrantedAuthority>) List.of(
                            (GrantedAuthority) () -> "ROLE_ADMIN"
                    ));
            when(authentication.isAuthenticated()).thenReturn(true);

            var result = controller.getCurrentUser(authentication);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("admin", result.getBody().get("username"));
            assertTrue((Boolean) result.getBody().get("authenticated"));
        }
    }

    @Nested
    class GenerateApiKeyTests {
        @BeforeEach
        void setupRequest() {
            lenient().when(httpRequest.getHeaders()).thenReturn(headers);
            lenient().when(headers.getFirst("X-Forwarded-For")).thenReturn(null);
            lenient().when(headers.getFirst("X-Real-IP")).thenReturn(null);
            lenient().when(httpRequest.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 9000));
        }

        @Test
        void createsApiKey() {
            var request = new ApiKeyRequest("test-client", List.of("MCP_CLIENT"), "Test");
            when(apiKeyService.generateApiKey("test-client", List.of("MCP_CLIENT"), "Test"))
                    .thenReturn("generated-key-abc123");

            var result = controller.generateApiKey(request, authentication, httpRequest);

            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals("generated-key-abc123", result.getBody().apiKey());
            assertEquals("test-client", result.getBody().clientName());
            verify(auditService).logApiKeyGeneration(eq("admin"), eq("test-client"),
                    any(), anyString());
        }

        @Test
        void returnsBadRequestOnIllegalArgument() {
            var request = new ApiKeyRequest("test-client", List.of("MCP_CLIENT"), "Test");
            when(apiKeyService.generateApiKey(any(), any(), any()))
                    .thenThrow(new IllegalArgumentException("Invalid role"));

            var result = controller.generateApiKey(request, authentication, httpRequest);

            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        }

        @Test
        void usesXForwardedForHeader() {
            when(headers.getFirst("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
            var request = new ApiKeyRequest("client", List.of("MCP_CLIENT"), null);
            when(apiKeyService.generateApiKey(any(), any(), any())).thenReturn("key");

            var result = controller.generateApiKey(request, authentication, httpRequest);

            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            verify(auditService).logApiKeyGeneration(any(), any(), any(), eq("192.168.1.1"));
        }

        @Test
        void usesXRealIpHeader() {
            when(headers.getFirst("X-Forwarded-For")).thenReturn(null);
            when(headers.getFirst("X-Real-IP")).thenReturn("10.0.0.5");
            var request = new ApiKeyRequest("client", List.of("MCP_CLIENT"), null);
            when(apiKeyService.generateApiKey(any(), any(), any())).thenReturn("key");

            var result = controller.generateApiKey(request, authentication, httpRequest);

            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            verify(auditService).logApiKeyGeneration(any(), any(), any(), eq("10.0.0.5"));
        }
    }

    @Nested
    class ListApiKeysTests {
        @Test
        void returnsActiveKeysByDefault() {
            var info = mock(ApiKeyService.ApiKeyInfo.class);
            when(info.getName()).thenReturn("client");
            when(info.getRoles()).thenReturn(List.of("MCP_CLIENT"));
            when(info.getDescription()).thenReturn("desc");
            when(info.isActive()).thenReturn(true);
            when(info.getCreatedAt()).thenReturn(System.currentTimeMillis());
            when(apiKeyService.listActiveApiKeys()).thenReturn(List.of(info));

            var result = controller.listApiKeys(false, authentication);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertEquals(1, result.getBody().size());
            verify(apiKeyService).listActiveApiKeys();
            verify(apiKeyService, never()).listAllApiKeys();
        }

        @Test
        void returnsAllKeysWhenIncludeInactive() {
            when(apiKeyService.listAllApiKeys()).thenReturn(List.of());

            var result = controller.listApiKeys(true, authentication);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(apiKeyService).listAllApiKeys();
        }
    }

    @Nested
    class ApiKeyStatisticsTests {
        @Test
        void returnsStatistics() {
            when(apiKeyService.getApiKeyStatistics())
                    .thenReturn(Map.of("total", 5L, "active", 3L));

            var result = controller.getApiKeyStatistics(authentication);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(5L, result.getBody().get("total"));
        }
    }

    @Nested
    class RevokeApiKeyTests {
        @BeforeEach
        void setupRequest() {
            lenient().when(httpRequest.getHeaders()).thenReturn(headers);
            lenient().when(headers.getFirst("X-Forwarded-For")).thenReturn(null);
            lenient().when(headers.getFirst("X-Real-IP")).thenReturn(null);
            lenient().when(httpRequest.getRemoteAddress()).thenReturn(null);
        }

        @Test
        void revokesExistingKey() {
            when(apiKeyService.revokeApiKey("my-api-key")).thenReturn(true);

            var result = controller.revokeApiKey("my-api-key", authentication, httpRequest);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("revoked", result.getBody().get("status"));
        }

        @Test
        void returns404ForUnknownKey() {
            when(apiKeyService.revokeApiKey("unknown")).thenReturn(false);

            var result = controller.revokeApiKey("unknown", authentication, httpRequest);

            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        }
    }

    @Nested
    class UpdateApiKeyRolesTests {
        @Test
        void updatesRoles() {
            when(apiKeyService.updateApiKeyRoles("key", List.of("ADMIN"))).thenReturn(true);

            var result = controller.updateApiKeyRoles("key",
                    Map.of("roles", List.of("ADMIN")), authentication);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(List.of("ADMIN"), result.getBody().get("roles"));
        }

        @Test
        void returns404ForUnknownKey() {
            when(apiKeyService.updateApiKeyRoles("unknown", List.of("ADMIN"))).thenReturn(false);

            var result = controller.updateApiKeyRoles("unknown",
                    Map.of("roles", List.of("ADMIN")), authentication);

            assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        }

        @Test
        void returnsBadRequestForEmptyRoles() {
            var result = controller.updateApiKeyRoles("key",
                    Map.of("roles", List.of()), authentication);

            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        }

        @Test
        void returnsBadRequestForMissingRoles() {
            var result = controller.updateApiKeyRoles("key", Map.of(), authentication);

            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        }

        @Test
        void returnsBadRequestOnIllegalArgument() {
            when(apiKeyService.updateApiKeyRoles(any(), any()))
                    .thenThrow(new IllegalArgumentException("Bad role"));

            var result = controller.updateApiKeyRoles("key",
                    Map.of("roles", List.of("BAD")), authentication);

            assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        }
    }

    @Nested
    class AuditEventsTests {
        @Test
        void returnsRecentEventsWithoutPrincipal() {
            when(auditService.getRecentEvents(50)).thenReturn(List.of());

            var result = controller.getAuditEvents(50, null, authentication);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(auditService).getRecentEvents(50);
        }

        @Test
        void returnsEventsByPrincipal() {
            when(auditService.getEventsByPrincipal("user1", 10)).thenReturn(List.of());

            var result = controller.getAuditEvents(10, "user1", authentication);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(auditService).getEventsByPrincipal("user1", 10);
        }

        @Test
        void returnsFailedAuthAttempts() {
            when(auditService.getFailedAuthAttempts(any(Instant.class))).thenReturn(List.of());

            var result = controller.getFailedAuthAttempts(24, authentication);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(auditService).getFailedAuthAttempts(any(Instant.class));
        }
    }

    @Nested
    class MiscEndpointsTests {
        @Test
        void listsMcpToolsEmpty() {
            var result = controller.listMcpTools();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertNotNull(result.getBody());
            assertTrue(result.getBody().isEmpty());
        }

        @Test
        void returnsSecurityHealth() {
            when(auditService.getEventCount()).thenReturn(42);

            var result = controller.securityHealth();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("UP", result.getBody().get("status"));
            assertEquals("enabled", result.getBody().get("security"));
            assertEquals(42, result.getBody().get("auditEventCount"));
        }
    }
}
