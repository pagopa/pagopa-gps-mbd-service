Feature: GPS MBD Service Integration Tests

  Scenario: Create debt postion successfully for Physical Person
    When an http POST request is sent to gps-mbd-service for physical person with fiscal code "RSSMRA85T10H501Z", name "Mario" and surname "Rossi"
    Then the statusCode is 201

  Scenario: Create debt postion successfully for Legal Entity with null name
    When an http POST request is sent to gps-mbd-service for legal entity with VAT "12345678901" and surname "Acme S.r.l."
    Then the statusCode is 201

  Scenario: Fail creating debt postion for Physical Person when debtorName is missing
    When an http POST request is sent to gps-mbd-service for physical person with fiscal code "RSSMRA85T10H501Z" and missing name
    Then the statusCode is 400
    
  Scenario: Fail creating debt postion for Legal Entity when debtorName is missing
    When an http POST request is sent to gps-mbd-service for legal entity with VAT "12345678901" and missing surname
    Then the statusCode is 400
  
  Scenario: Fail creating debt postion with invalid debtor fiscal code format
    When an http POST request is sent to gps-mbd-service with debtor fiscal code "INVALID_CF_123"
    Then the statusCode is 400

  Scenario: Fail creating debt postion with empty debtor email
    When an http POST request is sent to gps-mbd-service with empty email
    Then the statusCode is 400

  Scenario: Fail creating debt postion with documentHash shorter than 44 characters
    When an http POST request is sent to gps-mbd-service with invalid documentHash "TOO_SHORT_HASH="
    Then the statusCode is 400

  Scenario: Fail creating debt postion with documentHash not in Base64 format
    When an http POST request is sent to gps-mbd-service with non base64 documentHash "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!AAAAAAAAAAA="
    Then the statusCode is 400

  Scenario: Fail creating debt postion with null amount
    When an http POST request is sent to gps-mbd-service with null amount
    Then the statusCode is 400

  Scenario: Fail creating debt postion with empty debtor province
    When an http POST request is sent to gps-mbd-service with empty province
    Then the statusCode is 400

  Scenario: Fail creating debt postion when Creditor Institution (ciFiscalCode) is not found in cache
    When an http POST request is sent to gps-mbd-service for unknown creditor institution "00000000321"
    Then the statusCode is 404