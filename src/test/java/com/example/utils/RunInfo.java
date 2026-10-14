package com.example.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Identity of the current test run. Everything a run produces goes into one folder:
 * <pre>
 * Test Results/
 *   TestRun_QA_2026-09-28_10-15-30/
 *     TestReport_QA_2026-09-28_10-15-30.html
 *     ExtentReport_QA_2026-09-28_10-15-30.html
 *     cucumber.json
 *     rerun.txt
 *     Videos/
 * </pre>
 * The timestamp is fixed the first time this class is used (at suite start).
 * A "run.label" system property is appended, e.g. TestRun_QA_2026-09-28_10-15-30_Rerun (set by FailedScenariosRunner).
 */
public final class RunInfo {

    /** Prefix of every run folder; used to find old runs to delete. */
    public static final String RUN_FOLDER_PREFIX = "TestRun_";

    private static final String RUN_ID = ConfigReader.getEnv().toUpperCase() + "_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + label();

    private RunInfo() {
    }

    private static String label() {
        String label = System.getProperty("run.label");
        return label == null || label.isBlank() ? "" : "_" + label.trim();
    }

    /** e.g. QA_2026-09-28_10-15-30 */
    public static String runId() {
        return RUN_ID;
    }

    /** "report.folder" from config.properties, relative to the project folder. */
    public static Path resultsFolder() {
        return Paths.get(ConfigReader.get("report.folder"));
    }

    /** Folder of this run: Test Results/TestRun_QA_2026-09-28_10-15-30 */
    public static Path runFolder() {
        return resultsFolder().resolve(RUN_FOLDER_PREFIX + RUN_ID);
    }

    /** Videos of this run: Test Results/TestRun_QA_2026-09-28_10-15-30/Videos */
    public static Path videoFolder() {
        return runFolder().resolve("Videos");
    }
}
