package com.example.runner;

import io.cucumber.testng.CucumberOptions;

/** Runs all feature files (testng.xml). Shared behaviour is in {@link BaseRunner}. */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.stepdefinitions", "com.example.hooks"},
        tags = "",
        plugin = {
                "pretty",
                "html:" + BaseRunner.HTML_REPORT,
                "json:" + BaseRunner.JSON_REPORT,
                "com.example.listeners.FailedScenarioListener:" + BaseRunner.RERUN_FILE,
                "com.example.listeners.ExtentCucumberListener"
        },
        monochrome = true
)
public class TestRunner extends BaseRunner {
}
