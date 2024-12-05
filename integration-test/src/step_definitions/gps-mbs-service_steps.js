const { Given, When, Then } = require('@cucumber/cucumber')
const assert = require("assert");
const { post } = require("./support/common");
const { buildRequestBody } = require("./support/util");

const gpsMbdServiceHost = process.env.GPS_MBD_HOST;
const poDescription = process.env.MBD_PO_DESCRIPTION;
const transferRemittanceInformation = process.env.MBD_TRANSFER_REMITTANCE_INFORMATION;

this.body = null;
this.responseToCheck = null;

When('an http POST request is sent to gps-mbd-service with valid request body', async () => {
  this.body = buildRequestBody(16, "00000000000", "PR", "1trA5qyjSZNwiwtGG46dyjRpL16TFgGCFvnfFzQrFHbB");
  this.responseToCheck = await post(gpsMbdServiceHost + "/mbd/paymentOption", this.body);
});

Then('the statusCode is {int}', function (statusCode) {
  assert.strictEqual(this.responseToCheck.status, statusCode);
});

Then('the response body has the expected values', function () {
  console.log(this.responseToCheck.data.paymentOption);
  assert.strictEqual(this.responseToCheck.data.paymentOption.length, 1);
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].amount, this.body.properties.amount);
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].description, poDescription);
  assert.notStrictEqual(this.responseToCheck.data.paymentOption[0].dueDate, undefined);
  assert.notStrictEqual(this.responseToCheck.data.paymentOption[0].retentionDate, undefined);
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].isPartialPayment, false);
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].transfer.length, 1);
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].transfer[0].organizationFiscalCode, this.body.properties.fiscalCode);
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].transfer[0].idTransfer, "1");
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].transfer[0].amount, this.body.properties.amount);
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].transfer[0].remittanceInformation, transferRemittanceInformation);
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].transfer[0].stamp.hashDocument, this.body.properties.documentHash);
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].transfer[0].stamp.stampType, "st");
  assert.strictEqual(this.responseToCheck.data.paymentOption[0].transfer[0].stamp.provincialResidence, this.body.properties.provincialResidence);
});
