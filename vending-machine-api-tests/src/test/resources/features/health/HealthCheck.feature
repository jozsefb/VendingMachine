Feature: vending-machine-api is UP

  Scenario: vending-machine-api health check returns an UP response
    When I GET "/actuator/health"
    Then the response code should be 200 with "status": "UP"
