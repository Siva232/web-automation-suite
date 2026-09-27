package com.example.pages.app1;

import com.example.pages.BasePage;
import org.openqa.selenium.By;

/** App1 - Login module ({base.url}/login). */
public class LoginPage extends BasePage {

    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");
    private static final By LOGIN_BUTTON = By.cssSelector("button[type='submit']");
    private static final By FLASH_MESSAGE = By.id("flash");
    private static final By LOGOUT_BUTTON = By.cssSelector("a[href='/logout']");

    public LoginPage openPage() {
        open("/login");
        return this;
    }

    public void login(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
    }

    public String getFlashMessage() {
        return getText(FLASH_MESSAGE);
    }

    public boolean isLogoutVisible() {
        return isPresent(LOGOUT_BUTTON);
    }

    public void logout() {
        click(LOGOUT_BUTTON);
    }
}
