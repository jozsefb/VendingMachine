Feature: Product Management

  Scenario Outline: Upload a product
    Given a logged in "<user>"
    When I set the product details "<product name>"
    And I POST to "/products"
    Then the response code should be <response code>
    Examples:
      | user   | product name | response code |
      | SELLER | product 1    | 201           |
      | SELLER | product 1    | 500           |
      | BUYER  | product 2    | 403           |

    Scenario Outline: Get all products
      Given a logged in "<user>"
      When I GET "/products"
      Then the response code should be 200
      Examples:
        | user   |
        | SELLER |
        | BUYER  |

    Scenario Outline:
      Given a logged in "SELLER"
      And an existing product
      When I try to update the product with "<user type>" "<user>"
      Then the response code should be <response code>
      Examples:
        | user type | user           | response code |
        | SELLER    | same user      | 200           |
        | SELLER    | different user | 403           |
        | BUYER     | different user | 403           |
