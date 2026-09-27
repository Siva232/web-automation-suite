package com.example.stepdefinitions.app1;

import com.example.pages.app1.LoginPage;
import com.example.utils.ConfigReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LoginSteps {

    private final LoginPage loginPage = new LoginPage();

    @Given("the user is on the App1 login page")
    public void theUserIsOnTheLoginPage() {
        loginPage.openPage();
        Assert.assertTrue(loginPage.getCurrentUrl().endsWith("/login"), "Login page did not open");
    }

    @When("the user logs in to App1 with valid credentials")
    public void theUserLogsInWithValidCredentials() {
        loginPage.login(ConfigReader.get("username"), ConfigReader.get("password"));
    }

    @When("the user logs in to App1 with a valid username and password {string}")
    public void theUserLogsInWithValidUsernameAndPassword(String password) {
        loginPage.login(ConfigReader.get("username"), password);
    }

    @When("the user logs in to App1 with username {string} and a valid password")
    public void theUserLogsInWithUsernameAndValidPassword(String username) {
        loginPage.login(username, ConfigReader.get("password"));
    }

    @When("the user logs out of App1")
    public void theUserLogsOut() {
        loginPage.logout();
    }

    @Then("the App1 login message should be {string}")
    public void theLoginMessageShouldBe(String expected) {
        String actual = loginPage.getFlashMessage();
        Assert.assertTrue(actual.contains(expected),
                "Expected message to contain '" + expected + "' but was '" + actual + "'");
    }

    @Then("the App1 logout button should be displayed")
    public void theLogoutButtonShouldBeDisplayed() {
        Assert.assertTrue(loginPage.isLogoutVisible(), "Logout button is not displayed");
    }
}
