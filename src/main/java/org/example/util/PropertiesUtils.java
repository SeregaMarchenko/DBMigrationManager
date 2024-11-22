package org.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for loading and accessing properties from the application.properties file.
 */
@Slf4j
public class PropertiesUtils {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                log.error("Sorry, unable to find application.properties");
                throw new RuntimeException("application.properties file not found");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            log.error("Error loading properties file", ex);
            throw new RuntimeException("Critical error while loading properties file", ex);
        }
    }

    /**
     * Retrieves the property value associated with the specified key.
     *
     * @param key the property key
     * @return the property value, or null if the key is not found
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
