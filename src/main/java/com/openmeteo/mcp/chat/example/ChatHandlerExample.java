package com.openmeteo.mcp.chat.example;

import com.openmeteo.mcp.chat.model.ConversationContext;
import com.openmeteo.mcp.chat.model.WeatherPreferences;
import com.openmeteo.mcp.chat.service.ChatHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Example demonstrating ChatHandler integration.
 * Run with: mvn spring-boot:run -Dopenmeteo.chat.example.enabled=true
 * 
 * @since 2.0.0
 */
@Component
@ConditionalOnProperty(name = "openmeteo.chat.example.enabled", havingValue = "true")
public class ChatHandlerExample implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(ChatHandlerExample.class);
    
    private final ChatHandler chatHandler;
    
    public ChatHandlerExample(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
    }
    
    @Override
    public void run(String... args) throws Exception {
        log.info("=== ChatHandler Integration Example ===");
        
        // Example 1: Simple weather query
        example1_SimpleWeatherQuery();
        
        // Example 2: Multi-turn conversation with context
        example2_MultiTurnConversation();
        
        // Example 3: Location comparison
        example3_LocationComparison();
        
        // Example 4: User preferences
        example4_UserPreferences();
        
        log.info("=== Examples Complete ===");
    }
    
    /**
     * Example 1: Simple weather query
     */
    private void example1_SimpleWeatherQuery() throws Exception {
        log.info("\n--- Example 1: Simple Weather Query ---");
        
        var sessionId = "example-1";
        var response = chatHandler.chat(sessionId, "What's the weather in Zurich?").get();
        
        log.info("User: What's the weather in Zurich?");
        log.info("Assistant: {}", response.content());
        log.info("Metadata: {}", response.metadata());
    }
    
    /**
     * Example 2: Multi-turn conversation with context
     */
    private void example2_MultiTurnConversation() throws Exception {
        log.info("\n--- Example 2: Multi-Turn Conversation ---");
        
        var sessionId = "example-2";
        
        // First message
        var response1 = chatHandler.chat(sessionId, "What's the weather in Bern?").get();
        log.info("User: What's the weather in Bern?");
        log.info("Assistant: {}", response1.content());
        
        // Follow-up using context
        var response2 = chatHandler.chat(sessionId, "How about tomorrow?").get();
        log.info("User: How about tomorrow?");
        log.info("Assistant: {}", response2.content());
        
        // Another follow-up
        var response3 = chatHandler.chat(sessionId, "Should I bring an umbrella?").get();
        log.info("User: Should I bring an umbrella?");
        log.info("Assistant: {}", response3.content());
    }
    
    /**
     * Example 3: Location comparison
     */
    private void example3_LocationComparison() throws Exception {
        log.info("\n--- Example 3: Location Comparison ---");
        
        var sessionId = "example-3";
        var response = chatHandler.chat(sessionId, 
            "Compare the weather between Zurich, Geneva, and Lugano").get();
        
        log.info("User: Compare the weather between Zurich, Geneva, and Lugano");
        log.info("Assistant: {}", response.content());
    }
    
    /**
     * Example 4: User preferences
     * Note: Preferences would be set via session management API in production
     */
    private void example4_UserPreferences() throws Exception {
        log.info("\n--- Example 4: User Preferences ---");
        
        var sessionId = "example-4";
        
        // In production, preferences would be set via a separate API
        // For this example, we just demonstrate the query
        var response = chatHandler.chat(sessionId, 
            "What's the temperature in Zurich? Please use Fahrenheit.").get();
        
        log.info("User: What's the temperature in Zurich? Please use Fahrenheit.");
        log.info("Assistant: {}", response.content());
        log.info("Note: User can request specific units in their message");
    }
}
