# Web Automation Suite

Java + Maven + Cucumber + TestNG + Selenium WebDriver.

Only official artifacts from Maven Central are used:

| Artifact | Group | Version |
|---|---|---|
| cucumber-bom / cucumber-java / cucumber-testng | io.cucumber | 7.34.9 |
| testng | org.testng | 7.12.0 |
| selenium-java | org.seleniumhq.selenium | 4.49.0 |
| mysql-connector-j | com.mysql | 26.7.0 |
| maven-compiler-plugin | org.apache.maven.plugins | 3.16.0 |
| maven-surefire-plugin | org.apache.maven.plugins | 3.6.0 |

Browser drivers are handled by Selenium's built-in **Selenium Manager**, so no separate driver library is needed.

## Prerequisites
- JDK 17 or newer (`JAVA_HOME` set)
- Maven 3.9+
- Chrome, Firefox or Edge installed

## Applications and modules
| App | Sample site | Modules (one feature file each) |
|---|---|---|
| App1 | the-internet.herokuapp.com | login, home, accounts, forms, alerts (+ database) |
| App2 | www.saucedemo.com | login, products (home), product_details, cart, checkout |

## Structure
```
pom.xml                              dependencies and plugins
testng.xml                           TestNG suite that runs the Cucumber runner
src/test/java/com/example/
  runner/TestRunner.java             @CucumberOptions + TestNG runner (parallel DataProvider)
  listeners/ParallelExecutionListener.java   applies parallel.execution / thread.count
  hooks/AppHooks.java                picks the app (@app1/@app2) for each scenario
  hooks/Hooks.java                   browser setup/teardown for @ui, screenshot on failure
  hooks/DatabaseHooks.java           DB connection for @db
  pages/BasePage.java                shared wait-based actions
  pages/app1/*Page.java              App1 page objects (one per module)
  pages/app2/*Page.java              App2 page objects (one per module)
  stepdefinitions/app1/*Steps.java   App1 steps
  stepdefinitions/app2/*Steps.java   App2 steps
  stepdefinitions/common/DatabaseSteps.java
  utils/AppContext.java              current app per thread
  utils/ConfigReader.java            env + app aware config lookup
  utils/DriverFactory.java           ThreadLocal WebDriver (parallel-safe)
  utils/DBUtil.java                  JDBC helper
src/test/resources/
  config.properties
  features/app1/*.feature
  features/app2/*.feature
  db/setup-test-db.sql
  META-INF/services/org.testng.ITestNGListener   registers the parallel listener
```

## Environments and applications
`config.properties` keys are `<env>.<app>.<key>`:
```
env=qa
applications=app1,app2
qa.app1.base.url=...   qa.app1.username=...   qa.app1.password=...   qa.app1.db.url=...
qa.app2.base.url=...   ...
uat.app1.base.url=...  ...
uat.app2.base.url=...  ...
```
A feature file tagged `@app1` or `@app2` uses that app's settings. Code asks for the plain key
(`ConfigReader.get("base.url")`) and gets the value for the active env and the scenario's app.
Lookup order: `-D<app>.<key>` → `-D<key>` → `<env>.<app>.<key>` → `<env>.<key>` → `<app>.<key>` → `<key>`.

Step wording includes the app name ("the App1 login page", "the App2 cart") because Cucumber step
text must be unique across the whole suite.

### Adding an application (e.g. App3)
1. Add `app3` to `applications` and add `qa.app3.*` / `uat.app3.*` keys.
2. Create `pages/app3`, `stepdefinitions/app3` and `features/app3` (tag the features `@app3 @ui`).

## Parallel execution
```
parallel.execution=true    # false = run scenarios one after another
thread.count=3             # scenarios (browsers) running at the same time
```

## Database tests (MySQL)
- Scenarios tagged `@db` open a JDBC connection (`DatabaseHooks`) using `db.url`, `db.username`, `db.password`
  for the active env. Scenarios tagged `@ui` open a browser. A scenario with both tags gets both.
- `DBUtil` is plain JDBC with PreparedStatements; table/column names from feature files are validated.
- One-time setup of the sample databases (App1: `qa_db`, `uat_db`; App2: `qa_app2_db`, `uat_app2_db`)
  and the `qa_automation` user:
  ```
  mysql -u root -p < src/test/resources/db/setup-test-db.sql
  ```
- Driver: official `com.mysql:mysql-connector-j`.

## Running
In PowerShell, quote every `-D` option that contains a dot, otherwise PowerShell splits it.
```
mvn clean test                                                # everything, env from config.properties
mvn clean test -Denv=uat                                      # run against UAT
mvn clean test "-Dcucumber.filter.tags=@app1"                 # App1 only
mvn clean test "-Dcucumber.filter.tags=@app2 and @cart"       # one module of one app
mvn clean test "-Dcucumber.filter.tags=@smoke and not @db"    # smoke, UI only
mvn clean test "-Dcucumber.filter.tags=@db"                   # database tests only
mvn clean test "-Dparallel.execution=false"                   # run sequentially
mvn clean test "-Dthread.count=5"                             # 5 parallel browsers
mvn clean test -Dbrowser=firefox -Dheadless=true
mvn clean test "-Dapp1.password=secret"                       # override one app's value
mvn clean test "-Dvideo.recording=false"                      # no videos for this run
mvn clean test "-Dvideo.save.for=failed"                      # keep videos of failed scenarios only
```

## Reports
- **Test Results history:** every suite execution gets one folder with everything it produced:
  ```
  Test Results/
    TestRun_QA_2026-09-28_10-15-30/
      TestReport_QA_2026-09-28_10-15-30.html   HTML report, screenshots included, links to videos
      cucumber.json                            raw results
      rerun.txt                                failed scenarios
      Videos/                                  one video per scenario
  ```
  Only the newest `report.history.count` run folders are kept (config.properties; 0 = keep all).
  Parent folder name: `report.folder`.
- **Videos:** with `video.recording=true`, every UI scenario is recorded (Chrome/Edge, also headless and
  in parallel) and saved as `Videos/<app>-<module>_L<line>_<Scenario_name>_<PASSED|FAILED>.avi` in the
  run folder. Each scenario in the HTML report links to its video.
  `video.save.for=failed` keeps only failed scenarios' videos. The `.avi` files (Motion-JPEG) play in VLC or Windows Media Player, not inside a web browser.
  Recording uses Chrome's built-in DevTools screencast via Selenium, with no extra libraries.
- Cucumber HTML (latest run): `target/cucumber-reports/cucumber.html` (failure screenshots are attached here)
- Cucumber JSON: `target/cucumber-reports/cucumber.json`
- Failed scenarios list: `target/cucumber-reports/rerun.txt`
- TestNG: `target/surefire-reports/`

## Adding a module to an app
1. Add `features/<app>/<module>.feature`, tagged `@<app> @ui @<module>`.
2. Add a page object under `pages/<app>/` extending `BasePage`.
3. Add step definitions under `stepdefinitions/<app>/` (include the app name in the step text).
