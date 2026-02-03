package com.openmeteo.mcp.chat.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ChatHandler model classes.
 */
class ChatModelTest {
    
    @Test
    void testChatSessionCreation() {
        // When
        var session = ChatSession.create("test-session");
        
        // Then
        assertEquals("test-session", session.sessionId());
        assertNotNull(session.context());
        assertNotNull(session.createdAt());
        assertNotNull(session.lastActivity());
    }
    
    @Test
    void testChatSessionTouch() {
        // Given
        var session = ChatSession.create("test-session");
        var originalActivity = session.lastActivity();
        
        // When
        try {
            Thread.sleep(10); // Ensure time difference
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        var touched = session.touch();
        
        // Then
        assertEquals(session.sessionId(), touched.sessionId());
        assertTrue(touched.lastActivity().isAfter(originalActivity));
    }
    
    @Test
    void testChatSessionWithContext() {
        // Given
        var session = ChatSession.create("test-session");
        var newContext = session.context().withLocation("Zurich");
        
        // When
        var updated = session.withContext(newContext);
        
        // Then
        assertEquals("Zurich", updated.context().currentLocation());
        assertEquals(session.sessionId(), updated.sessionId());
    }
    
    @Test
    void testMessageUser() {
        // When
        var message = Message.user("session-1", "Hello");
        
        // Then
        assertEquals("session-1", message.sessionId());
        assertEquals("Hello", message.content());
        assertEquals(MessageType.USER, message.type());
        assertNotNull(message.timestamp());
    }
    
    @Test
    void testMessageAssistant() {
        // When
        var message = Message.assistant("session-1", "Hi there!");
        
        // Then
        assertEquals("session-1", message.sessionId());
        assertEquals("Hi there!", message.content());
        assertEquals(MessageType.ASSISTANT, message.type());
        assertNotNull(message.timestamp());
    }
    
    @Test
    void testAiResponseCreation() {
        // Given
        var metadata = Map.<String, Object>of("latencyMs", 150L, "tokens", 50);
        
        // When
        var response = AiResponse.of("Test response", metadata);
        
        // Then
        assertEquals("Test response", response.content());
        assertEquals(150L, response.metadata().get("latencyMs"));
        assertEquals(50, response.metadata().get("tokens"));
    }
    
    @Test
    void testConversationContextEmpty() {
        // When
        var context = ConversationContext.EMPTY;
        
        // Then
        assertNull(context.currentLocation());
        assertTrue(context.recentLocations().isEmpty());
        assertEquals(WeatherPreferences.DEFAULT, context.preferences());
    }
    
    @Test
    void testConversationContextWithLocation() {
        // Given
        var context = ConversationContext.EMPTY;
        
        // When
        var updated = context.withLocation("Zurich");
        
        // Then
        assertEquals("Zurich", updated.currentLocation());
        assertTrue(updated.recentLocations().contains("Zurich"));
    }
    
    @Test
    void testConversationContextWithMultipleLocations() {
        // Given
        var context = ConversationContext.EMPTY;
        
        // When
        var updated = context
            .withLocation("Zurich")
            .withLocation("Bern")
            .withLocation("Geneva");
        
        // Then
        assertEquals("Geneva", updated.currentLocation());
        assertEquals(3, updated.recentLocations().size());
        assertEquals("Geneva", updated.recentLocations().get(0));
        assertEquals("Bern", updated.recentLocations().get(1));
        assertEquals("Zurich", updated.recentLocations().get(2));
    }
    
    @Test
    void testConversationContextMaxLocations() {
        // Given
        var context = ConversationContext.EMPTY;
        
        // When - Add 12 locations (max is 10)
        for (int i = 1; i <= 12; i++) {
            context = context.withLocation("Location" + i);
        }
        
        // Then
        assertEquals(10, context.recentLocations().size());
        assertEquals("Location12", context.recentLocations().get(0));
        assertEquals("Location3", context.recentLocations().get(9));
    }
    
    @Test
    void testConversationContextWithPreferences() {
        // Given
        var context = ConversationContext.EMPTY;
        var prefs = new WeatherPreferences("fahrenheit", "mph", "inches", "America/New_York");
        
        // When
        var updated = context.withPreferences(prefs);
        
        // Then
        assertEquals("fahrenheit", updated.preferences().temperatureUnit());
        assertEquals("mph", updated.preferences().windSpeedUnit());
    }
    
    @Test
    void testConversationContextWithContextData() {
        // Given
        var context = ConversationContext.EMPTY;
        
        // When
        var updated = context.withContext("key1", "value1");
        
        // Then
        assertEquals("value1", updated.context().get("key1"));
    }
    
    @Test
    void testWeatherPreferencesDefault() {
        // When
        var prefs = WeatherPreferences.DEFAULT;
        
        // Then
        assertEquals("celsius", prefs.temperatureUnit());
        assertEquals("kmh", prefs.windSpeedUnit());
        assertEquals("mm", prefs.precipitationUnit());
        assertEquals("UTC", prefs.timezone());
    }
    
    @Test
    void testWeatherPreferencesCustom() {
        // When
        var prefs = new WeatherPreferences("fahrenheit", "mph", "inches", "America/Los_Angeles");
        
        // Then
        assertEquals("fahrenheit", prefs.temperatureUnit());
        assertEquals("mph", prefs.windSpeedUnit());
        assertEquals("inches", prefs.precipitationUnit());
        assertEquals("America/Los_Angeles", prefs.timezone());
    }
}
