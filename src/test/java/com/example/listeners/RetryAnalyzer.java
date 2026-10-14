package com.example.listeners;

import com.example.utils.ScenarioRetry;
import io.cucumber.testng.Pickle;
import io.cucumber.testng.PickleWrapper;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Runs a failed scenario again, up to "rerun.count" times (see {@link ScenarioRetry}).
 * Set on the runners' runScenario test. TestNG reports the failed attempts as skipped,
 * so the run only fails when the last attempt fails.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    @Override
    public boolean retry(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters.length == 0 || !(parameters[0] instanceof PickleWrapper wrapper)) {
            return false;
        }
        Pickle pickle = wrapper.getPickle();
        boolean retry = ScenarioRetry.consumeRetry(ScenarioRetry.key(pickle.getUri(), pickle.getLine()));
        if (retry) {
            System.out.println("[Retry] Running failed scenario again: " + pickle.getName());
        }
        return retry;
    }
}
