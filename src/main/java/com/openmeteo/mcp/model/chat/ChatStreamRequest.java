package com.openmeteo.mcp.model.chat;

/**
 * Request model for chat streaming.
 * 
 * @param sessionId Session identifier for conversation continuity
 * @param message User's message to the AI
 * @param includeWeather Whether to include weather context in the response
 * @param latitude Optional latitude for weather context
 * @param longitude Optional longitude for weather context
 * @param temperature Optional temperature parameter for AI response (0.0-1.0)
 * @param maxTokens Optional maximum tokens for response
 * 
 * @since 2.1.0
 */
public record ChatStreamRequest(
    String sessionId,
    String message,
    Boolean includeWeather,
    Double latitude,
    Double longitude,
    Double temperature,
    Integer maxTokens
) {
    
    /**
     * Create a simple chat request with just message.
     */
    public static ChatStreamRequest simple(String sessionId, String message) {
        return new ChatStreamRequest(sessionId, message, false, null, null, null, null);
    }
    
    /**
     * Create a chat request with weather context.
     */
    public static ChatStreamRequest withWeather(
        String sessionId, 
        String message, 
        double latitude, 
        double longitude
    ) {
        return new ChatStreamRequest(sessionId, message, true, latitude, longitude, null, null);
    }
    
    /**
     * Create a chat request with custom AI parameters.
     */
    public static ChatStreamRequest withOptions(
        String sessionId,
        String message,
        double temperature,
        int maxTokens
    ) {
        return new ChatStreamRequest(sessionId, message, false, null, null, temperature, maxTokens);
    }
    
    /**
     * Check if weather context should be included.
     */
    public boolean shouldIncludeWeather() {
        return Boolean.TRUE.equals(includeWeather) && latitude != null && longitude != null;
    }
    
    /**
     * Get temperature with default value.
     */
    public double getTemperature() {
        return temperature != null ? temperature : 0.7;
    }
    
    /**
     * Get max tokens with default value.
     */
    public int getMaxTokens() {
        return maxTokens != null ? maxTokens : 2000;
    }
}
