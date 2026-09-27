package com.example.pages.app1;

import com.example.pages.BasePage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** App1 - Alerts module: JavaScript alert / confirm / prompt ({base.url}/javascript_alerts). */
public class AlertsPage extends BasePage {

    private static final By RESULT = By.id("result");

    public AlertsPage openPage() {
        open("/javascript_alerts");
        return this;
    }

    public void clickButton(String buttonText) {
        click(By.xpath("//button[normalize-space()='" + buttonText + "']"));
    }

    public String getAlertText() {
        return waitForAlert().getText();
    }

    public void acceptAlert() {
        waitForAlert().accept();
    }

    public void dismissAlert() {
        waitForAlert().dismiss();
    }

    public void answerPrompt(String text) {
        Alert alert = waitForAlert();
        alert.sendKeys(text);
        alert.accept();
    }

    public String getResult() {
        return getText(RESULT);
    }

    private Alert waitForAlert() {
        return wait.until(ExpectedConditions.alertIsPresent());
    }
}
