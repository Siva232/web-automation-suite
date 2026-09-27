package com.example.pages.app2;

import com.example.pages.BasePage;
import org.openqa.selenium.By;

/** App2 - Login module ({base.url}/). */
public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("user-name");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.id("login-button");
    private static final By ERROR = By.cssSelector("[data-test='error']");

    public LoginPage openPage() {
        open("/");
        return this;
    }

    public void login(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
    }

    public String getError() {
        return getText(ERROR);
    }

    public boolean isDisplayed() {
        return waitForVisible(LOGIN_BUTTON).isDisplayed();
    }
}
