package com.openmeteo.mcp.model.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for LocationSearchRequest record.
 * <p>
 * Tests validation logic, factory methods, and record behavior.
 * </p>
 */
class LocationSearchRequestTest {

    @Test
    void shouldCreateLocationSearchRequestWithValidParameters() {
        // Arrange & Act
        var request = new LocationSearchRequest("Zurich", 10, "en", "CH");

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.name()).isEqualTo("Zurich");
        assertThat(request.count()).isEqualTo(10);
        assertThat(request.language()).isEqualTo("en");
        assertThat(request.country()).isEqualTo("CH");
    }

    @Test
    void shouldCreateLocationSearchRequestWithDefaultLanguage() {
        // Arrange & Act
        var request = new LocationSearchRequest("Zurich", 10, null, "CH");

        // Assert
        assertThat(request.language()).isEqualTo("en");
    }

    @Test
    void shouldCreateLocationSearchRequestWithBlankLanguage() {
        // Arrange & Act
        var request = new LocationSearchRequest("Zurich", 10, "  ", "CH");

        // Assert
        assertThat(request.language()).isEqualTo("en");
    }

    @Test
    void shouldCreateLocationSearchRequestWithNullCountry() {
        // Arrange & Act
        var request = new LocationSearchRequest("Zurich", 10, "en", null);

        // Assert
        assertThat(request.country()).isNull();
    }

    @Test
    void shouldCreateLocationSearchRequestWithDefaults() {
        // Arrange & Act
        var request = LocationSearchRequest.withDefaults("Zurich");

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.name()).isEqualTo("Zurich");
        assertThat(request.count()).isEqualTo(10);
        assertThat(request.language()).isEqualTo("en");
        assertThat(request.country()).isNull();
    }

    @Test
    void shouldCreateLocationSearchRequestWithCountry() {
        // Arrange & Act
        var request = LocationSearchRequest.withCountry("Zurich", "CH");

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.name()).isEqualTo("Zurich");
        assertThat(request.count()).isEqualTo(10);
        assertThat(request.language()).isEqualTo("en");
        assertThat(request.country()).isEqualTo("CH");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void shouldThrowExceptionForInvalidName(String invalidName) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new LocationSearchRequest(invalidName, 10, "en", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Location name cannot be null or blank");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 101, 200, 1000})
    void shouldThrowExceptionForInvalidCount(int invalidCount) {
        // Arrange, Act & Assert
        assertThatThrownBy(() -> new LocationSearchRequest("Zurich", invalidCount, "en", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Result count must be between 1 and 100");
    }

    @Test
    void shouldAcceptMinimumValidCount() {
        // Arrange & Act
        var request = new LocationSearchRequest("Zurich", 1, "en", null);

        // Assert
        assertThat(request.count()).isEqualTo(1);
    }

    @Test
    void shouldAcceptMaximumValidCount() {
        // Arrange & Act
        var request = new LocationSearchRequest("Zurich", 100, "en", null);

        // Assert
        assertThat(request.count()).isEqualTo(100);
    }

    @Test
    void shouldHandleMultiWordLocationName() {
        // Arrange & Act
        var request = new LocationSearchRequest("New York City", 10, "en", "US");

        // Assert
        assertThat(request.name()).isEqualTo("New York City");
    }

    @Test
    void shouldHandleLocationNameWithSpecialCharacters() {
        // Arrange & Act
        var request = new LocationSearchRequest("São Paulo", 10, "pt", "BR");

        // Assert
        assertThat(request.name()).isEqualTo("São Paulo");
    }

    @Test
    void shouldHandleVariousLanguageCodes() {
        // Arrange & Act
        var request1 = new LocationSearchRequest("Zurich", 10, "de", null);
        var request2 = new LocationSearchRequest("Zurich", 10, "fr", null);
        var request3 = new LocationSearchRequest("Zurich", 10, "it", null);

        // Assert
        assertThat(request1.language()).isEqualTo("de");
        assertThat(request2.language()).isEqualTo("fr");
        assertThat(request3.language()).isEqualTo("it");
    }

    @Test
    void shouldHandleVariousCountryCodes() {
        // Arrange & Act
        var request1 = new LocationSearchRequest("Zurich", 10, "en", "CH");
        var request2 = new LocationSearchRequest("Paris", 10, "en", "FR");
        var request3 = new LocationSearchRequest("Berlin", 10, "en", "DE");

        // Assert
        assertThat(request1.country()).isEqualTo("CH");
        assertThat(request2.country()).isEqualTo("FR");
        assertThat(request3.country()).isEqualTo("DE");
    }

    @Test
    void shouldSupportRecordEquality() {
        // Arrange
        var request1 = new LocationSearchRequest("Zurich", 10, "en", "CH");
        var request2 = new LocationSearchRequest("Zurich", 10, "en", "CH");

        // Act & Assert
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void shouldSupportRecordInequality() {
        // Arrange
        var request1 = new LocationSearchRequest("Zurich", 10, "en", "CH");
        var request2 = new LocationSearchRequest("Geneva", 10, "en", "CH");

        // Act & Assert
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    void shouldSupportRecordToString() {
        // Arrange
        var request = new LocationSearchRequest("Zurich", 10, "en", "CH");

        // Act
        String result = request.toString();

        // Assert
        assertThat(result).contains("LocationSearchRequest");
        assertThat(result).contains("name=Zurich");
        assertThat(result).contains("count=10");
        assertThat(result).contains("language=en");
        assertThat(result).contains("country=CH");
    }

    @Test
    void shouldBeSerializable() {
        // Arrange
        var request = new LocationSearchRequest("Zurich", 10, "en", "CH");

        // Assert
        assertThat(request).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    void shouldHandleVariousCountValues() {
        // Arrange & Act
        var request1 = new LocationSearchRequest("Zurich", 1, "en", null);
        var request2 = new LocationSearchRequest("Zurich", 50, "en", null);
        var request3 = new LocationSearchRequest("Zurich", 100, "en", null);

        // Assert
        assertThat(request1.count()).isEqualTo(1);
        assertThat(request2.count()).isEqualTo(50);
        assertThat(request3.count()).isEqualTo(100);
    }

    @Test
    void shouldHandleLongLocationNames() {
        // Arrange
        String longName = "A".repeat(100);

        // Act
        var request = new LocationSearchRequest(longName, 10, "en", null);

        // Assert
        assertThat(request.name()).hasSize(100);
    }
}
