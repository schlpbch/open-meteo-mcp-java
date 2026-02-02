package com.openmeteo.mcp.chat.model;

/**
 * Type of message in a conversation.
 * 
 * @since 1.2.0
 */
public enum MessageType {
    /**
     * Message from the user
     */
    USER,
    
    /**
     * Message from the AI assistant
     */
    ASSISTANT,
    
    /**
     * System message (e.g., context, instructions)
     */
    SYSTEM,
    
    /**
     * Function call result
     */
    FUNCTION
}
