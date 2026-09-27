@app2 @ui @products
Feature: App2 - Products (Home page) module
  As an App2 shopper
  I want to browse the product catalogue
  So that I can find what I want to buy

  Background:
    Given the user is logged in to App2

  @smoke
  Scenario: Home page lists all products
    Then the App2 products page should be displayed
    And the App2 products page should list 6 products

  @regression
  Scenario Outline: Products can be sorted by "<sort option>"
    When the user sorts App2 products by "<sort option>"
    Then the first App2 product should be "<first product>"

    Examples:
      | sort option         | first product                     |
      | Name (A to Z)       | Sauce Labs Backpack               |
      | Name (Z to A)       | Test.allTheThings() T-Shirt (Red) |
      | Price (low to high) | Sauce Labs Onesie                 |
      | Price (high to low) | Sauce Labs Fleece Jacket          |

  @regression
  Scenario: Prices are ordered when sorting by price
    When the user sorts App2 products by "Price (low to high)"
    Then the App2 product prices should be in ascending order
    When the user sorts App2 products by "Price (high to low)"
    Then the App2 product prices should be in descending order

  @regression
  Scenario: Adding and removing a product updates the cart icon
    When the user adds "Sauce Labs Backpack" to the App2 cart
    Then the App2 button for "Sauce Labs Backpack" should read "Remove"
    And the App2 cart icon should show 1
    When the user removes "Sauce Labs Backpack" from the App2 products page
    Then the App2 button for "Sauce Labs Backpack" should read "Add to cart"
    And the App2 cart icon should show 0
