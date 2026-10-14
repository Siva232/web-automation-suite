package com.example.hooks;

import com.example.utils.ConfigReader;
import com.example.utils.DriverFactory;
import com.example.utils.RunInfo;
import com.example.utils.ScenarioRetry;
import com.example.utils.VideoRecorder;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.IOException;
import java.nio.file.Path;

/** Browser lifecycle for scenarios tagged @ui (DB-only scenarios do not open a browser). */
public class Hooks {

    @Before("@ui")
    public void setUp(Scenario scenario) {
        scenario.log("URL: " + ConfigReader.get("base.url"));
        DriverFactory.initDriver();
        VideoRecorder.start(DriverFactory.getDriver());
    }

    /** Attach a screenshot to the reports (screenshot.capture = failed | all | none). */
    @AfterStep("@ui")
    public void captureScreenshot(Scenario scenario) {
        String capture = ConfigReader.get("screenshot.capture").toLowerCase();
        boolean wanted = capture.equals("all") || (capture.equals("failed") && scenario.isFailed());
        if (wanted) {
            byte[] screenshot = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", scenario.isFailed() ? "Failure screenshot" : "Screenshot");
        }
    }

    @After("@ui")
    public void tearDown(Scenario scenario) {
        try {
            saveVideo(scenario);
        } finally {
            DriverFactory.quitDriver();
        }
    }

    /**
     * Saves the scenario's video to Test Results/TestRun_<run>/Videos/ (video.save.for = all | failed)
     * and adds a link to it in the HTML report.
     */
    private void saveVideo(Scenario scenario) {
        VideoRecorder recorder = VideoRecorder.stop();
        if (recorder == null) {
            return;
        }
        boolean failedOnly = "failed".equalsIgnoreCase(ConfigReader.get("video.save.for"));
        if (failedOnly && !scenario.isFailed()) {
            return;
        }
        // A video problem is only logged - it must never fail the scenario
        try {
            Path file = RunInfo.videoFolder().resolve(videoFileName(scenario));
            if (recorder.save(file, ConfigReader.getInt("video.fps"))) {
                // Link is relative to the report saved in the same run folder, e.g. Videos/xyz.avi
                String link = RunInfo.runFolder().relativize(file).toString().replace('\\', '/');
                scenario.attach(link, "text/uri-list", "Scenario video");
            }
        } catch (IOException | RuntimeException e) {
            scenario.log("Video could not be saved: " + e.getMessage());
        }
    }

    /**
     * e.g. app2-cart_L14_Cart_shows_the_products_that_were_added_PASSED.avi,
     * with "_attempt2", "_attempt3", ... for the retries of a failed scenario (rerun.count).
     */
    private static String videoFileName(Scenario scenario) {
        int attempt = ScenarioRetry.attempt(ScenarioRetry.key(scenario.getUri(), scenario.getLine()));
        // URI like file:///C:/.../features/app2/cart.feature -> "app2-cart"
        String[] parts = scenario.getUri().toString().split("/");
        String feature = (parts.length > 1 ? parts[parts.length - 2] + "-" : "")
                + parts[parts.length - 1].replace(".feature", "");
        String name = scenario.getName().replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_|_$", "");
        if (name.length() > 80) {
            name = name.substring(0, 80);
        }
        return feature + "_L" + scenario.getLine() + "_" + name + "_" + scenario.getStatus()
                + (attempt > 1 ? "_attempt" + attempt : "") + ".avi";
    }
}
