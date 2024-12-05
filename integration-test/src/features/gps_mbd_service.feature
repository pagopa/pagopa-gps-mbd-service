Feature: All about MBD payment option build request handled by pagopa-gps-mbd-service

  Scenario: successful build payment option request
    When an http POST request is sent to gps-mbd-service with valid request body
    Then the statusCode is 200
    And the response body has the expected values

  Scenario: fail build payment option request for null amount
    When an http POST request is sent to gps-mbd-service with invalid 'amount' request body
    Then the statusCode is 400

  Scenario: fail build payment option request for empty fiscal code
    When an http POST request is sent to gps-mbd-service with invalid 'fiscalCode' request body
    Then the statusCode is 400

  Scenario: fail build payment option request for empty residence provice
    When an http POST request is sent to gps-mbd-service with invalid 'provincialResidence' request body
    Then the statusCode is 400

  Scenario: fail build payment option request for document hash too short
    When an http POST request is sent to gps-mbd-service with invalid 'documentHash' request body
    Then the statusCode is 400
