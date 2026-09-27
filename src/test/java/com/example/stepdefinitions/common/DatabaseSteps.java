package com.example.stepdefinitions.common;

import com.example.utils.ConfigReader;
import com.example.utils.DBUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static com.example.utils.DBUtil.identifier;

public class DatabaseSteps {

    /** Record fetched by the last "is looked up" step; null when not found. */
    private Map<String, Object> userRecord;

    @Given("the database connection is established")
    public void theDatabaseConnectionIsEstablished() throws SQLException {
        Assert.assertTrue(DBUtil.getConnection().isValid(ConfigReader.getInt("db.timeout")),
                "Database connection is not valid");
    }

    @Then("the table {string} should exist")
    public void theTableShouldExist(String table) {
        long count = DBUtil.queryForCount(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                table);
        Assert.assertEquals(count, 1L, "Table '" + table + "' does not exist");
    }

    @Then("the table {string} should have the columns:")
    public void theTableShouldHaveTheColumns(String table, List<String> expectedColumns) {
        List<Object> actual = DBUtil.query(
                        "SELECT column_name FROM information_schema.columns "
                                + "WHERE table_schema = DATABASE() AND table_name = ?", table)
                .stream().map(row -> row.values().iterator().next()).toList();
        SoftAssert soft = new SoftAssert();
        for (String column : expectedColumns) {
            soft.assertTrue(actual.contains(column), "Column '" + column + "' missing from '" + table + "'");
        }
        soft.assertAll();
    }

    @Then("the table {string} should have at least {int} row(s)")
    public void theTableShouldHaveAtLeastRows(String table, int minRows) {
        long count = DBUtil.queryForCount("SELECT COUNT(*) FROM " + identifier(table));
        Assert.assertTrue(count >= minRows, "Expected at least " + minRows + " rows in '" + table + "' but found " + count);
    }

    @Then("the column {string} in table {string} should have no null values")
    public void theColumnShouldHaveNoNullValues(String column, String table) {
        long nulls = DBUtil.queryForCount(
                "SELECT COUNT(*) FROM " + identifier(table) + " WHERE " + identifier(column) + " IS NULL");
        Assert.assertEquals(nulls, 0L, nulls + " NULL value(s) found in " + table + "." + column);
    }

    @Then("the column {string} in table {string} should have no duplicate values")
    public void theColumnShouldHaveNoDuplicateValues(String column, String table) {
        List<Map<String, Object>> duplicates = DBUtil.query(
                "SELECT " + identifier(column) + ", COUNT(*) AS occurrences FROM " + identifier(table)
                        + " GROUP BY " + identifier(column) + " HAVING COUNT(*) > 1");
        Assert.assertTrue(duplicates.isEmpty(), "Duplicate values in " + table + "." + column + ": " + duplicates);
    }

    @When("the configured application user is looked up in the {string} table")
    public void theConfiguredUserIsLookedUp(String table) {
        theUserIsLookedUp(ConfigReader.get("username"), table);
    }

    @When("the user {string} is looked up in the {string} table")
    public void theUserIsLookedUp(String username, String table) {
        List<Map<String, Object>> rows = DBUtil.query(
                "SELECT * FROM " + identifier(table) + " WHERE username = ?", username);
        userRecord = rows.isEmpty() ? null : rows.get(0);
    }

    @Then("the user record should exist")
    public void theUserRecordShouldExist() {
        Assert.assertNotNull(userRecord, "User record was not found");
    }

    @Then("the user record should not exist")
    public void theUserRecordShouldNotExist() {
        Assert.assertNull(userRecord, "User record was found but should not exist: " + userRecord);
    }

    @Then("the user record should have:")
    public void theUserRecordShouldHave(Map<String, String> expected) {
        Assert.assertNotNull(userRecord, "User record was not found");
        SoftAssert soft = new SoftAssert();
        expected.forEach((column, value) -> {
            soft.assertTrue(userRecord.containsKey(column), "Column '" + column + "' not in record " + userRecord.keySet());
            soft.assertEquals(String.valueOf(userRecord.get(column)), value, "Mismatch in column '" + column + "'");
        });
        soft.assertAll();
    }

    /** Inserts a row from a two-column data table. Any existing row with the same username is removed first. */
    @When("a new user is inserted into the {string} table:")
    public void aNewUserIsInserted(String table, Map<String, String> data) {
        String username = data.get("username");
        Assert.assertNotNull(username, "Data table must contain a 'username' row");
        theUserIsDeleted(username, table);

        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        data.keySet().forEach(column -> {
            columns.add(identifier(column));
            placeholders.add("?");
        });
        int inserted = DBUtil.update(
                "INSERT INTO " + identifier(table) + " (" + columns + ") VALUES (" + placeholders + ")",
                data.values().toArray());
        Assert.assertEquals(inserted, 1, "Insert did not create exactly one row");
    }

    @When("the user {string} is deleted from the {string} table")
    public void theUserIsDeleted(String username, String table) {
        DBUtil.update("DELETE FROM " + identifier(table) + " WHERE username = ?", username);
    }
}
