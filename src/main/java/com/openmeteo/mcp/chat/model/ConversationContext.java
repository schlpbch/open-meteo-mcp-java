package com.openmeteo.mcp.chat.model;

import java.util.List;
import java.util.Map;

/**
 * Context information for a conversation session.
 * Tracks location history and user preferences.
 * 
 * @param currentLocation Current location being discussed (e.g., "Zurich, Switzerland")
 * @param recentLocations Recently mentioned locations
 * @param preferences User weather preferences
 * @param context Additional context data
 * 
 * @since 1.2.0
 */
public record ConversationContext(
    String currentLocation,
    List<String> recentLocations,
    WeatherPreferences preferences,
    Map<String, Object> context
) {
    /**
     * Empty context
     */
    public static final ConversationContext EMPTY = new ConversationContext(
        null,
        List.of(),
        WeatherPreferences.DEFAULT,
        Map.of()
    );
    
    /**
     * Compact constructor with defaults
     */
    public ConversationContext {
        if (recentLocations == null) {
            recentLocations = List.of();
        }
        if (preferences == null) {
            preferences = WeatherPreferences.DEFAULT;
        }
        if (context == null) {
            context = Map.of();
        }
    }
    
    /**
     * Update current location and add to recent locations
     */
    public ConversationContext withLocation(String location) {
        var updated = new java.util.ArrayList<>(recentLocations);
        if (location != null && !location.isBlank() && !updated.contains(location)) {
            updated.add(0, location);
            // Keep only last 10 locations
            if (updated.size() > 10) {
                updated = new java.util.ArrayList<>(updated.subList(0, 10));
            }
        }
        return new ConversationContext(location, List.copyOf(updated), preferences, context);
    }
    
    /**
     * Update preferences
     */
    public ConversationContext withPreferences(WeatherPreferences newPreferences) {
        return new ConversationContext(currentLocation, recentLocations, newPreferences, context);
    }
    
    /**
     * Add context data
     */
    public ConversationContext withContext(String key, Object value) {
        var updated = new java.util.HashMap<>(context);
        updated.put(key, value);
        return new ConversationContext(currentLocation, recentLocations, preferences, Map.copyOf(updated));
    }
}
