package com.openmeteo.mcp.chat.exception;

/**
 * Exception thrown when an LLM provider fails.
 * 
 * @since 2.0.0
 */
public class LlmProviderException extends ChatException {
    
    private final String provider;
    
    public LlmProviderException(String provider, String message) {
        super("LLM Provider '" + provider + "' failed: " + message);
        this.provider = provider;
    }
    
    public LlmProviderException(String provider, String message, Throwable cause) {
        super("LLM Provider '" + provider + "' failed: " + message, cause);
        this.provider = provider;
    }
    
    public String getProvider() {
        return provider;
    }
}
