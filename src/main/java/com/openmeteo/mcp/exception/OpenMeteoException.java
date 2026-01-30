package com.openmeteo.mcp.exception;

/**
 * Custom exception for Open-Meteo API errors.
 * <p>
 * This exception is thrown when the Open-Meteo API returns an error response
 * or when there are issues communicating with the API.
 * </p>
 */
public class OpenMeteoException extends RuntimeException {

    private final int statusCode;

    /**
     * Constructs a new OpenMeteoException with the specified message and status code.
     *
     * @param message    the detail message
     * @param statusCode the HTTP status code
     */
    public OpenMeteoException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new OpenMeteoException with the specified message, cause, and status code.
     *
     * @param message    the detail message
     * @param cause      the cause
     * @param statusCode the HTTP status code
     */
    public OpenMeteoException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * Returns the HTTP status code associated with this exception.
     *
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return String.format("OpenMeteoException[statusCode=%d, message=%s]",
                statusCode, getMessage());
    }
}
