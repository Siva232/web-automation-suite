@app2 @ui @product-details
Feature: App2 - Product details module
  As an App2 shopper
  I want to see the details of a product
  So that I can decide whether to buy it

  Background:
    Given the user is logged in to App2

  @smoke
  Scenario Outline: Product details are shown for "<product>"
    When the user opens the App2 product "<product>"
    Then the App2 product details should show name "<product>" and price "<price>"
    And the App2 product details should show a description

    Examples:
      | product                  | price  |
      | Sauce Labs Backpack      | $29.99 |
      | Sauce Labs Bike Light    | $9.99  |
      | Sauce Labs Fleece Jacket | $49.99 |

  @regression
  Scenario: Product can be added to the cart from the details page
    When the user opens the App2 product "Sauce Labs Bolt T-Shirt"
    And the user adds the product to the App2 cart from the details page
    Then the App2 cart icon should show 1

  @regression
  Scenario: User can go back to the products page
    When the user opens the App2 product "Sauce Labs Onesie"
    And the user goes back to the App2 products page
    Then the App2 products page should be displayed
