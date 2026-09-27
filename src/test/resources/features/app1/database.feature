@app1 @db
Feature: App1 - Database validation
  As a QA engineer
  I want to validate the App1 database
  So that the data behind the application is correct

  # Connection details (db.url / db.username / db.password) come from config.properties (<env>.app1.db.*)

  Background:
    Given the database connection is established

  @smoke
  Scenario: Users table exists with the expected structure
    Then the table "users" should exist
    And the table "users" should have the columns:
      | id         |
      | username   |
      | email      |
      | status     |
      | created_at |

  @regression
  Scenario: Users table contains data
    Then the table "users" should have at least 1 row

  @regression @data-quality
  Scenario Outline: Column "<column>" has no null values
    Then the column "<column>" in table "users" should have no null values

    Examples:
      | column   |
      | username |
      | email    |
      | status   |

  @regression @data-quality
  Scenario Outline: Column "<column>" has unique values
    Then the column "<column>" in table "users" should have no duplicate values

    Examples:
      | column   |
      | username |
      | email    |

  @smoke
  Scenario: Application login user exists and is active
    When the configured application user is looked up in the "users" table
    Then the user record should exist
    And the user record should have:
      | status | ACTIVE |

  @regression @crud
  Scenario: Insert, verify and delete a user record
    When a new user is inserted into the "users" table:
      | username | db_test_user          |
      | email    | db_test_user@test.com |
      | status   | ACTIVE                |
    And the user "db_test_user" is looked up in the "users" table
    Then the user record should exist
    And the user record should have:
      | email  | db_test_user@test.com |
      | status | ACTIVE                |
    When the user "db_test_user" is deleted from the "users" table
    And the user "db_test_user" is looked up in the "users" table
    Then the user record should not exist

  @ui @e2e
  Scenario: Active user in the database can log in to the application
    When the configured application user is looked up in the "users" table
    Then the user record should have:
      | status | ACTIVE |
    Given the user is on the App1 login page
    When the user logs in to App1 with valid credentials
    Then the App1 login message should be "You logged into a secure area!"
