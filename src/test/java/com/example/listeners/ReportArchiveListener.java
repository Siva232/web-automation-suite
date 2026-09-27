package com.example.listeners;

import com.example.runner.TestRunner;
import com.example.utils.ConfigReader;
import org.testng.IExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * After the whole run finishes, copies the Cucumber HTML report into the folder set by
 * "report.folder" as TestReport_<ENV>_<yyyy-MM-dd_HH-mm-ss>.html, then keeps only the newest
 * "report.history.count" reports (0 = keep all).
 * <p>
 * Runs in onExecutionFinish because Cucumber writes the HTML file when the runner class finishes.
 * Registered via META-INF/services/org.testng.ITestNGListener.
 */
public class ReportArchiveListener implements IExecutionListener {

    private static final String PREFIX = "TestReport_";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    // Static: Surefire can register this listener more than once, but each run is archived once
    private static final AtomicBoolean ARCHIVED = new AtomicBoolean();
    private static volatile LocalDateTime startedAt;

    @Override
    public void onExecutionStart() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }

    @Override
    public void onExecutionFinish() {
        if (!ARCHIVED.compareAndSet(false, true)) {
            return;
        }
        Path source = Paths.get(TestRunner.HTML_REPORT);
        if (!Files.exists(source)) {
            System.out.println("[Report] No HTML report found at " + source.toAbsolutePath() + " - nothing archived");
            return;
        }
        try {
            Path folder = Paths.get(ConfigReader.get("report.folder"));
            Files.createDirectories(folder);

            String name = PREFIX + ConfigReader.getEnv().toUpperCase() + "_"
                    + (startedAt == null ? LocalDateTime.now() : startedAt).format(TIMESTAMP) + ".html";
            Path target = folder.resolve(name);
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[Report] Saved " + target.toAbsolutePath());

            deleteOldReports(folder, ConfigReader.getInt("report.history.count"));
        } catch (IOException e) {
            System.out.println("[Report] Could not archive the HTML report: " + e.getMessage());
        }
    }

    private static void deleteOldReports(Path folder, int keep) throws IOException {
        if (keep <= 0) {
            return;
        }
        List<Path> reports;
        try (Stream<Path> files = Files.list(folder)) {
            reports = files
                    .filter(p -> p.getFileName().toString().startsWith(PREFIX) && p.toString().endsWith(".html"))
                    .sorted(Comparator.comparing(ReportArchiveListener::lastModified).reversed())
                    .toList();
        }
        for (Path old : reports.subList(Math.min(keep, reports.size()), reports.size())) {
            Files.deleteIfExists(old);
            System.out.println("[Report] Deleted old report " + old.getFileName());
        }
    }

    private static long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }
}
