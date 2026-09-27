package com.example.listeners;

import com.example.utils.ConfigReader;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Applies "parallel.execution" and "thread.count" from config.properties to the TestNG suite
 * before it starts. TestRunner's scenario DataProvider is parallel, so the thread count
 * decides how many scenarios run at once (1 = sequential).
 * <p>
 * Registered via META-INF/services/org.testng.ITestNGListener - TestNG does not call
 * IAlterSuiteListener when it is declared inside testng.xml.
 */
public class ParallelExecutionListener implements IAlterSuiteListener {

    private static final AtomicBoolean LOGGED = new AtomicBoolean();

    @Override
    public void alter(List<XmlSuite> suites) {
        boolean parallel = ConfigReader.getBoolean("parallel.execution");
        int threads = parallel ? Math.max(1, ConfigReader.getInt("thread.count")) : 1;
        // Surefire may pass the suite through more than once; log only the first time
        if (LOGGED.compareAndSet(false, true)) {
            System.out.println("[Suite] Environment: " + ConfigReader.getEnv().toUpperCase()
                    + " | Parallel execution: " + parallel + " | Threads: " + threads);
        }
        suites.forEach(suite -> suite.setDataProviderThreadCount(threads));
    }
}
