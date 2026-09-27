@app2 @ui @checkout
Feature: App2 - Checkout module
  As an App2 shopper
  I want to check out the products in my cart
  So that I can place an order

  Background:
    Given the user is logged in to App2
    And the user adds "Sauce Labs Backpack" to the App2 cart
    And the user opens the App2 cart
    And the user proceeds to App2 checkout

  @smoke @e2e
  Scenario: User places an order successfully
    When the user enters App2 checkout details "John", "Smith", "560001"
    Then the App2 order item total should be "Item total: $29.99"
    When the user finishes the App2 order
    Then the App2 order confirmation should be "Thank you for your order!"

  @regression @negative
  Scenario Outline: Checkout requires <field>
    When the user enters App2 checkout details "<first>", "<last>", "<postal>"
    Then the App2 checkout error should be "<error>"

    Examples:
      | field       | first | last  | postal | error                          |
      | first name  |       | Smith | 560001 | Error: First Name is required  |
      | last name   | John  |       | 560001 | Error: Last Name is required   |
      | postal code | John  | Smith |        | Error: Postal Code is required |
