const { After, When, Then } = require('@cucumber/cucumber')
const assert = require("assert");
const { post } = require("./support/common");
const { buildRequestBody } = require("./support/util");

const gpsMbdServiceHost = process.env.GPS_MBD_HOST;
const poDescription = process.env.MBD_PO_DESCRIPTION;
const transferRemittanceInformation = process.env.MBD_TRANSFER_REMITTANCE_INFORMATION;

let body = null;
let responseToCheck = null;

After(async function () {
  body = null;
  responseToCheck = null;
});

When('an http POST request is sent to gps-mbd-service with valid request body', async () => {
  body = buildRequestBody(16, "00000000000", "PR", "1trA5qyjSZNwiwtGG46dyjRpL16TFgGCFvnfFzQrFHbB");
  responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
});

Then('the statusCode is {int}', function (statusCode) {
  assert.strictEqual(responseToCheck.status, statusCode);
});

Then('the response body has the expected values', function () {
  assert.strictEqual(responseToCheck.data.paymentOption.length, 1);
  assert.strictEqual(responseToCheck.data.paymentOption[0].amount, body.properties.amount);
  assert.strictEqual(responseToCheck.data.paymentOption[0].description, poDescription);
  assert.notStrictEqual(responseToCheck.data.paymentOption[0].dueDate, undefined);
  assert.notStrictEqual(responseToCheck.data.paymentOption[0].retentionDate, undefined);
  assert.strictEqual(responseToCheck.data.paymentOption[0].isPartialPayment, false);
  assert.strictEqual(responseToCheck.data.paymentOption[0].organizationFiscalCode, body.properties.ciFiscalCode);
  assert.strictEqual(responseToCheck.data.paymentOption[0].transfer.length, 1);
  assert.strictEqual(responseToCheck.data.paymentOption[0].transfer[0].organizationFiscalCode, body.properties.ciFiscalCode);
  assert.strictEqual(responseToCheck.data.paymentOption[0].transfer[0].idTransfer, "1");
  assert.strictEqual(responseToCheck.data.paymentOption[0].transfer[0].amount, body.properties.amount);
  assert.strictEqual(responseToCheck.data.paymentOption[0].transfer[0].remittanceInformation, transferRemittanceInformation);
  assert.strictEqual(responseToCheck.data.paymentOption[0].transfer[0].stamp.hashDocument, body.properties.documentHash);
  assert.strictEqual(responseToCheck.data.paymentOption[0].transfer[0].stamp.stampType, "01");
  assert.strictEqual(responseToCheck.data.paymentOption[0].transfer[0].stamp.provincialResidence, body.properties.debtorProvince);
});

When('an http POST request is sent to gps-mbd-service with invalid {string} request body', async (invalidParam) => {
  body = buildRequestBody(16, "00000000000", "PR", "1trA5qyjSZNwiwtGG46dyjRpL16TFgGCFvnfFzQrFHbB");

  switch (invalidParam) {
    case "amount": body.properties.amount = null; break;
    case "debtorName": body.properties.debtorName = ""; break;
    case "debtorSurname": body.properties.debtorSurname = ""; break;
    case "debtorEmail": body.properties.debtorEmail = ""; break;
    case "debtorFiscalCode": body.properties.debtorFiscalCode = ""; break;
    case "ciFiscalCode": body.properties.ciFiscalCode = ""; break;
    case "debtorProvince": body.properties.debtorProvince = ""; break;
    case "documentHash": body.properties.documentHash = "tooShortHash"; break;
  }

  responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", body);
})
