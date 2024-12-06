Feature: All about MBD payment option build request handled by pagopa-gps-mbd-service

  Scenario: successful build payment option request
    When an http POST request is sent to gps-mbd-service with valid request body
    Then the statusCode is 200
    And the response body has the expected values

  Scenario: fail build payment option request for null amount
    When an http POST request is sent to gps-mbd-service with invalid 'amount' request body
    Then the statusCode is 400

  Scenario: fail build payment option request for null debtor name
    When an http POST request is sent to gps-mbd-service with invalid 'debtorName' request body
    Then the statusCode is 400

  Scenario: fail build payment option request for null debtor surname
    When an http POST request is sent to gps-mbd-service with invalid 'debtorSurname' request body
    Then the statusCode is 400

  Scenario: fail build payment option request for null debtor fiscal code
    When an http POST request is sent to gps-mbd-service with invalid 'debtorFiscalCode' request body
    Then the statusCode is 400

  Scenario: fail build payment option request for null debtor email
    When an http POST request is sent to gps-mbd-service with invalid 'debtorEmail' request body
    Then the statusCode is 400

  Scenario: fail build payment option request for empty CI fiscal code
    When an http POST request is sent to gps-mbd-service with invalid 'ciFiscalCode' request body
    Then the statusCode is 400

  Scenario: fail build payment option request for empty debtor residence province
    When an http POST request is sent to gps-mbd-service with invalid 'debtorProvince' request body
    Then the statusCode is 400

  Scenario: fail build payment option request for document hash too short
    When an http POST request is sent to gps-mbd-service with invalid 'documentHash' request body
    Then the statusCode is 400
