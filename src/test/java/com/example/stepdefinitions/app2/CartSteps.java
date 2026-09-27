package com.example.stepdefinitions.app2;

import com.example.pages.app2.CartPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.List;

public class CartSteps {

    private final CartPage cartPage = new CartPage();

    @Then("the App2 cart should contain:")
    public void theCartShouldContain(List<String> expected) {
        Assert.assertEquals(cartPage.getItemNames(), expected, "Products in cart");
    }

    @Then("the App2 cart should be empty")
    public void theCartShouldBeEmpty() {
        Assert.assertTrue(cartPage.getItemNames().isEmpty(), "Cart is not empty: " + cartPage.getItemNames());
    }

    @When("the user removes {string} from the App2 cart")
    public void theUserRemovesFromCart(String product) {
        cartPage.remove(product);
    }

    @When("the user continues shopping in App2")
    public void theUserContinuesShopping() {
        cartPage.continueShopping();
    }

    @When("the user proceeds to App2 checkout")
    public void theUserProceedsToCheckout() {
        cartPage.checkout();
    }
}
