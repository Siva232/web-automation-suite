package com.example.pages.app2;

import com.example.pages.BasePage;
import org.openqa.selenium.By;

/** App2 - Product details module. */
public class ProductDetailsPage extends BasePage {

    private static final By NAME = By.cssSelector("[data-test='inventory-item-name']");
    private static final By PRICE = By.cssSelector("[data-test='inventory-item-price']");
    private static final By DESCRIPTION = By.cssSelector("[data-test='inventory-item-desc']");
    private static final By ADD_OR_REMOVE = By.cssSelector("#add-to-cart, #remove");
    private static final By BACK = By.id("back-to-products");

    public String getName() {
        return getText(NAME);
    }

    public String getPrice() {
        return getText(PRICE);
    }

    public String getDescription() {
        return getText(DESCRIPTION);
    }

    public void clickAddOrRemove() {
        click(ADD_OR_REMOVE);
    }

    public void backToProducts() {
        click(BACK);
    }
}
