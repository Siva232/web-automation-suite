package com.example.hooks;

import com.example.utils.ConfigReader;
import com.example.utils.DBUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/** Opens a DB connection only for scenarios tagged @db. */
public class DatabaseHooks {

    @Before("@db")
    public void connect(Scenario scenario) {
        scenario.log("Database: " + ConfigReader.get("db.url"));
        DBUtil.connect();
    }

    @After("@db")
    public void disconnect() {
        DBUtil.close();
    }
}
