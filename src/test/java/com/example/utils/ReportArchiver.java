package com.example.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Collects the results of the finished run into its own folder (see {@link RunInfo}):
 * the Cucumber HTML report as TestReport_<ENV>_<yyyy-MM-dd_HH-mm-ss>.html plus the other result files,
 * next to the Videos folder the recorder already wrote. Then keeps only the newest
 * "report.history.count" run folders (0 = keep all).
 * <p>
 * Called from TestRunner after Cucumber has finished writing its reports.
 */
public final class ReportArchiver {

    private ReportArchiver() {
    }

    /**
     * @param htmlReport  Cucumber HTML report of the run
     * @param otherFiles  further result files to copy as they are (e.g. cucumber.json, rerun.txt)
     */
    public static void archive(String htmlReport, String... otherFiles) {
        Path html = Paths.get(htmlReport);
        if (!Files.exists(html)) {
            System.out.println("[Report] No HTML report found at " + html.toAbsolutePath() + " - nothing archived");
            return;
        }
        try {
            Path runFolder = RunInfo.runFolder();
            Files.createDirectories(runFolder);

            Files.copy(html, runFolder.resolve("TestReport_" + RunInfo.runId() + ".html"),
                    StandardCopyOption.REPLACE_EXISTING);
            for (String file : otherFiles) {
                Path source = Paths.get(file);
                if (Files.exists(source)) {
                    Files.copy(source, runFolder.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            System.out.println("[Report] Test results saved in " + runFolder.toAbsolutePath());

            deleteOldRuns(ConfigReader.getInt("report.history.count"));
        } catch (IOException e) {
            System.out.println("[Report] Could not save the test results: " + e.getMessage());
        }
    }

    /** Keeps the newest {@code keep} TestRun_* folders and deletes the rest. */
    private static void deleteOldRuns(int keep) throws IOException {
        if (keep <= 0) {
            return;
        }
        List<Path> runs;
        try (Stream<Path> entries = Files.list(RunInfo.resultsFolder())) {
            runs = entries
                    .filter(p -> Files.isDirectory(p)
                            && p.getFileName().toString().startsWith(RunInfo.RUN_FOLDER_PREFIX))
                    .sorted(Comparator.comparing(ReportArchiver::lastModified).reversed())
                    .toList();
        }
        for (Path old : runs.subList(Math.min(keep, runs.size()), runs.size())) {
            deleteRecursively(old);
            System.out.println("[Report] Deleted old test run " + old.getFileName());
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
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
