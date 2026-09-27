@app1 @ui @accounts
Feature: App1 - Accounts page module
  As an App1 user
  I want to view customer accounts
  So that I can check account details and amounts due

  Background:
    Given the user is on the App1 accounts page

  @smoke
  Scenario: Accounts table shows the expected columns
    Then the App1 accounts table should have the columns:
      | Last Name  |
      | First Name |
      | Email      |
      | Due        |
      | Web Site   |
      | Action     |

  @regression
  Scenario: Accounts table lists all accounts
    Then the App1 accounts table should list 4 accounts

  @regression
  Scenario Outline: Account details are correct for "<email>"
    Then the App1 account with email "<email>" should have:
      | Last Name  | <last>  |
      | First Name | <first> |
      | Due        | <due>   |

    Examples:
      | email                 | last   | first | due     |
      | jsmith@gmail.com      | Smith  | John  | $50.00  |
      | fbach@yahoo.com       | Bach   | Frank | $51.00  |
      | jdoe@hotmail.com      | Doe    | Jason | $100.00 |
      | tconway@earthlink.net | Conway | Tim   | $50.00  |

  @regression
  Scenario: Accounts can be sorted by last name
    When the user sorts the App1 accounts by "Last Name"
    Then the App1 "Last Name" column should read:
      | Bach   |
      | Conway |
      | Doe    |
      | Smith  |
