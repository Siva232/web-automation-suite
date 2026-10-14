package com.example.runner;

import com.example.listeners.RetryAnalyzer;
import com.example.utils.ReportArchiver;
import com.example.utils.RunInfo;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Behaviour shared by the runners (TestRunner, FailedScenariosRunner): parallel scenarios,
 * retry of failed scenarios and archiving of the results. Each runner only adds its {@code @CucumberOptions};
 * this class has none, because Cucumber merges the options of the whole class hierarchy.
 */
public abstract class BaseRunner extends AbstractTestNGCucumberTests {

    /** Cucumber output of the current run; copied into Test Results/TestRun_<ENV>_<timestamp> after the run. */
    public static final String HTML_REPORT = "target/cucumber-reports/cucumber.html";
    public static final String JSON_REPORT = "target/cucumber-reports/cucumber.json";
    /** Scenarios still failing after their retries; input of FailedScenariosRunner (testng-rerun.xml). */
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

    /** Same as Cucumber's test, plus RetryAnalyzer: a failed scenario runs again up to "rerun.count" times. */
    @Override
    @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "scenarios",
            retryAnalyzer = RetryAnalyzer.class)
    public void runScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        super.runScenario(pickleWrapper, featureWrapper);
    }

    /** Cucumber writes the HTML report in super.tearDownClass(); archive it right after. */
    @Override
    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        super.tearDownClass();
        ReportArchiver.archive(HTML_REPORT, JSON_REPORT, RERUN_FILE);
    }
}
