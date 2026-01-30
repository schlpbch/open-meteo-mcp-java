package com.openmeteo.mcp.service;

import com.openmeteo.mcp.client.OpenMeteoClient;
import com.openmeteo.mcp.model.dto.GeocodingResponse;
import com.openmeteo.mcp.model.dto.GeocodingResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LocationService.
 */
@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private OpenMeteoClient client;

    @InjectMocks
    private LocationService service;

    @Test
    void shouldSearchLocationSuccessfully() {
        // Arrange
        var result1 = new GeocodingResult(
                2657896L, "Zurich", 47.3769, 8.5417,
                408.0, "PPL", "CH",
                null, null, null, null,
                "Europe/Zurich", null, "Switzerland", null
        );
        var mockResponse = new GeocodingResponse(List.of(result1), 1.5);

        when(client.searchLocation(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        var result = service.searchLocation("Zurich", 10, "en", "CH").join();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.results()).hasSize(1);
        assertThat(result.results().get(0).name()).isEqualTo("Zurich");
        assertThat(result.results().get(0).latitude()).isEqualTo(47.3769);
        assertThat(result.results().get(0).countryCode()).isEqualTo("CH");

        verify(client).searchLocation("Zurich", 10, "en", "CH");
    }

    @Test
    void shouldValidateLocationName() {
        // Act & Assert
        assertThatThrownBy(() ->
                service.searchLocation("", 10, "en", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location name");

        assertThatThrownBy(() ->
                service.searchLocation(null, 10, "en", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Location name");
    }

    @Test
    void shouldClampCountToMaximum() {
        // Arrange
        var mockResponse = new GeocodingResponse(List.of(), 1.0);
        when(client.searchLocation("Zurich", 100, "en", null))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act - Request 200 results (should be clamped to 100)
        service.searchLocation("Zurich", 200, "en", null).join();

        // Assert - Verify clamped to 100
        verify(client).searchLocation("Zurich", 100, "en", null);
    }

    @Test
    void shouldClampCountToMinimum() {
        // Arrange
        var mockResponse = new GeocodingResponse(List.of(), 1.0);
        when(client.searchLocation("Zurich", 1, "en", null))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act - Request 0 results (should be clamped to 1)
        service.searchLocation("Zurich", 0, "en", null).join();

        // Assert - Verify clamped to 1
        verify(client).searchLocation("Zurich", 1, "en", null);
    }

    @Test
    void shouldFindBestMatch() {
        // Arrange
        var result1 = new GeocodingResult(
                2657896L, "Zurich", 47.3769, 8.5417,
                null, "PPL", "CH",
                null, null, null, null,
                "Europe/Zurich", null, "Switzerland", null
        );
        var mockResponse = new GeocodingResponse(List.of(result1), 1.5);

        when(client.searchLocation(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        var result = service.findBestMatch("Zurich", "CH").join();

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Zurich");
        assertThat(result.get().countryCode()).isEqualTo("CH");

        verify(client).searchLocation("Zurich", 1, "en", "CH");
    }

    @Test
    void shouldReturnEmptyWhenNoBestMatch() {
        // Arrange
        var mockResponse = new GeocodingResponse(List.of(), 1.0);

        when(client.searchLocation(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        var result = service.findBestMatch("NonExistent", "XX").join();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNullResults() {
        // Arrange
        var mockResponse = new GeocodingResponse(null, 1.0);

        when(client.searchLocation("Test", 1, "en", null))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // Act
        var result = service.findBestMatch("Test", null).join();

        // Assert
        assertThat(result).isEmpty();
    }
}
