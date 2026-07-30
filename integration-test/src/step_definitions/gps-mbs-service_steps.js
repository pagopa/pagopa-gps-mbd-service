const { After, When, Then } = require('@cucumber/cucumber');
const assert = require('assert');
const { post } = require('./support/common');
const { buildRequestBody } = require('./support/util');

const gpsMbdServiceHost = process.env.GPS_MBD_HOST;

let body = null;
let responseToCheck = null;

After(async function () {
    body = null;
    responseToCheck = null;
});

When('an http POST request is sent to gps-mbd-service for physical person with fiscal code {string}, name {string} and surname {string}', async function (fiscalCode, name, surname) {
    body = buildRequestBody(100, fiscalCode, "MI", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", name, surname);
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service for legal entity with VAT {string} and surname {string}', async function (vat, surname) {
    body = buildRequestBody(100, vat, "MI", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", null, surname);
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service for physical person with fiscal code {string} and missing name', async function (fiscalCode) {
    body = buildRequestBody(100, fiscalCode, "MI", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", null, "Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

Then('the statusCode is {int}', function (statusCode) {
    assert.strictEqual(responseToCheck.status, statusCode);
});

When('an http POST request is sent to gps-mbd-service for legal entity with VAT {string} and missing surname', async function (vat) {
   const body = buildRequestBody(100, vat, "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", null, null);
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with debtor fiscal code {string}', async function (invalidFiscalCode) {
    const body = buildRequestBody(100, invalidFiscalCode, "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", "Mario", "Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with empty email', async function () {
    const body = buildRequestBody(100, "RSSMRA85T10H501Z", "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", "Mario", "Rossi");
    body.properties.debtorEmail = "";
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with invalid documentHash {string}', async function (invalidHash) {
    const body = buildRequestBody(100, "RSSMRA85T10H501Z", "MI", invalidHash, "Mario", "Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with non base64 documentHash {string}', async function (nonBase64Hash) {
    const body = buildRequestBody(100, "RSSMRA85T10H501Z", "MI", nonBase64Hash, "Mario", "Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with null amount', async function () {
    const body = buildRequestBody(null, "RSSMRA85T10H501Z", "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", "Mario", "Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service with empty province', async function () {
    const body = buildRequestBody(100, "RSSMRA85T10H501Z", "", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", "Mario", "Rossi");
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

When('an http POST request is sent to gps-mbd-service for unknown creditor institution {string}', async function (unknownCiFiscalCode) {
    const body = buildRequestBody(100, "RSSMRA85T10H501Z", "MI", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", "Mario", "Rossi", unknownCiFiscalCode);
    body.properties.ciFiscalCode = unknownCiFiscalCode;
    responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});