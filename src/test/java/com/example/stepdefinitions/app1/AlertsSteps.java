package com.example.stepdefinitions.app1;

import com.example.pages.app1.AlertsPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class AlertsSteps {

    private final AlertsPage alertsPage = new AlertsPage();

    @Given("the user is on the App1 alerts page")
    public void theUserIsOnTheAlertsPage() {
        alertsPage.openPage();
    }

    @When("the user clicks the App1 {string} button")
    public void theUserClicksTheButton(String buttonText) {
        alertsPage.clickButton(buttonText);
    }

    @Then("the App1 browser alert should say {string}")
    public void theAlertShouldSay(String expected) {
        Assert.assertEquals(alertsPage.getAlertText(), expected, "Alert text");
    }

    @When("the user accepts the App1 alert")
    public void theUserAcceptsTheAlert() {
        alertsPage.acceptAlert();
    }

    @When("the user dismisses the App1 alert")
    public void theUserDismissesTheAlert() {
        alertsPage.dismissAlert();
    }

    @When("the user enters {string} in the App1 prompt")
    public void theUserEntersInThePrompt(String text) {
        alertsPage.answerPrompt(text);
    }

    @Then("the App1 alert result should be {string}")
    public void theAlertResultShouldBe(String expected) {
        Assert.assertEquals(alertsPage.getResult(), expected, "Alert result text");
    }
}
