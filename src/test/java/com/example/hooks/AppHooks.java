package com.example.hooks;

import com.example.utils.AppContext;
import com.example.utils.ConfigReader;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

import java.util.List;

/**
 * Detects which application a scenario belongs to from its tags (@app1, @app2, ...).
 * Runs before all other hooks (order = 0) so the browser and DB hooks use that app's config.
 */
public class AppHooks {

    @Before(order = 0)
    public void setApplication(Scenario scenario) {
        List<String> applications = ConfigReader.getList("applications");
        String app = scenario.getSourceTagNames().stream()
                .map(tag -> tag.substring(1).toLowerCase())
                .filter(applications::contains)
                .findFirst()
                .orElse(null);
        AppContext.set(app);
        scenario.log("Environment: " + ConfigReader.getEnv().toUpperCase()
                + " | Application: " + (app == null ? "-" : app.toUpperCase()));
    }

    /** @After hooks run in descending order, so order = 0 runs last. */
    @After(order = 0)
    public void clearApplication() {
        AppContext.clear();
    }
}
