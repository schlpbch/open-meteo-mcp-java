package com.openmeteo.mcp.resource.util;

import com.openmeteo.mcp.exception.ResourceLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for loading resource files from the classpath.
 * <p>
 * Provides methods to load JSON data files and other resources
 * as UTF-8 encoded strings.
 * </p>
 */
@Component
public class ResourceLoader {

    private static final Logger log = LoggerFactory.getLogger(ResourceLoader.class);

    /**
     * Load a resource file from classpath as UTF-8 string.
     *
     * @param resourcePath Path relative to classpath (e.g., "data/weather-codes.json")
     * @return File contents as string
     * @throws ResourceLoadException if file not found or cannot be read
     */
    public String loadResource(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);

            if (!resource.exists()) {
                throw new ResourceLoadException(
                        "Resource not found: " + resourcePath
                );
            }

            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

        } catch (IOException e) {
            log.error("Failed to load resource: {}", resourcePath, e);
            throw new ResourceLoadException(
                    "Failed to load resource: " + resourcePath, e
            );
        }
    }

    /**
     * Check if a resource exists on the classpath.
     *
     * @param resourcePath Path relative to classpath
     * @return true if the resource exists, false otherwise
     */
    public boolean resourceExists(String resourcePath) {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        return resource.exists();
    }
}
