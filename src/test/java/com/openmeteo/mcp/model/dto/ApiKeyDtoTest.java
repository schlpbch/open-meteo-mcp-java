package com.openmeteo.mcp.model.dto;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyDtoTest {

    @Nested
    class ApiKeyRequestTests {
        @Test
        void createsValidRequest() {
            var request = new ApiKeyRequest("my-client", List.of("MCP_CLIENT"), "Test key");

            assertEquals("my-client", request.clientName());
            assertEquals(List.of("MCP_CLIENT"), request.roles());
            assertEquals("Test key", request.description());
        }

        @Test
        void throwsOnBlankClientName() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ApiKeyRequest("  ", List.of("MCP_CLIENT"), null));
        }

        @Test
        void throwsOnNullClientName() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ApiKeyRequest(null, List.of("MCP_CLIENT"), null));
        }

        @Test
        void throwsOnEmptyRoles() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ApiKeyRequest("client", List.of(), "desc"));
        }

        @Test
        void throwsOnNullRoles() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ApiKeyRequest("client", null, "desc"));
        }

        @Test
        void allowsNullDescription() {
            var request = new ApiKeyRequest("client", List.of("ADMIN"), null);
            assertNull(request.description());
        }

        @Test
        void supportsMultipleRoles() {
            var roles = List.of("MCP_CLIENT", "ADMIN");
            var request = new ApiKeyRequest("client", roles, null);
            assertEquals(2, request.roles().size());
        }
    }

    @Nested
    class ApiKeyResponseTests {
        @Test
        void forGenerationIncludesKey() {
            var response = ApiKeyResponse.forGeneration(
                    "secret-key-123",
                    "my-client",
                    List.of("MCP_CLIENT"),
                    "Test key"
            );

            assertEquals("secret-key-123", response.apiKey());
            assertEquals("my-client", response.clientName());
            assertEquals(List.of("MCP_CLIENT"), response.roles());
            assertEquals("Test key", response.description());
            assertTrue(response.active());
            assertNotNull(response.createdAt());
            assertNotNull(response.message());
        }

        @Test
        void forInfoExcludesKey() {
            var createdAt = System.currentTimeMillis();
            var response = ApiKeyResponse.forInfo(
                    "my-client",
                    List.of("ADMIN"),
                    "Admin key",
                    true,
                    createdAt
            );

            assertNull(response.apiKey());
            assertEquals("my-client", response.clientName());
            assertTrue(response.active());
            assertEquals(createdAt, response.createdAt().toEpochMilli());
        }

        @Test
        void forInfoHandlesInactiveKey() {
            var response = ApiKeyResponse.forInfo("client", List.of("MCP_CLIENT"), null, false, 0L);

            assertFalse(response.active());
        }
    }
}
