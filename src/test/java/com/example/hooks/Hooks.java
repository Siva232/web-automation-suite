package com.example.hooks;

import com.example.utils.ConfigReader;
import com.example.utils.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/** Browser lifecycle for scenarios tagged @ui (DB-only scenarios do not open a browser). */
public class Hooks {

    @Before("@ui")
    public void setUp(Scenario scenario) {
        scenario.log("URL: " + ConfigReader.get("base.url"));
        DriverFactory.initDriver();
    }

    /** Attach a screenshot to the Cucumber report for the step that failed. */
    @AfterStep("@ui")
    public void captureOnFailure(Scenario scenario) {
        if (scenario.isFailed()) {
            byte[] screenshot = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Failure screenshot");
        }
    }

    @After("@ui")
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
