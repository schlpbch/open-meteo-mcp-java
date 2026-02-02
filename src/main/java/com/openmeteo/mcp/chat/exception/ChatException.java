package com.openmeteo.mcp.chat.exception;

/**
 * Base exception for chat-related errors.
 * 
 * @since 1.2.0
 */
public class ChatException extends RuntimeException {
    
    public ChatException(String message) {
        super(message);
    }
    
    public ChatException(String message, Throwable cause) {
        super(message, cause);
    }
}
