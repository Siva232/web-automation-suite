package com.example.pages.app1;

import com.example.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/** App1 - Forms module: checkboxes ({base.url}/checkboxes) and dropdown ({base.url}/dropdown). */
public class FormsPage extends BasePage {

    private static final By CHECKBOXES = By.cssSelector("#checkboxes input[type='checkbox']");
    private static final By DROPDOWN = By.id("dropdown");

    public FormsPage openCheckboxes() {
        open("/checkboxes");
        return this;
    }

    public FormsPage openDropdown() {
        open("/dropdown");
        return this;
    }

    /** Checkbox number is 1-based, as shown on the page. */
    public boolean isCheckboxChecked(int number) {
        return checkbox(number).isSelected();
    }

    public void setCheckbox(int number, boolean checked) {
        WebElement checkbox = checkbox(number);
        if (checkbox.isSelected() != checked) {
            checkbox.click();
        }
    }

    public void selectOption(String option) {
        selectByVisibleText(DROPDOWN, option);
    }

    public String getSelectedOption() {
        return getSelectedOption(DROPDOWN);
    }

    private WebElement checkbox(int number) {
        waitForVisible(CHECKBOXES);
        return driver.findElements(CHECKBOXES).get(number - 1);
    }
}
