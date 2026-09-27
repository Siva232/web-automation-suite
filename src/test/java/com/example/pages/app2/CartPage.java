package com.example.pages.app2;

import com.example.pages.BasePage;
import org.openqa.selenium.By;

import java.util.List;

/** App2 - Cart module. */
public class CartPage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By ITEM_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By CHECKOUT = By.id("checkout");
    private static final By CONTINUE_SHOPPING = By.id("continue-shopping");

    public String getPageTitle() {
        return getText(TITLE);
    }

    /** Names of the products in the cart; empty list when the cart is empty. */
    public List<String> getItemNames() {
        waitForVisible(CHECKOUT);
        return isPresent(ITEM_NAMES) ? getTexts(ITEM_NAMES) : List.of();
    }

    public void remove(String productName) {
        click(By.xpath("//div[@data-test='inventory-item']"
                + "[.//div[@data-test='inventory-item-name' and normalize-space()='" + productName + "']]//button"));
    }

    public void checkout() {
        click(CHECKOUT);
    }

    public void continueShopping() {
        click(CONTINUE_SHOPPING);
    }
}
