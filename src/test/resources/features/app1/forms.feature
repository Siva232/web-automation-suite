@app1 @ui @forms
Feature: App1 - Forms module
  As an App1 user
  I want to use checkboxes and dropdowns
  So that I can fill in forms

  @smoke
  Scenario: Checkboxes show their default state
    Given the user is on the App1 checkboxes form
    Then App1 checkbox 1 should be unchecked
    And App1 checkbox 2 should be checked

  @regression
  Scenario: User can check and uncheck checkboxes
    Given the user is on the App1 checkboxes form
    When the user checks App1 checkbox 1
    And the user unchecks App1 checkbox 2
    Then App1 checkbox 1 should be checked
    And App1 checkbox 2 should be unchecked

  @smoke
  Scenario: Dropdown shows the placeholder by default
    Given the user is on the App1 dropdown form
    Then the App1 dropdown should show "Please select an option"

  @regression
  Scenario Outline: User can select "<option>" from the dropdown
    Given the user is on the App1 dropdown form
    When the user selects "<option>" from the App1 dropdown
    Then the App1 dropdown should show "<option>"

    Examples:
      | option   |
      | Option 1 |
      | Option 2 |
