package com.example.stepdefinitions.app1;

import com.example.pages.app1.FormsPage;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class FormsSteps {

    private final FormsPage formsPage = new FormsPage();

    @Given("the user is on the App1 checkboxes form")
    public void theUserIsOnTheCheckboxesForm() {
        formsPage.openCheckboxes();
    }

    @Given("the user is on the App1 dropdown form")
    public void theUserIsOnTheDropdownForm() {
        formsPage.openDropdown();
    }

    @When("the user checks App1 checkbox {int}")
    public void theUserChecksCheckbox(int number) {
        formsPage.setCheckbox(number, true);
    }

    @When("the user unchecks App1 checkbox {int}")
    public void theUserUnchecksCheckbox(int number) {
        formsPage.setCheckbox(number, false);
    }

    @Then("App1 checkbox {int} should be checked")
    public void checkboxShouldBeChecked(int number) {
        Assert.assertTrue(formsPage.isCheckboxChecked(number), "Checkbox " + number + " is not checked");
    }

    @Then("App1 checkbox {int} should be unchecked")
    public void checkboxShouldBeUnchecked(int number) {
        Assert.assertFalse(formsPage.isCheckboxChecked(number), "Checkbox " + number + " is checked");
    }

    @When("the user selects {string} from the App1 dropdown")
    public void theUserSelectsFromDropdown(String option) {
        formsPage.selectOption(option);
    }

    @Then("the App1 dropdown should show {string}")
    public void theDropdownShouldShow(String expected) {
        Assert.assertEquals(formsPage.getSelectedOption(), expected, "Selected dropdown option");
    }
}
