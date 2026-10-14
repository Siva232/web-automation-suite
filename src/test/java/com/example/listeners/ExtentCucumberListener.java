package com.example.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.GherkinKeyword;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.gherkin.model.Feature;
import com.aventstack.extentreports.gherkin.model.Scenario;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.ViewName;
import com.example.utils.ConfigReader;
import com.example.utils.RunInfo;
import com.example.utils.ScenarioRetry;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.DataTableArgument;
import io.cucumber.plugin.event.DocStringArgument;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.HookTestStep;
import io.cucumber.plugin.event.Node;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceParsed;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.WriteEvent;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Cucumber plugin that writes an ExtentReports Spark report of the run:
 * Feature -> Scenario -> Given/When/Then steps, with tags as categories, the browser as device,
 * data tables, scenario.log() text, screenshots (embedded) and links to the scenario videos.
 * <p>
 * Saved as Test Results/TestRun_&lt;run&gt;/ExtentReport_&lt;run&gt;.html, next to the Videos folder.
 * Look and feel come from extent-config.xml; see the "Extent report" section of config.properties.
 * <p>
 * Each attempt of a scenario is recorded first and only added to the report when it is the final one,
 * so a scenario that passes on a retry ("rerun.count") shows as passed, tagged "retried", with the
 * failures of the earlier attempts listed on its first step.
 * <p>
 * The Spark BDD view only shows logs on step rows, so output of Before hooks is put on the first step
 * and output of After hooks on the last step.
 * <p>
 * Registered in the runners' {@code @CucumberOptions(plugin = ...)}. Scenarios run in parallel: each attempt
 * is recorded by its own thread and the shared Extent model is only touched under the lock.
 */
public class ExtentCucumberListener implements ConcurrentEventListener {

    /** Recorded attempt of a scenario, replayed into the report when it is final. */
    private static final class Attempt {
        private final Instant start;
        private final int number;
        private final List<Consumer<ScenarioNodes>> actions = new ArrayList<>();

        private Attempt(Instant start, int number) {
            this.start = start;
            this.number = number;
        }
    }

    /** Report nodes of one scenario while its final attempt is replayed. */
    private static final class ScenarioNodes {
        private final ExtentTest scenario;
        private ExtentTest step;
        /** Before-hook output waiting for the first step to exist. */
        private final List<Consumer<ExtentTest>> pending = new ArrayList<>();
        /** Timings to apply after the replay: Extent sets them to "now" while nodes are built. */
        private final Map<ExtentTest, Instant[]> times = new HashMap<>();

        private ScenarioNodes(ExtentTest scenario) {
            this.scenario = scenario;
        }

        /** Runs the log action on the current step, or keeps it for the first step. */
        private void log(Consumer<ExtentTest> action) {
            if (step != null) {
                action.accept(step);
            } else {
                pending.add(action);
            }
        }
    }

    private final Map<URI, String> featureNames = new ConcurrentHashMap<>();
    private final Map<UUID, Attempt> attempts = new ConcurrentHashMap<>();
    /** Scenario key -> why its earlier attempts failed. */
    private final Map<String, List<String>> retryHistory = new ConcurrentHashMap<>();
    private final Map<URI, ExtentTest> features = new HashMap<>();

    private ExtentReports extent;

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        if (!ConfigReader.getBoolean("extent.report.enabled")) {
            return;
        }
        publisher.registerHandlerFor(TestRunStarted.class, event -> startReport());
        publisher.registerHandlerFor(TestSourceParsed.class, this::onSourceParsed);
        publisher.registerHandlerFor(TestCaseStarted.class, this::onScenarioStarted);
        publisher.registerHandlerFor(TestStepStarted.class, this::onStepStarted);
        publisher.registerHandlerFor(TestStepFinished.class, this::onStepFinished);
        publisher.registerHandlerFor(WriteEvent.class, this::onWrite);
        publisher.registerHandlerFor(EmbedEvent.class, this::onEmbed);
        publisher.registerHandlerFor(TestCaseFinished.class, this::onScenarioFinished);
        publisher.registerHandlerFor(TestRunFinished.class, event -> finishReport());
    }

    private synchronized void startReport() {
        Path file = RunInfo.runFolder().resolve("ExtentReport_" + RunInfo.runId() + ".html");
        ExtentSparkReporter spark = new ExtentSparkReporter(file.toFile());

        Path configFile = Paths.get(ConfigReader.get("extent.config.file"));
        if (Files.exists(configFile)) {
            try {
                spark.loadXMLConfig(configFile.toFile());
            } catch (IOException e) {
                System.out.println("[Extent] Could not read " + configFile + " - using defaults: " + e.getMessage());
            }
        } else {
            System.out.println("[Extent] " + configFile.toAbsolutePath() + " not found - using defaults");
        }
        spark.viewConfigurer().viewOrder().as(viewOrder()).apply();

        extent = new ExtentReports();
        // timings come from the Cucumber events, not from when the nodes are built
        extent.setReportUsesManualConfiguration(true);
        extent.attachReporter(spark);
        boolean parallel = ConfigReader.getBoolean("parallel.execution");
        extent.setSystemInfo("Environment", ConfigReader.getEnv().toUpperCase());
        extent.setSystemInfo("Applications", String.join(", ", ConfigReader.getList("applications")).toUpperCase());
        extent.setSystemInfo("Browser", ConfigReader.get("browser") + (ConfigReader.getBoolean("headless") ? " (headless)" : ""));
        extent.setSystemInfo("Threads", String.valueOf(parallel ? Math.max(1, ConfigReader.getInt("thread.count")) : 1));
        extent.setSystemInfo("Retries", String.valueOf(ScenarioRetry.maxRetries()));
        extent.setSystemInfo("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
        extent.setSystemInfo("User", System.getProperty("user.name"));
    }

    /** "extent.view.order" -> tabs in the report's side menu, e.g. dashboard,test,category,exception */
    private static List<ViewName> viewOrder() {
        return ConfigReader.getList("extent.view.order").stream()
                .map(name -> {
                    try {
                        return ViewName.valueOf(name.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Unknown extent.view.order entry '" + name
                                + "', use: dashboard, test, category, device, author, exception, log", e);
                    }
                })
                .toList();
    }

    private void onSourceParsed(TestSourceParsed event) {
        event.getNodes().stream()
                .filter(Node.Feature.class::isInstance)
                .findFirst()
                .flatMap(Node::getName)
                .ifPresent(name -> featureNames.put(event.getUri(), name));
    }

    private void onScenarioStarted(TestCaseStarted event) {
        int number = ScenarioRetry.attempt(key(event.getTestCase()));
        attempts.put(event.getTestCase().getId(), new Attempt(event.getInstant(), number));
    }

    private void onStepStarted(TestStepStarted event) {
        if (!(event.getTestStep() instanceof PickleStepTestStep step)) {
            return;
        }
        Instant start = event.getInstant();
        record(event.getTestCase(), nodes -> {
            String keyword = step.getStep().getKeyword().trim();
            try {
                nodes.step = nodes.scenario.createNode(new GherkinKeyword(keyword), step.getStep().getText());
            } catch (ClassNotFoundException | RuntimeException e) {
                // keyword Extent does not know (e.g. another Gherkin language) - plain node instead
                nodes.step = nodes.scenario.createNode(keyword + " " + step.getStep().getText());
            }
            nodes.times.put(nodes.step, new Instant[]{start, start});
            nodes.pending.forEach(action -> action.accept(nodes.step));
            nodes.pending.clear();
            logArgument(nodes.step, step.getStep().getArgument());
        });
    }

    /** Shows a step's data table or doc string under the step. */
    private static void logArgument(ExtentTest node, StepArgument argument) {
        if (argument instanceof DataTableArgument table) {
            String[][] cells = table.cells().stream()
                    .map(row -> row.stream().map(ExtentCucumberListener::escape).toArray(String[]::new))
                    .toArray(String[][]::new);
            node.info(MarkupHelper.createTable(cells));
        } else if (argument instanceof DocStringArgument docString) {
            node.info(MarkupHelper.createCodeBlock(escape(docString.getContent())));
        }
    }

    private void onStepFinished(TestStepFinished event) {
        Result result = event.getResult();
        Instant end = event.getInstant();

        if (event.getTestStep() instanceof HookTestStep hook) {
            // Hooks only show up in the report when they break the scenario
            if (result.getError() != null) {
                String title = "<b>" + hook.getHookType() + " hook failed</b>";
                record(event.getTestCase(), nodes -> nodes.log(step -> step.fail(title).fail(result.getError())));
            }
            return;
        }
        record(event.getTestCase(), nodes -> {
            switch (result.getStatus()) {
                case PASSED -> {
                    // a step without logs is PASS already
                }
                case FAILED, AMBIGUOUS -> nodes.step.fail(result.getError());
                case UNDEFINED -> nodes.step.skip("Step is not implemented (undefined)");
                case PENDING -> nodes.step.skip("Step is pending" + message(result));
                default -> nodes.step.skip("Skipped" + message(result));
            }
            nodes.times.get(nodes.step)[1] = end;
        });
    }

    /** scenario.log(...) text. */
    private void onWrite(WriteEvent event) {
        String text = escape(event.getText()).replace("\n", "<br>");
        record(event.getTestCase(), nodes -> nodes.log(step -> step.info(text)));
    }

    /** scenario.attach(...): images are embedded, text/uri-list becomes a link, other text a code block. */
    private void onEmbed(EmbedEvent event) {
        String name = escape(event.getName() == null ? "Attachment" : event.getName());
        String mediaType = event.getMediaType();
        String text = new String(event.getData(), StandardCharsets.UTF_8);
        Consumer<ExtentTest> action;

        if (mediaType.startsWith("image/")) {
            String base64 = Base64.getEncoder().encodeToString(event.getData());
            action = step -> step.info(name, MediaEntityBuilder.createScreenCaptureFromBase64String(base64, name).build());
        } else if (mediaType.equals("text/uri-list")) {
            // e.g. the scenario video, relative to the run folder this report is saved in
            String links = text.lines()
                    .filter(link -> !link.isBlank())
                    .map(link -> "<a href='" + escape(link.trim()) + "' target='_blank'>" + name + "</a>")
                    .reduce((a, b) -> a + "<br>" + b)
                    .orElse(name);
            action = step -> step.info(links);
        } else if (mediaType.startsWith("text/") || mediaType.endsWith("json") || mediaType.endsWith("xml")) {
            action = step -> step.info("<b>" + name + "</b>").info(MarkupHelper.createCodeBlock(escape(text)));
        } else {
            String note = name + " (" + mediaType + ", " + event.getData().length + " bytes) - not shown";
            action = step -> step.info(note);
        }
        record(event.getTestCase(), nodes -> nodes.log(action));
    }

    private void onScenarioFinished(TestCaseFinished event) {
        TestCase testCase = event.getTestCase();
        Attempt attempt = attempts.remove(testCase.getId());
        Result result = event.getResult();
        String key = key(testCase);

        if (ScenarioRetry.willRetry(testCase, result.getStatus())) {
            retryHistory.computeIfAbsent(key, k -> new ArrayList<>())
                    .add("Attempt " + attempt.number + " failed: " + firstLine(result.getError()));
            return;
        }
        List<String> history = retryHistory.remove(key);
        synchronized (this) {
            addToReport(testCase, attempt, event.getInstant(), result.getStatus(), history);
        }
    }

    private void addToReport(TestCase testCase, Attempt attempt, Instant end, Status status, List<String> history) {
        ExtentTest feature = features.computeIfAbsent(testCase.getUri(), uri -> {
            ExtentTest node = extent.createTest(Feature.class, featureNames.getOrDefault(uri, featurePath(uri)),
                    featurePath(uri));
            node.getModel().setStartTime(Date.from(attempt.start));
            return node;
        });
        ScenarioNodes nodes = new ScenarioNodes(feature.createNode(Scenario.class, testCase.getName()));
        testCase.getTags().forEach(tag -> nodes.scenario.assignCategory(tag.substring(1)));
        if (testCase.getTags().contains("@ui")) {
            nodes.scenario.assignDevice(ConfigReader.get("browser"));
        }
        if (history != null) {
            nodes.scenario.assignCategory("retried");
            history.forEach(line -> nodes.log(step -> step.info("<b>Retry</b> - " + escape(line))));
            String outcome = status == Status.PASSED ? "Passed on attempt " : "Still failing on attempt ";
            nodes.log(step -> step.info("<b>Retry</b> - " + outcome + attempt.number));
        }

        attempt.actions.forEach(action -> action.accept(nodes));
        if (!nodes.pending.isEmpty()) {
            // scenario without steps - give the hook output a row of its own
            ExtentTest hooks = nodes.scenario.createNode("Hooks");
            nodes.pending.forEach(action -> action.accept(hooks));
        }

        nodes.times.forEach((step, startEnd) -> setTimes(step, startEnd[0], startEnd[1]));
        setTimes(nodes.scenario, attempt.start, end);
        if (feature.getModel().getStartTime().after(Date.from(attempt.start))) {
            feature.getModel().setStartTime(Date.from(attempt.start));
        }
        if (feature.getModel().getEndTime().before(Date.from(end))) {
            feature.getModel().setEndTime(Date.from(end));
        }
    }

    private synchronized void finishReport() {
        if (features.isEmpty()) {
            // e.g. testng-rerun.xml when nothing failed; Extent cannot write a report without tests
            System.out.println("[Extent] No scenarios ran - no Extent report written");
            return;
        }
        try {
            extent.flush();
            System.out.println("[Extent] Report saved in " + RunInfo.runFolder().toAbsolutePath());
        } catch (RuntimeException e) {
            System.out.println("[Extent] Could not write the report: " + e.getMessage());
        }
    }

    /** Adds an action to the scenario's current attempt (each attempt runs on one thread). */
    private void record(TestCase testCase, Consumer<ScenarioNodes> action) {
        Attempt attempt = attempts.get(testCase.getId());
        if (attempt != null) {
            attempt.actions.add(action);
        }
    }

    private static void setTimes(ExtentTest test, Instant start, Instant end) {
        test.getModel().setStartTime(Date.from(start));
        test.getModel().setEndTime(Date.from(end));
    }

    private static String key(TestCase testCase) {
        return ScenarioRetry.key(testCase.getUri(), testCase.getLocation().getLine());
    }

    /** file:///C:/.../features/app1/accounts.feature -> app1/accounts.feature */
    private static String featurePath(URI uri) {
        String path = uri.toString().replace('\\', '/');
        int index = path.indexOf("/features/");
        return index >= 0 ? path.substring(index + "/features/".length()) : path.substring(path.lastIndexOf('/') + 1);
    }

    private static String firstLine(Throwable error) {
        if (error == null) {
            return "unknown error";
        }
        String message = error.getMessage();
        return message == null || message.isBlank()
                ? error.getClass().getSimpleName()
                : message.lines().findFirst().orElse(message);
    }

    private static String message(Result result) {
        return result.getError() == null || result.getError().getMessage() == null
                ? "" : ": " + escape(result.getError().getMessage());
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
