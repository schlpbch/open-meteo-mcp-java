package com.openmeteo.mcp.model.chat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatStreamRequestTest {

    @Nested
    class FactoryMethodTests {
        @Test
        void simpleRequestHasNoWeather() {
            var request = ChatStreamRequest.simple("s1", "hello");

            assertEquals("s1", request.sessionId());
            assertEquals("hello", request.message());
            assertFalse(request.shouldIncludeWeather());
            assertNull(request.latitude());
            assertNull(request.longitude());
        }

        @Test
        void withWeatherIncludesCoordinates() {
            var request = ChatStreamRequest.withWeather("s1", "weather?", 47.3, 8.5);

            assertTrue(request.shouldIncludeWeather());
            assertEquals(47.3, request.latitude());
            assertEquals(8.5, request.longitude());
        }

        @Test
        void withOptionsHasCustomParameters() {
            var request = ChatStreamRequest.withOptions("s1", "msg", 0.5, 1000);

            assertEquals(0.5, request.temperature());
            assertEquals(1000, request.maxTokens());
            assertFalse(request.shouldIncludeWeather());
        }
    }

    @Nested
    class DefaultValueTests {
        @Test
        void defaultTemperatureIs0_7() {
            var request = ChatStreamRequest.simple("s1", "msg");
            assertEquals(0.7, request.getTemperature());
        }

        @Test
        void defaultMaxTokensIs2000() {
            var request = ChatStreamRequest.simple("s1", "msg");
            assertEquals(2000, request.getMaxTokens());
        }

        @Test
        void customTemperatureOverridesDefault() {
            var request = ChatStreamRequest.withOptions("s1", "msg", 0.2, 500);
            assertEquals(0.2, request.getTemperature());
            assertEquals(500, request.getMaxTokens());
        }
    }

    @Nested
    class ShouldIncludeWeatherTests {
        @Test
        void falseWhenIncludeWeatherIsNull() {
            var request = new ChatStreamRequest("s1", "msg", null, 47.3, 8.5, null, null);
            assertFalse(request.shouldIncludeWeather());
        }

        @Test
        void falseWhenLatitudeIsNull() {
            var request = new ChatStreamRequest("s1", "msg", true, null, 8.5, null, null);
            assertFalse(request.shouldIncludeWeather());
        }

        @Test
        void falseWhenLongitudeIsNull() {
            var request = new ChatStreamRequest("s1", "msg", true, 47.3, null, null, null);
            assertFalse(request.shouldIncludeWeather());
        }

        @Test
        void trueWhenAllWeatherFieldsPresent() {
            var request = new ChatStreamRequest("s1", "msg", true, 47.3, 8.5, null, null);
            assertTrue(request.shouldIncludeWeather());
        }
    }
}
