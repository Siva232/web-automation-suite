package com.example.listeners;

import com.example.utils.ScenarioRetry;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Cucumber plugin that decides the in-run retries ({@link ScenarioRetry}) and writes the scenarios that
 * are still failing after their last attempt to a rerun file, e.g.
 * <pre>
 * src/test/resources/features/app1/login.feature:13:21
 * </pre>
 * testng-rerun.xml (FailedScenariosRunner) runs exactly those scenarios.
 * <p>
 * Replaces Cucumber's built-in "rerun:" plugin, which would also list scenarios that passed on a retry.
 * Usage in {@code @CucumberOptions}: {@code "com.example.listeners.FailedScenarioListener:<rerun file>"}.
 */
public class FailedScenarioListener implements ConcurrentEventListener {

    private final Path rerunFile;
    /** feature path -> lines of the scenarios that failed */
    private final Map<String, NavigableSet<Integer>> failed = new TreeMap<>();

    public FailedScenarioListener(File rerunFile) {
        this.rerunFile = rerunFile.toPath();
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestCaseFinished.class, this::onScenarioFinished);
        publisher.registerHandlerFor(TestRunFinished.class, event -> write());
    }

    private synchronized void onScenarioFinished(TestCaseFinished event) {
        TestCase testCase = event.getTestCase();
        Status status = event.getResult().getStatus();
        if (ScenarioRetry.willRetry(testCase, status)) {
            return;
        }
        String feature = featurePath(testCase.getUri());
        int line = testCase.getLocation().getLine();
        if (status.isOk()) {
            NavigableSet<Integer> lines = failed.get(feature);
            if (lines != null) {
                lines.remove(line);
            }
        } else {
            failed.computeIfAbsent(feature, f -> new TreeSet<>()).add(line);
        }
    }

    /** Always written, so an old list is never run again by mistake (empty = nothing failed). */
    private synchronized void write() {
        String content = failed.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(entry -> entry.getKey() + ":" + entry.getValue().stream()
                        .map(String::valueOf).collect(Collectors.joining(":")))
                .collect(Collectors.joining(System.lineSeparator()));
        try {
            if (rerunFile.getParent() != null) {
                Files.createDirectories(rerunFile.getParent());
            }
            Files.writeString(rerunFile, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("[Rerun] Could not write " + rerunFile + ": " + e.getMessage());
        }
    }

    /** Feature file relative to the project folder, as Cucumber expects it in a rerun file. */
    private static String featurePath(URI uri) {
        if (!"file".equals(uri.getScheme())) {
            return uri.toString();
        }
        Path feature = Paths.get(uri);
        Path projectFolder = Paths.get("").toAbsolutePath();
        Path path = feature.startsWith(projectFolder) ? projectFolder.relativize(feature) : feature;
        return path.toString().replace('\\', '/');
    }
}
