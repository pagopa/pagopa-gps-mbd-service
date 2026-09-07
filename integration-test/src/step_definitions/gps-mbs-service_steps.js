const {After, When, Then, setDefaultTimeout} = require('@cucumber/cucumber');
const assert = require('assert');
const {post} = require('./support/common');
const {buildRequestBody} = require('./support/util');

setDefaultTimeout(15 * 1000);

const gpsMbdServiceHost = process.env.GPS_MBD_HOST;

let body = null;
let responseToCheck = null;

After(async function () {
    body = null;
    responseToCheck = null;
});

When('an http POST request is sent to gps-mbd-service for physical person with fiscal code {string} and fullName {string}', async function (fiscalCode, fullName) {
    body = buildRequestBody(100, fiscalCode, "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", fullName);
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service for legal entity with VAT {string} and fullName {string}', async function (vat, fullName) {
    body = buildRequestBody(100, vat, "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", fullName);
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service for physical person with fiscal code {string} and missing fullName', async function (fiscalCode) {
    body = buildRequestBody(100, fiscalCode, "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", null);
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service for legal entity with VAT {string} and missing fullName', async function (vat) {
    body = buildRequestBody(100, vat, "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", null);
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

Then('the statusCode is {int}', function (expectedStatusCode) {
    assert.ok(responseToCheck, "Nessuna risposta ricevuta dal servizio.");
    assert.strictEqual(responseToCheck.status, 200, `Expected HTTP status 200 OK but got ${responseToCheck.status}`);

    const responseData = responseToCheck.data || "";

    if (expectedStatusCode === 201) {
        assert.ok(
            responseData.includes('<outcome>OK</outcome>'),
            `Expected outcome OK in XML response, but got:\n${responseData}`
        );
    } else if (expectedStatusCode === 400 || expectedStatusCode === 404) {
        assert.ok(
            responseData.includes('<outcome>KO</outcome>'),
            `Expected outcome KO in XML response for status ${expectedStatusCode}, but got:\n${responseData}`
        );
    }
});

Then('the response outcome is {string}', function (expectedOutcome) {
    assert.ok(responseToCheck && responseToCheck.data, "Body della risposta assente.");
    assert.ok(
        responseToCheck.data.includes(`<outcome>${expectedOutcome}</outcome>`),
        `Expected outcome ${expectedOutcome} in XML response, but got:\n${responseToCheck.data}`
    );
});

Then('the response faultCode is {string}', function (expectedFaultCode) {
    assert.ok(responseToCheck && responseToCheck.data, "Body della risposta assente.");
    assert.ok(
        responseToCheck.data.includes(`<faultCode>${expectedFaultCode}</faultCode>`),
        `Expected faultCode ${expectedFaultCode} in XML response, but got:\n${responseToCheck.data}`
    );
});

When('an http POST request is sent to gps-mbd-service with debtor fiscal code {string}', async function (invalidFiscalCode) {
    body = buildRequestBody(100, invalidFiscalCode, "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", "Mario Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with empty email', async function () {
    body = buildRequestBody(100, "RSSMRA85T10H501Z", "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", "Mario Rossi", "77777777777", "");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with invalid documentHash {string}', async function (invalidHash) {
    body = buildRequestBody(100, "RSSMRA85T10H501Z", "MI", invalidHash, "Mario Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with non base64 documentHash {string}', async function (nonBase64Hash) {
    body = buildRequestBody(100, "RSSMRA85T10H501Z", "MI", nonBase64Hash, "Mario Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with null amount', async function () {
    body = buildRequestBody(null, "RSSMRA85T10H501Z", "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", "Mario Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with empty province', async function () {
    body = buildRequestBody(100, "RSSMRA85T10H501Z", "", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", "Mario Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service for unknown creditor institution {string}', async function (unknownCiFiscalCode) {
    body = buildRequestBody(100, "RSSMRA85T10H501Z", "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", "Mario Rossi", unknownCiFiscalCode);
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});