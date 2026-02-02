package com.openmeteo.mcp.chat.rag;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Weather knowledge documents for RAG.
 * Contains domain knowledge about weather interpretation and best practices.
 * 
 * Phase 4.2: Static documents.
 * Future (Phase 4.3): Will be indexed in vector store for semantic search.
 * 
 * @since 1.2.0
 */
@Component
public class WeatherKnowledgeDocuments {
    
    /**
     * Get all weather knowledge documents.
     * Each document contains domain knowledge that can enhance AI responses.
     */
    public List<WeatherDocument> getAllDocuments() {
        return List.of(
            new WeatherDocument(
                "temperature_interpretation",
                "Temperature Interpretation",
                """
                Temperature interpretation guidelines:
                - Below 0°C (32°F): Freezing, risk of ice
                - 0-10°C (32-50°F): Cold, warm clothing recommended
                - 10-20°C (50-68°F): Cool to mild, light jacket suggested
                - 20-25°C (68-77°F): Comfortable, ideal outdoor conditions
                - 25-30°C (77-86°F): Warm, stay hydrated
                - 30-35°C (86-95°F): Hot, limit outdoor activities
                - Above 35°C (95°F): Very hot, heat warning
                
                Apparent temperature (feels like) accounts for wind chill and humidity.
                """
            ),
            new WeatherDocument(
                "precipitation_guide",
                "Precipitation Guide",
                """
                Precipitation intensity:
                - 0-0.5 mm/h: Light rain/drizzle
                - 0.5-4 mm/h: Moderate rain
                - 4-16 mm/h: Heavy rain
                - 16-50 mm/h: Very heavy rain
                - Above 50 mm/h: Extreme rainfall
                
                Snow accumulation:
                - 1 cm = approximately 1 mm of water equivalent
                - 5-10 cm: Light snow
                - 10-25 cm: Moderate snow
                - Above 25 cm: Heavy snow
                
                Always check precipitation probability alongside amount.
                """
            ),
            new WeatherDocument(
                "wind_conditions",
                "Wind Conditions",
                """
                Wind speed interpretation (km/h):
                - 0-10: Calm to light breeze
                - 10-20: Gentle breeze
                - 20-30: Moderate breeze
                - 30-40: Fresh breeze
                - 40-50: Strong breeze
                - 50-60: Near gale
                - 60-75: Gale
                - 75-90: Strong gale
                - Above 90: Storm conditions
                
                Wind gusts can be significantly higher than sustained wind.
                Consider wind chill effect on apparent temperature.
                """
            ),
            new WeatherDocument(
                "air_quality_index",
                "Air Quality Index (AQI)",
                """
                Air Quality Index categories:
                - 0-50: Good - Air quality is satisfactory
                - 51-100: Moderate - Acceptable for most people
                - 101-150: Unhealthy for sensitive groups
                - 151-200: Unhealthy - Everyone may experience effects
                - 201-300: Very unhealthy - Health alert
                - 301+: Hazardous - Health warnings
                
                Key pollutants:
                - PM2.5: Fine particles, most health-concerning
                - PM10: Coarse particles
                - NO2: Nitrogen dioxide from traffic
                - O3: Ozone, higher in summer
                - SO2: Sulfur dioxide from industry
                """
            ),
            new WeatherDocument(
                "ski_conditions",
                "Ski and Snow Conditions",
                """
                Optimal skiing conditions:
                - Fresh snow: 10-30 cm ideal
                - Temperature: -5 to -15°C best for powder
                - Wind: Below 30 km/h for comfortable skiing
                
                Snow quality indicators:
                - Powder: Fresh, dry snow (best)
                - Packed: Groomed, firm snow (good)
                - Wet: Above 0°C, heavy snow
                - Ice: Frozen, hard surface
                
                Check avalanche warnings in backcountry areas.
                """
            ),
            new WeatherDocument(
                "marine_conditions",
                "Marine and Wave Conditions",
                """
                Wave height interpretation:
                - 0-0.5m: Calm, smooth sea
                - 0.5-1.25m: Slight, small wavelets
                - 1.25-2.5m: Moderate, larger waves
                - 2.5-4m: Rough, considerable waves
                - 4-6m: Very rough, high waves
                - Above 6m: Dangerous conditions
                
                Swell vs. wind waves:
                - Swell: Long-period waves from distant storms
                - Wind waves: Local waves from current wind
                
                Wave period affects surfing and boating safety.
                """
            )
        );
    }
    
    /**
     * Get document by ID
     */
    public WeatherDocument getDocument(String id) {
        return getAllDocuments().stream()
            .filter(doc -> doc.id().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Search documents by keyword (simple implementation)
     * Future: Will use vector similarity search
     */
    public List<WeatherDocument> searchDocuments(String query) {
        var lowerQuery = query.toLowerCase();
        return getAllDocuments().stream()
            .filter(doc -> 
                doc.title().toLowerCase().contains(lowerQuery) ||
                doc.content().toLowerCase().contains(lowerQuery)
            )
            .toList();
    }
    
    /**
     * Weather knowledge document record
     */
    public record WeatherDocument(
        String id,
        String title,
        String content
    ) {}
}
