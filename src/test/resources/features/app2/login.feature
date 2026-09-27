@app2 @ui @login
Feature: App2 - Login module
  As an App2 shopper
  I want to log in to App2
  So that I can browse and buy products

  # URL, username and password come from config.properties (<env>.app2.*)

  Background:
    Given the user is on the App2 login page

  @smoke @positive
  Scenario: Successful login with valid credentials
    When the user logs in to App2 with valid credentials
    Then the App2 products page should be displayed

  @regression @negative
  Scenario: Locked out user cannot log in
    When the user logs in to App2 as the locked out user
    Then the App2 login error should be "Epic sadface: Sorry, this user has been locked out."

  @regression @negative
  Scenario Outline: Login fails with <case>
    When the user logs in to App2 with username "<username>" and password "<password>"
    Then the App2 login error should be "<error>"

    Examples:
      | case             | username      | password     | error                                                                     |
      | a wrong password | standard_user | wrong_pass   | Epic sadface: Username and password do not match any user in this service |
      | no username      |               | secret_sauce | Epic sadface: Username is required                                        |
      | no password      | standard_user |              | Epic sadface: Password is required                                        |

  @regression
  Scenario: User can log out
    When the user logs in to App2 with valid credentials
    And the user logs out of App2
    Then the App2 login page should be displayed
