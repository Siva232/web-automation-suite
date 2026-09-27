package com.example.pages.app1;

import com.example.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** App1 - Accounts module: the accounts table ({base.url}/tables). */
public class AccountsPage extends BasePage {

    private static final By HEADERS = By.cssSelector("#table1 thead th");
    private static final By ROWS = By.cssSelector("#table1 tbody tr");

    public AccountsPage openPage() {
        open("/tables");
        return this;
    }

    public List<String> getColumnHeaders() {
        return getTexts(HEADERS);
    }

    public int getAccountCount() {
        return getTexts(ROWS).size();
    }

    public List<String> getColumnValues(String column) {
        int index = columnIndex(column);
        return getTexts(By.cssSelector("#table1 tbody tr td:nth-child(" + (index + 1) + ")"));
    }

    /** Returns the row whose Email matches, as header -> cell text; empty map when not found. */
    public Map<String, String> getAccountByEmail(String email) {
        List<String> headers = getColumnHeaders();
        int emailIndex = headers.indexOf("Email");
        for (WebElement row : driver.findElements(ROWS)) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.get(emailIndex).getText().trim().equals(email)) {
                Map<String, String> account = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    account.put(headers.get(i), cells.get(i).getText().trim());
                }
                return account;
            }
        }
        return Map.of();
    }

    public void sortBy(String column) {
        driver.findElements(HEADERS).get(columnIndex(column)).click();
    }

    private int columnIndex(String column) {
        int index = getColumnHeaders().indexOf(column);
        if (index < 0) {
            throw new IllegalArgumentException("No column '" + column + "' in accounts table");
        }
        return index;
    }
}
