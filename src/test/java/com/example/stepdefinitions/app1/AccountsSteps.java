package com.example.stepdefinitions.app1;

import com.example.pages.app1.AccountsPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.Map;

public class AccountsSteps {

    private final AccountsPage accountsPage = new AccountsPage();

    @Given("the user is on the App1 accounts page")
    public void theUserIsOnTheAccountsPage() {
        accountsPage.openPage();
    }

    @Then("the App1 accounts table should have the columns:")
    public void theAccountsTableShouldHaveTheColumns(List<String> expected) {
        Assert.assertEquals(accountsPage.getColumnHeaders(), expected, "Accounts table columns");
    }

    @Then("the App1 accounts table should list {int} accounts")
    public void theAccountsTableShouldList(int expected) {
        Assert.assertEquals(accountsPage.getAccountCount(), expected, "Number of accounts");
    }

    @Then("the App1 account with email {string} should have:")
    public void theAccountShouldHave(String email, Map<String, String> expected) {
        Map<String, String> account = accountsPage.getAccountByEmail(email);
        Assert.assertFalse(account.isEmpty(), "No account found with email " + email);
        SoftAssert soft = new SoftAssert();
        expected.forEach((column, value) ->
                soft.assertEquals(account.get(column), value, "Mismatch in column '" + column + "'"));
        soft.assertAll();
    }

    @When("the user sorts the App1 accounts by {string}")
    public void theUserSortsBy(String column) {
        accountsPage.sortBy(column);
    }

    @Then("the App1 {string} column should read:")
    public void theColumnShouldRead(String column, List<String> expected) {
        Assert.assertEquals(accountsPage.getColumnValues(column), expected, "Values in column '" + column + "'");
    }
}
