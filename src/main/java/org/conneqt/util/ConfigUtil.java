package org.conneqt.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigUtil.class.getClassLoader().getResourceAsStream("custom-config.properties")) {
            if (input == null) {
                throw new RuntimeException("custom-config.properties file not found in classpath");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Error loading custom-config.properties", ex);
        }
    }

    public static String getSecretKey() {
        return properties.getProperty("secret.key");
    }

    public static String getRedirectUrl() {
        return properties.getProperty("redirect.url");
    }

}
