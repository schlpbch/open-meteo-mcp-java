package com.openmeteo.mcp.service;

import com.openmeteo.mcp.service.ApiKeyService.ApiKeyInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for ApiKeyService.
 * 
 * Tests API key management functionality as specified in ADR-019 Phase 2.
 */
@DisplayName("API Key Service Tests")
class ApiKeyServiceTest {

    private ApiKeyService apiKeyService;

    @BeforeEach
    void setUp() {
        apiKeyService = new ApiKeyService(300); // 300 seconds cache TTL
    }

    @Test
    @DisplayName("Should generate API key with valid roles")
    void shouldGenerateApiKeyWithValidRoles() {
        // When
        String apiKey = apiKeyService.generateApiKey("test-client", List.of("MCP_CLIENT"), "Test client key");

        // Then
        assertThat(apiKey).isNotNull();
        assertThat(apiKey).startsWith("ak_");
        
        Optional<ApiKeyInfo> info = apiKeyService.getApiKeyInfo(apiKey);
        assertThat(info).isPresent();
        assertThat(info.get().getName()).isEqualTo("test-client");
        assertThat(info.get().getRoles()).containsExactly("MCP_CLIENT");
        assertThat(info.get().getDescription()).isEqualTo("Test client key");
        assertThat(info.get().isActive()).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid roles")
    void shouldRejectInvalidRoles() {
        // When/Then
        assertThatThrownBy(() -> 
            apiKeyService.generateApiKey("test-client", List.of("INVALID_ROLE"), "Test")
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid roles");
    }

    @Test
    @DisplayName("Should validate API key")
    void shouldValidateApiKey() {
        // Given
        String apiKey = apiKeyService.generateApiKey("test-client", List.of("MCP_CLIENT"), null);

        // When
        boolean valid = apiKeyService.isValidApiKey(apiKey);

        // Then
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("Should not validate invalid API key")
    void shouldNotValidateInvalidApiKey() {
        // When
        boolean valid = apiKeyService.isValidApiKey("invalid_key");

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should not validate revoked API key")
    void shouldNotValidateRevokedApiKey() {
        // Given
        String apiKey = apiKeyService.generateApiKey("test-client", List.of("MCP_CLIENT"), null);
        apiKeyService.revokeApiKey(apiKey);

        // When
        boolean valid = apiKeyService.isValidApiKey(apiKey);

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Should revoke API key")
    void shouldRevokeApiKey() {
        // Given
        String apiKey = apiKeyService.generateApiKey("test-client", List.of("MCP_CLIENT"), null);

        // When
        boolean revoked = apiKeyService.revokeApiKey(apiKey);

        // Then
        assertThat(revoked).isTrue();
        Optional<ApiKeyInfo> info = apiKeyService.getApiKeyInfo(apiKey);
        assertThat(info).isPresent();
        assertThat(info.get().isActive()).isFalse();
    }

    @Test
    @DisplayName("Should update API key roles")
    void shouldUpdateApiKeyRoles() {
        // Given
        String apiKey = apiKeyService.generateApiKey("test-client", List.of("MCP_CLIENT"), null);

        // When
        boolean updated = apiKeyService.updateApiKeyRoles(apiKey, List.of("ADMIN", "MCP_CLIENT"));

        // Then
        assertThat(updated).isTrue();
        Optional<ApiKeyInfo> info = apiKeyService.getApiKeyInfo(apiKey);
        assertThat(info).isPresent();
        assertThat(info.get().getRoles()).containsExactlyInAnyOrder("ADMIN", "MCP_CLIENT");
    }

    @Test
    @DisplayName("Should not update roles for non-existent API key")
    void shouldNotUpdateRolesForNonExistentApiKey() {
        // When
        boolean updated = apiKeyService.updateApiKeyRoles("non_existent_key", List.of("ADMIN"));

        // Then
        assertThat(updated).isFalse();
    }

    @Test
    @DisplayName("Should delete API key")
    void shouldDeleteApiKey() {
        // Given
        String apiKey = apiKeyService.generateApiKey("test-client", List.of("MCP_CLIENT"), null);

        // When
        boolean deleted = apiKeyService.deleteApiKey(apiKey);

        // Then
        assertThat(deleted).isTrue();
        assertThat(apiKeyService.isValidApiKey(apiKey)).isFalse();
        assertThat(apiKeyService.getApiKeyInfo(apiKey)).isEmpty();
    }

    @Test
    @DisplayName("Should reactivate API key")
    void shouldReactivateApiKey() {
        // Given
        String apiKey = apiKeyService.generateApiKey("test-client", List.of("MCP_CLIENT"), null);
        apiKeyService.revokeApiKey(apiKey);
        assertThat(apiKeyService.isValidApiKey(apiKey)).isFalse();

        // When
        boolean reactivated = apiKeyService.reactivateApiKey(apiKey);

        // Then
        assertThat(reactivated).isTrue();
        assertThat(apiKeyService.isValidApiKey(apiKey)).isTrue();
    }

    @Test
    @DisplayName("Should list all API keys")
    void shouldListAllApiKeys() {
        // Given
        apiKeyService.generateApiKey("client1", List.of("MCP_CLIENT"), null);
        apiKeyService.generateApiKey("client2", List.of("ADMIN"), null);
        apiKeyService.generateApiKey("client3", List.of("PUBLIC"), null);

        // When
        List<ApiKeyInfo> allKeys = apiKeyService.listAllApiKeys();

        // Then
        assertThat(allKeys).hasSize(5); // 3 new + 2 dev keys
        assertThat(allKeys.stream().map(ApiKeyInfo::getName))
                .contains("client1", "client2", "client3");
    }

    @Test
    @DisplayName("Should get API key statistics")
    void shouldGetApiKeyStatistics() {
        // Given
        apiKeyService.generateApiKey("client1", List.of("MCP_CLIENT"), null);
        String toRevoke = apiKeyService.generateApiKey("client2", List.of("ADMIN"), null);
        apiKeyService.revokeApiKey(toRevoke);

        // When
        Map<String, Long> stats = apiKeyService.getApiKeyStatistics();

        // Then
        assertThat(stats).containsKeys("total", "active", "inactive");
        assertThat(stats.get("total")).isEqualTo(4); // 2 new + 2 dev keys
        assertThat(stats.get("active")).isEqualTo(3); // 1 active new + 2 dev keys
        assertThat(stats.get("inactive")).isEqualTo(1); // 1 revoked
    }

    @Test
    @DisplayName("Should handle concurrent API key generation")
    void shouldHandleConcurrentApiKeyGeneration() throws InterruptedException {
        // Given
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        String[] generatedKeys = new String[threadCount];

        // When - generate API keys concurrently
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                generatedKeys[index] = apiKeyService.generateApiKey(
                        "client" + index,
                        List.of("MCP_CLIENT"),
                        "Concurrent test"
                );
            });
            threads[i].start();
        }

        // Wait for all threads
        for (Thread thread : threads) {
            thread.join();
        }

        // Then - all keys should be unique and valid
        assertThat(generatedKeys).doesNotContainNull();
        assertThat(generatedKeys).doesNotHaveDuplicates();
        for (String key : generatedKeys) {
            assertThat(apiKeyService.isValidApiKey(key)).isTrue();
        }
    }
}
