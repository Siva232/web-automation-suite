package com.example.pages.app1;

import com.example.pages.BasePage;
import org.openqa.selenium.By;

/** App1 - Home module ({base.url}/). */
public class HomePage extends BasePage {

    private static final By HEADING = By.cssSelector("h1.heading");
    private static final By SUB_HEADING = By.cssSelector("#content h2");
    private static final By MENU_LINKS = By.cssSelector("#content ul li a");

    public HomePage openPage() {
        open("/");
        return this;
    }

    public String getHeading() {
        return getText(HEADING);
    }

    public String getSubHeading() {
        return getText(SUB_HEADING);
    }

    public int getMenuLinkCount() {
        return getTexts(MENU_LINKS).size();
    }

    public void openMenuLink(String linkText) {
        click(By.linkText(linkText));
    }
}
