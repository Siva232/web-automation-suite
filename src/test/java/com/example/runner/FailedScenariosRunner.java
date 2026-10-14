package com.example.runner;

import io.cucumber.testng.CucumberOptions;

/**
 * Runs only the scenarios that failed in the previous run (testng-rerun.xml), read from the rerun file
 * that run left in target/cucumber-reports. Its own failures are written to the same file again,
 * so running testng-rerun.xml repeatedly works through what is still failing.
 * <p>
 * Results go to a run folder ending in "_Rerun", e.g. Test Results/TestRun_QA_2026-10-12_10-15-30_Rerun.
 */
@CucumberOptions(
        features = "@" + BaseRunner.RERUN_FILE,
        glue = {"com.example.stepdefinitions", "com.example.hooks"},
        plugin = {
                "pretty",
                "html:" + BaseRunner.HTML_REPORT,
                "json:" + BaseRunner.JSON_REPORT,
                "com.example.listeners.FailedScenarioListener:" + BaseRunner.RERUN_FILE,
                "com.example.listeners.ExtentCucumberListener"
        },
        monochrome = true
)
public class FailedScenariosRunner extends BaseRunner {

    static {
        // Runs when TestNG loads this class, before anything has named the run (see RunInfo)
        if (System.getProperty("run.label") == null) {
            System.setProperty("run.label", "Rerun");
        }
    }
}
