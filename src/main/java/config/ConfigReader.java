package config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

/**
 * Central place for test configuration.
 *
 * Lookup order for a key such as "base.url":
 *   1. JVM system property      -Dbase.url=...
 *   2. Environment variable     BASE_URL=...
 *   3. config.properties on the classpath
 *
 * This lets CI override any value without editing files, and keeps secrets out of the repo.
 */
public final class ConfigReader {

    private static final Properties FILE_PROPERTIES = load();

    private ConfigReader() {
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read config.properties", e);
        }
        return properties;
    }

    public static String get(String key) {
        String value = System.getProperty(key);
        if (isBlank(value)) {
            value = System.getenv(toEnvName(key));
        }
        if (isBlank(value)) {
            value = FILE_PROPERTIES.getProperty(key);
        }
        return isBlank(value) ? null : value.trim();
    }

    /** Fails fast with a clear message instead of a NullPointerException deep inside a test. */
    public static String require(String key) {
        String value = get(key);
        if (value == null) {
            throw new IllegalStateException("Missing required setting '" + key + "'. Set the environment variable "
                    + toEnvName(key) + " or pass -D" + key + "=<value>.");
        }
        return value;
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public static int getInt(String key) {
        return Integer.parseInt(require(key));
    }

    /** "base.url" -> "BASE_URL" */
    static String toEnvName(String key) {
        return key.toUpperCase().replace('.', '_');
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
