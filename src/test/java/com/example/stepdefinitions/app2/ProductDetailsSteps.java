package com.example.stepdefinitions.app2;

import com.example.pages.app2.ProductDetailsPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class ProductDetailsSteps {

    private final ProductDetailsPage detailsPage = new ProductDetailsPage();

    @Then("the App2 product details should show name {string} and price {string}")
    public void theDetailsShouldShow(String name, String price) {
        Assert.assertEquals(detailsPage.getName(), name, "Product name");
        Assert.assertEquals(detailsPage.getPrice(), price, "Product price");
    }

    @Then("the App2 product details should show a description")
    public void theDetailsShouldShowADescription() {
        Assert.assertFalse(detailsPage.getDescription().isBlank(), "Product description is empty");
    }

    @When("the user adds the product to the App2 cart from the details page")
    public void theUserAddsFromDetails() {
        detailsPage.clickAddOrRemove();
    }

    @When("the user goes back to the App2 products page")
    public void theUserGoesBack() {
        detailsPage.backToProducts();
    }
}
