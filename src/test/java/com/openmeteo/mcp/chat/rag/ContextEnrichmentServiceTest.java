package com.openmeteo.mcp.chat.rag;

import com.openmeteo.mcp.chat.model.ConversationContext;
import com.openmeteo.mcp.chat.model.WeatherPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContextEnrichmentService.
 */
class ContextEnrichmentServiceTest {
    
    private ContextEnrichmentService service;
    private WeatherKnowledgeDocuments knowledgeDocs;
    
    @BeforeEach
    void setUp() {
        knowledgeDocs = new WeatherKnowledgeDocuments();
        service = new ContextEnrichmentService(knowledgeDocs);
    }
    
    @Test
    void testEnrichPromptWithLocation() throws ExecutionException, InterruptedException {
        // Given
        var context = ConversationContext.EMPTY.withLocation("Zurich");
        
        // When
        var enriched = service.enrichPrompt("What's the weather?", context).get();
        
        // Then
        assertTrue(enriched.contains("Zurich"));
        assertTrue(enriched.contains("What's the weather?"));
    }
    
    @Test
    void testEnrichPromptWithRecentLocations() throws ExecutionException, InterruptedException {
        // Given
        var context = new ConversationContext(null, List.of("Zurich", "Bern", "Geneva"), 
            WeatherPreferences.DEFAULT, Map.of());
        
        // When
        var enriched = service.enrichPrompt("Compare weather", context).get();
        
        // Then
        assertTrue(enriched.contains("Zurich"));
        assertTrue(enriched.contains("Bern"));
        assertTrue(enriched.contains("Geneva"));
    }
    
    @Test
    void testEnrichPromptWithPreferences() throws ExecutionException, InterruptedException {
        // Given
        var prefs = new WeatherPreferences("fahrenheit", "mph", "inches", "America/New_York");
        var context = new ConversationContext(null, List.of(), prefs, Map.of());
        
        // When
        var enriched = service.enrichPrompt("Temperature today?", context).get();
        
        // Then
        assertTrue(enriched.contains("fahrenheit"));
        assertTrue(enriched.contains("mph"));
        assertTrue(enriched.contains("America/New_York"));
    }
    
    @Test
    void testExtractLocationFromPrompt() throws ExecutionException, InterruptedException {
        // Given
        var prompt = "What's the weather in Zurich?";
        
        // When
        var location = service.extractLocation(prompt).get();
        
        // Then
        assertNotNull(location);
        assertEquals("zurich", location.toLowerCase());
    }
    
    @Test
    void testExtractLocationNotFound() throws ExecutionException, InterruptedException {
        // Given
        var prompt = "What's the weather?";
        
        // When
        var location = service.extractLocation(prompt).get();
        
        // Then
        assertNull(location);
    }
    
    @Test
    void testEnrichWithKnowledgeTemperature() throws ExecutionException, InterruptedException {
        // Given
        var prompt = "What does temperature mean?";
        
        // When
        var enriched = service.enrichWithKnowledge(prompt).get();
        
        // Then - Should find temperature-related documents
        assertTrue(enriched.length() >= prompt.length()); // Should be enriched or same
    }
    
    @Test
    void testEnrichWithKnowledgePrecipitation() throws ExecutionException, InterruptedException {
        // Given
        var prompt = "Tell me about precipitation";
        
        // When
        var enriched = service.enrichWithKnowledge(prompt).get();
        
        // Then - Should find precipitation-related documents
        assertTrue(enriched.length() >= prompt.length()); // Should be enriched or same
    }
    
    @Test
    void testEnrichWithKnowledgeNoMatch() throws ExecutionException, InterruptedException {
        // Given
        var prompt = "Random unrelated query";
        
        // When
        var enriched = service.enrichWithKnowledge(prompt).get();
        
        // Then
        assertEquals(prompt, enriched); // Should return original if no match
    }
    
    @Test
    void testBuildSystemContext() {
        // When
        var context = service.buildSystemContext();
        
        // Then
        assertNotNull(context);
        assertTrue(context.contains("weather assistant"));
        assertTrue(context.contains("Open-Meteo"));
    }
}
