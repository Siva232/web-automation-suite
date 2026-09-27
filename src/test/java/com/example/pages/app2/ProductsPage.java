package com.example.pages.app2;

import com.example.pages.BasePage;
import org.openqa.selenium.By;

import java.util.List;

/** App2 - Products (home) module, plus the header cart icon and side menu shown on every page. */
public class ProductsPage extends BasePage {

    private static final By TITLE = By.cssSelector("[data-test='title']");
    private static final By PRODUCT_NAMES = By.cssSelector("[data-test='inventory-item-name']");
    private static final By PRODUCT_PRICES = By.cssSelector("[data-test='inventory-item-price']");
    private static final By SORT = By.cssSelector("[data-test='product-sort-container']");
    private static final By CART_BADGE = By.cssSelector("[data-test='shopping-cart-badge']");
    private static final By CART_LINK = By.cssSelector("[data-test='shopping-cart-link']");
    private static final By MENU_BUTTON = By.id("react-burger-menu-btn");
    private static final By LOGOUT_LINK = By.id("logout_sidebar_link");

    public String getPageTitle() {
        return getText(TITLE);
    }

    public List<String> getProductNames() {
        return getTexts(PRODUCT_NAMES);
    }

    public List<Double> getProductPrices() {
        return getTexts(PRODUCT_PRICES).stream().map(p -> Double.parseDouble(p.replace("$", ""))).toList();
    }

    public void sortBy(String option) {
        selectByVisibleText(SORT, option);
    }

    public void addToCart(String productName) {
        click(productButton(productName));
    }

    public void removeFromCart(String productName) {
        click(productButton(productName));
    }

    public String getProductButtonText(String productName) {
        return getText(productButton(productName));
    }

    public void openProduct(String productName) {
        click(By.xpath("//div[@data-test='inventory-item-name' and normalize-space()='" + productName + "']"));
    }

    /** Number on the cart icon; 0 when the badge is not shown. */
    public int getCartCount() {
        return isPresent(CART_BADGE) ? Integer.parseInt(getText(CART_BADGE)) : 0;
    }

    public void openCart() {
        click(CART_LINK);
    }

    public void logout() {
        click(MENU_BUTTON);
        click(LOGOUT_LINK);
    }

    /** The Add to cart / Remove button inside the product card with the given name. */
    private By productButton(String productName) {
        return By.xpath("//div[@data-test='inventory-item']"
                + "[.//div[@data-test='inventory-item-name' and normalize-space()='" + productName + "']]//button");
    }
}
