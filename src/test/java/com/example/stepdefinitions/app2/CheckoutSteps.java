package com.example.stepdefinitions.app2;

import com.example.pages.app2.CheckoutPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class CheckoutSteps {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @When("the user enters App2 checkout details {string}, {string}, {string}")
    public void theUserEntersCheckoutDetails(String firstName, String lastName, String postalCode) {
        checkoutPage.enterDetails(firstName, lastName, postalCode);
    }

    @Then("the App2 checkout error should be {string}")
    public void theCheckoutErrorShouldBe(String expected) {
        Assert.assertEquals(checkoutPage.getError(), expected, "Checkout error message");
    }

    @Then("the App2 order item total should be {string}")
    public void theItemTotalShouldBe(String expected) {
        Assert.assertEquals(checkoutPage.getItemTotal(), expected, "Item total");
    }

    @When("the user finishes the App2 order")
    public void theUserFinishesTheOrder() {
        checkoutPage.finish();
    }

    @Then("the App2 order confirmation should be {string}")
    public void theConfirmationShouldBe(String expected) {
        Assert.assertEquals(checkoutPage.getConfirmation(), expected, "Order confirmation");
    }
}
