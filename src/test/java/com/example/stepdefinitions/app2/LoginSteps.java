package com.example.stepdefinitions.app2;

import com.example.pages.app2.LoginPage;
import com.example.pages.app2.ProductsPage;
import com.example.utils.ConfigReader;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class LoginSteps {

    private final LoginPage loginPage = new LoginPage();

    @Given("the user is on the App2 login page")
    public void theUserIsOnTheLoginPage() {
        loginPage.openPage();
    }

    /** Shortcut used by the other App2 modules, which all sit behind the login. */
    @Given("the user is logged in to App2")
    public void theUserIsLoggedIn() {
        loginPage.openPage();
        theUserLogsInWithValidCredentials();
        Assert.assertEquals(new ProductsPage().getPageTitle(), "Products", "Login to App2 failed");
    }

    @When("the user logs in to App2 with valid credentials")
    public void theUserLogsInWithValidCredentials() {
        loginPage.login(ConfigReader.get("username"), ConfigReader.get("password"));
    }

    @When("the user logs in to App2 as the locked out user")
    public void theUserLogsInAsLockedOutUser() {
        loginPage.login(ConfigReader.get("locked.username"), ConfigReader.get("password"));
    }

    @When("the user logs in to App2 with username {string} and password {string}")
    public void theUserLogsIn(String username, String password) {
        loginPage.login(username, password);
    }

    @Then("the App2 login error should be {string}")
    public void theLoginErrorShouldBe(String expected) {
        Assert.assertEquals(loginPage.getError(), expected, "Login error message");
    }

    @Then("the App2 login page should be displayed")
    public void theLoginPageShouldBeDisplayed() {
        Assert.assertTrue(loginPage.isDisplayed(), "App2 login page is not displayed");
    }
}
