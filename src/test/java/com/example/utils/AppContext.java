package com.example.utils;

/**
 * Application (e.g. "app1", "app2") of the scenario running on the current thread.
 * Set by AppHooks from the scenario's tags; read by ConfigReader to pick app-specific settings.
 */
public final class AppContext {

    private static final ThreadLocal<String> APP = new ThreadLocal<>();

    private AppContext() {
    }

    public static void set(String app) {
        APP.set(app);
    }

    /** Current application, or null when the scenario is not tagged with an application. */
    public static String get() {
        return APP.get();
    }

    public static void clear() {
        APP.remove();
    }
}
