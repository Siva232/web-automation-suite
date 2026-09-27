@app1 @ui @login
Feature: App1 - Login module
  As a registered App1 user
  I want to log in to App1
  So that I can access the secure area

  # URL, username and password come from config.properties (<env>.app1.*)

  Background:
    Given the user is on the App1 login page

  @smoke @positive
  Scenario: Successful login with valid credentials
    When the user logs in to App1 with valid credentials
    Then the App1 login message should be "You logged into a secure area!"
    And the App1 logout button should be displayed

  @regression @negative
  Scenario: Login fails with an invalid username
    When the user logs in to App1 with username "wronguser" and a valid password
    Then the App1 login message should be "Your username is invalid!"

  @regression @negative
  Scenario: Login fails with an invalid password
    When the user logs in to App1 with a valid username and password "wrongpassword"
    Then the App1 login message should be "Your password is invalid!"

  @regression
  Scenario: User can log out after logging in
    When the user logs in to App1 with valid credentials
    And the user logs out of App1
    Then the App1 login message should be "You logged out of the secure area!"
