package org.example.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading and accessing properties from the application.properties file.
 */
public class PropertiesUtils {

    private static Properties properties = new Properties();

    static {
        try (InputStream input = PropertiesUtils.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find application.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
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
