package com.openmeteo.mcp.chat.rag;

import com.openmeteo.mcp.chat.model.ConversationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for enriching prompts with contextual weather information.
 * Prepares prompts for better AI responses by adding relevant context.
 * 
 * Phase 4.2: Basic implementation with location context.
 * Future: Will integrate with vector store for RAG.
 * 
 * @since 2.0.0
 */
@Service
@ConditionalOnProperty(name = "openmeteo.chat.enabled", havingValue = "true")
public class ContextEnrichmentService {
    
    private static final Logger log = LoggerFactory.getLogger(ContextEnrichmentService.class);
    private final WeatherKnowledgeDocuments knowledgeDocs;
    
    public ContextEnrichmentService(WeatherKnowledgeDocuments knowledgeDocs) {
        this.knowledgeDocs = knowledgeDocs;
    }
    
    /**
     * Enrich a user prompt with conversation context.
     * 
     * @param userPrompt Original user prompt
     * @param context Conversation context
     * @return Enriched prompt
     */
    public CompletableFuture<String> enrichPrompt(String userPrompt, ConversationContext context) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Enriching prompt with context");
            
            var enriched = new StringBuilder();
            
            // Add location context if available
            if (context.currentLocation() != null && !context.currentLocation().isBlank()) {
                enriched.append("Current location context: ").append(context.currentLocation()).append("\n");
            }
            
            // Add recent locations if available
            if (!context.recentLocations().isEmpty()) {
                enriched.append("Recently discussed locations: ");
                enriched.append(String.join(", ", context.recentLocations().subList(
                    0, Math.min(3, context.recentLocations().size())
                )));
                enriched.append("\n");
            }
            
            // Add user preferences
            var prefs = context.preferences();
            enriched.append("User preferences: ");
            enriched.append("temperature=").append(prefs.temperatureUnit()).append(", ");
            enriched.append("wind=").append(prefs.windSpeedUnit()).append(", ");
            enriched.append("precipitation=").append(prefs.precipitationUnit()).append(", ");
            enriched.append("timezone=").append(prefs.timezone());
            enriched.append("\n\n");
            
            // Add the original prompt
            enriched.append("User query: ").append(userPrompt);
            
            log.debug("Prompt enriched with {} chars of context", enriched.length() - userPrompt.length());
            return enriched.toString();
        });
    }
    
    /**
     * Extract location mentions from a prompt.
     * Simple implementation - can be enhanced with NER in the future.
     * 
     * @param prompt User prompt
     * @return Extracted location (if found)
     */
    public CompletableFuture<String> extractLocation(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            // Simple keyword-based extraction
            // Future: Use NER or LLM-based extraction
            var lowerPrompt = prompt.toLowerCase();
            
            // Common patterns
            if (lowerPrompt.contains("in ")) {
                var parts = lowerPrompt.split("in ");
                if (parts.length > 1) {
                    var location = parts[1].split("[,\\.\\?\\s]")[0].trim();
                    if (!location.isBlank() && location.length() > 2) {
                        log.debug("Extracted location: {}", location);
                        return location;
                    }
                }
            }
            
            return null;
        });
    }
    
    /**
     * Build system context for weather assistant.
     * This provides the AI with domain knowledge about weather data.
     * 
     * @return System context
     */
    public String buildSystemContext() {
        return """
            You are a weather assistant with access to comprehensive weather data from Open-Meteo.
            
            Available data sources:
            - Current weather and 7-day forecasts (temperature, precipitation, wind, etc.)
            - Historical weather data from 1940 to present
            - Snow conditions for ski resorts worldwide
            - Air quality data (PM2.5, PM10, NO2, O3, etc.)
            - Marine conditions (wave height, swell, period)
            - Weather alerts and warnings
            - Comfort indices (apparent temperature, heat index)
            - Astronomy data (sunrise, sunset, moon phases)
            
            Best practices:
            - Always specify units clearly (use user's preferred units)
            - Include timezone information for time-based data
            - Provide context for weather values (e.g., "moderate wind" for 20 km/h)
            - Suggest appropriate activities based on conditions
            - Warn about extreme weather conditions
            
            When users ask about weather, use the available tools to fetch accurate data.
            """;
    }
    
    /**
     * Enrich prompt with relevant weather knowledge documents.
     * Simple keyword matching for now - will use vector similarity in Phase 4.3.
     * 
     * @param userPrompt User's prompt
     * @return Enriched prompt with relevant knowledge
     */
    public CompletableFuture<String> enrichWithKnowledge(String userPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            // Search for relevant documents
            var relevantDocs = knowledgeDocs.searchDocuments(userPrompt);
            
            if (relevantDocs.isEmpty()) {
                return userPrompt;
            }
            
            var enriched = new StringBuilder();
            enriched.append("Relevant weather knowledge:\\n\\n");
            
            // Add up to 2 most relevant documents
            for (var doc : relevantDocs.subList(0, Math.min(2, relevantDocs.size()))) {
                enriched.append("### ").append(doc.title()).append("\\n");
                enriched.append(doc.content()).append("\\n\\n");
            }
            
            enriched.append("User query: ").append(userPrompt);
            
            log.debug("Enriched prompt with {} knowledge documents", relevantDocs.size());
            return enriched.toString();
        });
    }
}
