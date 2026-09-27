@app2 @ui @cart
Feature: App2 - Cart module
  As an App2 shopper
  I want to manage the products in my cart
  So that I only buy what I need

  Background:
    Given the user is logged in to App2

  @smoke
  Scenario: Cart shows the products that were added
    When the user adds "Sauce Labs Backpack" to the App2 cart
    And the user adds "Sauce Labs Bike Light" to the App2 cart
    Then the App2 cart icon should show 2
    When the user opens the App2 cart
    Then the App2 cart should contain:
      | Sauce Labs Backpack   |
      | Sauce Labs Bike Light |

  @regression
  Scenario: Product can be removed from the cart
    When the user adds "Sauce Labs Backpack" to the App2 cart
    And the user adds "Sauce Labs Bike Light" to the App2 cart
    And the user opens the App2 cart
    And the user removes "Sauce Labs Backpack" from the App2 cart
    Then the App2 cart should contain:
      | Sauce Labs Bike Light |
    And the App2 cart icon should show 1

  @regression
  Scenario: Cart is empty when nothing was added
    When the user opens the App2 cart
    Then the App2 cart should be empty
    And the App2 cart icon should show 0

  @regression
  Scenario: User can continue shopping from the cart
    When the user opens the App2 cart
    And the user continues shopping in App2
    Then the App2 products page should be displayed
