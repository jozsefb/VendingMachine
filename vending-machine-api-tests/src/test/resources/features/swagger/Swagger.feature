Feature: Swagger
  As an engineer
  I want to view the microservices Swagger specification
  So that I know the description of each endpoint

  Scenario: Swagger UI endpoint returns 200
    When I GET "/swagger-ui.html"
    Then the response code should be 200

  Scenario: Swagger UI endpoint returns a HTML page
    When I GET "/swagger-ui.html"
    Then the response content-type should be "text/html"
    
  Scenario: Swagger api-docs endpoint returns 200
    When I GET "/v3/api-docs"
    Then the response code should be 200
    
  Scenario: Swagger api-docs endpoint returns JSON
    When I GET "/v3/api-docs"
    Then the response content-type should be "application/json;charset=UTF-8"
