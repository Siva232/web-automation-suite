package com.example.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.stepdefinitions", "com.example.hooks"},
        tags = "not @ignore",
        plugin = {
                "pretty",
                "html:" + TestRunner.HTML_REPORT,
                "json:target/cucumber-reports/cucumber.json",
                "rerun:target/cucumber-reports/rerun.txt"
        },
        monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {

    /** Cucumber HTML report of the current run; ReportArchiveListener copies it into the Test Results folder. */
    public static final String HTML_REPORT = "target/cucumber-reports/cucumber.html";

    /**
     * Each Cucumber scenario becomes one TestNG test.
     * Parallel is always on here; ParallelExecutionListener sets the thread count from
     * config.properties (parallel.execution / thread.count), and 1 thread means sequential.
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
