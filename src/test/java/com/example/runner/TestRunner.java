package com.example.runner;

import com.example.utils.ReportArchiver;
import com.example.utils.RunInfo;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.example.stepdefinitions", "com.example.hooks"},
        tags = "not @ignore",
        plugin = {
                "pretty",
                "html:" + TestRunner.HTML_REPORT,
                "json:" + TestRunner.JSON_REPORT,
                "rerun:" + TestRunner.RERUN_FILE
        },
        monochrome = true
)
public class TestRunner extends AbstractTestNGCucumberTests {

    /** Cucumber output of the current run; copied into Test Results/TestRun_<ENV>_<timestamp> after the run. */
    public static final String HTML_REPORT = "target/cucumber-reports/cucumber.html";
    public static final String JSON_REPORT = "target/cucumber-reports/cucumber.json";
    public static final String RERUN_FILE = "target/cucumber-reports/rerun.txt";

    @BeforeClass(alwaysRun = true)
    public void fixRunTimestamp() {
        RunInfo.runId(); // report and video folder names use the run's start time
    }

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

    /** Cucumber writes the HTML report in super.tearDownClass(); archive it right after. */
    @Override
    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        super.tearDownClass();
        ReportArchiver.archive(HTML_REPORT, JSON_REPORT, RERUN_FILE);
    }
}
