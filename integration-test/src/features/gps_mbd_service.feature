Feature: All about MBD payment option build request handled by pagopa-gps-mbd-service

  Scenario: successful build payment option request
    When an http POST request is sent to gps-mbd-service with valid request body
    Then the statusCode is 200
    And the response body has the expected values
