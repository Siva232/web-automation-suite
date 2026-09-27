package com.example.stepdefinitions.app2;

import com.example.pages.app2.ProductsPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.Comparator;
import java.util.List;

public class ProductsSteps {

    private final ProductsPage productsPage = new ProductsPage();

    @Then("the App2 products page should be displayed")
    public void theProductsPageShouldBeDisplayed() {
        Assert.assertEquals(productsPage.getPageTitle(), "Products", "App2 page title");
    }

    @Then("the App2 products page should list {int} products")
    public void theProductsPageShouldList(int expected) {
        Assert.assertEquals(productsPage.getProductNames().size(), expected, "Number of products");
    }

    @When("the user sorts App2 products by {string}")
    public void theUserSortsProductsBy(String option) {
        productsPage.sortBy(option);
    }

    @Then("the first App2 product should be {string}")
    public void theFirstProductShouldBe(String expected) {
        Assert.assertEquals(productsPage.getProductNames().get(0), expected, "First product");
    }

    @Then("the App2 product prices should be in ascending order")
    public void thePricesShouldBeAscending() {
        List<Double> prices = productsPage.getProductPrices();
        Assert.assertEquals(prices, prices.stream().sorted().toList(), "Prices are not in ascending order");
    }

    @Then("the App2 product prices should be in descending order")
    public void thePricesShouldBeDescending() {
        List<Double> prices = productsPage.getProductPrices();
        Assert.assertEquals(prices, prices.stream().sorted(Comparator.reverseOrder()).toList(),
                "Prices are not in descending order");
    }

    @When("the user adds {string} to the App2 cart")
    public void theUserAddsToCart(String product) {
        productsPage.addToCart(product);
    }

    @When("the user removes {string} from the App2 products page")
    public void theUserRemovesFromProductsPage(String product) {
        productsPage.removeFromCart(product);
    }

    @Then("the App2 button for {string} should read {string}")
    public void theButtonShouldRead(String product, String expected) {
        Assert.assertEquals(productsPage.getProductButtonText(product), expected, "Button for " + product);
    }

    @Then("the App2 cart icon should show {int}")
    public void theCartIconShouldShow(int expected) {
        Assert.assertEquals(productsPage.getCartCount(), expected, "Cart icon count");
    }

    @When("the user opens the App2 product {string}")
    public void theUserOpensProduct(String product) {
        productsPage.openProduct(product);
    }

    @When("the user opens the App2 cart")
    public void theUserOpensTheCart() {
        productsPage.openCart();
    }

    @When("the user logs out of App2")
    public void theUserLogsOut() {
        productsPage.logout();
    }
}
