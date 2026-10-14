package com.example.utils;

import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Retry of failed scenarios within the same run ("rerun.count" in config.properties).
 * <p>
 * The decision is made once per attempt when Cucumber reports the scenario's result, so the reports
 * (Extent, rerun.txt) and TestNG's RetryAnalyzer, which asks right after, always agree.
 * A scenario is identified by its feature file and line, so equal names in different features stay apart.
 */
public final class ScenarioRetry {

    /** Retries already granted per scenario. */
    private static final Map<String, Integer> RETRIES = new ConcurrentHashMap<>();
    /** One decision per attempt (Cucumber test case id), however many listeners ask. */
    private static final Map<UUID, Boolean> DECISIONS = new ConcurrentHashMap<>();
    /** Scenarios granted a retry that TestNG has not started yet. */
    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();

    private ScenarioRetry() {
    }

    /** e.g. file:///C:/.../features/app1/login.feature:13 */
    public static String key(URI featureUri, int line) {
        return featureUri + ":" + line;
    }

    public static int maxRetries() {
        return Math.max(0, ConfigReader.getInt("rerun.count"));
    }

    /** 1 for the first run of the scenario, 2 for the first retry, ... */
    public static int attempt(String key) {
        return RETRIES.getOrDefault(key, 0) + 1;
    }

    /**
     * True when this attempt failed and the scenario still has retries left. Only real failures
     * (FAILED, AMBIGUOUS) are retried - an undefined or pending step fails the same way every time.
     */
    public static boolean willRetry(TestCase testCase, Status status) {
        return DECISIONS.computeIfAbsent(testCase.getId(), id -> {
            String key = key(testCase.getUri(), testCase.getLocation().getLine());
            boolean failed = status == Status.FAILED || status == Status.AMBIGUOUS;
            boolean retry = failed && RETRIES.getOrDefault(key, 0) < maxRetries();
            if (retry) {
                RETRIES.merge(key, 1, Integer::sum);
                PENDING.add(key);
            }
            return retry;
        });
    }

    /** Called by TestNG's RetryAnalyzer: true (once) when the scenario was granted a retry. */
    public static boolean consumeRetry(String key) {
        return PENDING.remove(key);
    }
}
