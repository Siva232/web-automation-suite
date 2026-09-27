@app1 @ui @home
Feature: App1 - Home page module
  As an App1 user
  I want the home page to show the available modules
  So that I can navigate to them

  Background:
    Given the user is on the App1 home page

  @smoke
  Scenario: Home page displays the welcome heading
    Then the App1 home page heading should be "Welcome to the-internet"
    And the App1 home page sub-heading should be "Available Examples"

  @regression
  Scenario: Home page lists the application modules
    Then the App1 home page should list at least 40 modules

  @regression
  Scenario Outline: Navigate from the home page to "<module>"
    When the user opens the "<module>" link on the App1 home page
    Then the App1 page URL should end with "<path>"

    Examples:
      | module               | path               |
      | Form Authentication  | /login             |
      | Sortable Data Tables | /tables            |
      | Checkboxes           | /checkboxes        |
      | Dropdown             | /dropdown          |
      | JavaScript Alerts    | /javascript_alerts |
