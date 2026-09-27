@app1 @ui @alerts
Feature: App1 - Alerts module
  As an App1 user
  I want browser alerts, confirmations and prompts to work
  So that I can respond to messages from the application

  Background:
    Given the user is on the App1 alerts page

  @smoke
  Scenario: User accepts an information alert
    When the user clicks the App1 "Click for JS Alert" button
    Then the App1 browser alert should say "I am a JS Alert"
    When the user accepts the App1 alert
    Then the App1 alert result should be "You successfully clicked an alert"

  @regression
  Scenario: User confirms a confirmation dialog
    When the user clicks the App1 "Click for JS Confirm" button
    And the user accepts the App1 alert
    Then the App1 alert result should be "You clicked: Ok"

  @regression
  Scenario: User cancels a confirmation dialog
    When the user clicks the App1 "Click for JS Confirm" button
    And the user dismisses the App1 alert
    Then the App1 alert result should be "You clicked: Cancel"

  @regression
  Scenario Outline: User answers a prompt with "<text>"
    When the user clicks the App1 "Click for JS Prompt" button
    And the user enters "<text>" in the App1 prompt
    Then the App1 alert result should be "You entered: <text>"

    Examples:
      | text          |
      | QA Automation |
      | 12345         |
