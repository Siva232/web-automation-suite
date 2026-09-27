package com.example.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Reads config.properties from the classpath.
 * <p>
 * Keys are resolved for the active environment and the application of the running scenario
 * (see {@link AppContext}). Lookup order for a key such as "base.url" with env=qa and app=app1:
 * <ol>
 *     <li>System property "app1.base.url"   (e.g. -Dapp1.base.url=...)</li>
 *     <li>System property "base.url"</li>
 *     <li>qa.app1.base.url</li>
 *     <li>qa.base.url</li>
 *     <li>app1.base.url</li>
 *     <li>base.url</li>
 * </ol>
 * The active environment comes from -Denv=... or the "env" key in config.properties.
 */
public final class ConfigReader {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in == null) {
                throw new IllegalStateException("config.properties not found on classpath");
            }
            PROPERTIES.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.properties", e);
        }
    }

    private ConfigReader() {
    }

    public static String get(String key) {
        String env = getEnv();
        String app = AppContext.get();

        List<String> systemKeys = new ArrayList<>();
        List<String> fileKeys = new ArrayList<>();
        if (app != null) {
            systemKeys.add(app + "." + key);
            fileKeys.add(env + "." + app + "." + key);
        }
        systemKeys.add(key);
        fileKeys.add(env + "." + key);
        if (app != null) {
            fileKeys.add(app + "." + key);
        }
        fileKeys.add(key);

        for (String candidate : systemKeys) {
            String value = systemProperty(candidate);
            if (value != null) {
                return value;
            }
        }
        for (String candidate : fileKeys) {
            String value = PROPERTIES.getProperty(candidate);
            if (value != null) {
                return value.trim();
            }
        }
        throw new IllegalArgumentException("Missing config key, tried: " + fileKeys);
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    /** Comma-separated value as a lower-case list, e.g. "app1,app2". */
    public static List<String> getList(String key) {
        return Arrays.stream(get(key).split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .toList();
    }

    /** Active environment in lower case, e.g. "qa" or "uat". */
    public static String getEnv() {
        String env = systemProperty("env");
        if (env == null) {
            env = PROPERTIES.getProperty("env", "qa");
        }
        return env.trim().toLowerCase();
    }

    private static String systemProperty(String key) {
        String value = System.getProperty(key);
        return value == null || value.isBlank() ? null : value.trim();
    }
}
