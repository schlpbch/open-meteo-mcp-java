package com.openmeteo.mcp.exception;

/**
 * Exception thrown when a resource file cannot be loaded from the classpath.
 * <p>
 * This exception is typically thrown when:
 * - A requested resource file does not exist
 * - A resource file cannot be read due to IO errors
 * - A resource path is invalid
 * </p>
 */
public class ResourceLoadException extends RuntimeException {

    /**
     * Constructs a new ResourceLoadException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceLoadException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceLoadException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ResourceLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
