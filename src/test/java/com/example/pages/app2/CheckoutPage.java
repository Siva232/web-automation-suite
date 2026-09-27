package com.example.pages.app2;

import com.example.pages.BasePage;
import org.openqa.selenium.By;

/** App2 - Checkout module: your information, overview and order confirmation steps. */
public class CheckoutPage extends BasePage {

    private static final By FIRST_NAME = By.id("first-name");
    private static final By LAST_NAME = By.id("last-name");
    private static final By POSTAL_CODE = By.id("postal-code");
    private static final By CONTINUE = By.id("continue");
    private static final By ERROR = By.cssSelector("[data-test='error']");
    private static final By ITEM_TOTAL = By.cssSelector("[data-test='subtotal-label']");
    private static final By FINISH = By.id("finish");
    private static final By COMPLETE_HEADER = By.cssSelector("[data-test='complete-header']");

    public void enterDetails(String firstName, String lastName, String postalCode) {
        type(FIRST_NAME, firstName);
        type(LAST_NAME, lastName);
        type(POSTAL_CODE, postalCode);
        click(CONTINUE);
    }

    public String getError() {
        return getText(ERROR);
    }

    public String getItemTotal() {
        return getText(ITEM_TOTAL);
    }

    public void finish() {
        click(FINISH);
    }

    public String getConfirmation() {
        return getText(COMPLETE_HEADER);
    }
}
