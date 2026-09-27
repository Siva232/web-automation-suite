package com.example.stepdefinitions.app1;

import com.example.pages.app1.HomePage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class HomeSteps {

    private final HomePage homePage = new HomePage();

    @Given("the user is on the App1 home page")
    public void theUserIsOnTheHomePage() {
        homePage.openPage();
    }

    @Then("the App1 home page heading should be {string}")
    public void theHeadingShouldBe(String expected) {
        Assert.assertEquals(homePage.getHeading(), expected);
    }

    @Then("the App1 home page sub-heading should be {string}")
    public void theSubHeadingShouldBe(String expected) {
        Assert.assertEquals(homePage.getSubHeading(), expected);
    }

    @Then("the App1 home page should list at least {int} modules")
    public void theHomePageShouldListAtLeast(int minimum) {
        int count = homePage.getMenuLinkCount();
        Assert.assertTrue(count >= minimum, "Expected at least " + minimum + " links but found " + count);
    }

    @When("the user opens the {string} link on the App1 home page")
    public void theUserOpensTheLink(String linkText) {
        homePage.openMenuLink(linkText);
    }

    @Then("the App1 page URL should end with {string}")
    public void thePageUrlShouldEndWith(String path) {
        String url = homePage.getCurrentUrl();
        Assert.assertTrue(url.endsWith(path), "Expected URL ending with '" + path + "' but was '" + url + "'");
    }
}
