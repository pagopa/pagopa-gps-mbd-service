import { postToGPSMBDService } from './modules/client.js';
import { check } from 'k6';
import { SharedArray } from 'k6/data';

export const options = JSON.parse(open(__ENV.TEST_TYPE));

// read configuration
// note: SharedArray can currently only be constructed inside init code
// according to https://k6.io/docs/javascript-api/k6-data/sharedarray
const varsArray = new SharedArray('vars', function () {
  return JSON.parse(open(`./${__ENV.VARS}`)).environment;
});
// workaround to use shared array (only array should be used)
export const ENV_VARS = varsArray[0];
const gpsMbdHost = ENV_VARS.gpsMbdHost;

export function setup() {
  // Before All
  // setup code (once)
  // The setup code runs, setting up the test environment (optional) and generating data
  // used to reuse code for the same VU

  // precondition is moved to default fn because in this stage
  // __VU is always 0 and cannot be used to create env properly
}

function precondition() {
  // no pre conditions
}

function postcondition() {
  // Delete the new entity created
}

export default function () {
  let response = postToGPSMBDService(`${gpsMbdHost}/mbd/paymentOption`, "00000000000");
  console.info(`GPS MBD Service buildMbdPaymentOption status ${response.status}`);

  let responseBody = JSON.parse(response.body);

  if (response.status != 200) {
    console.log("ERROR: ", responseBody);
  }
  check(response, {
    'GPS MBD Service buildMbdPaymentOption status is 200': () => response.status === 200,
    'GPS MBD Service buildMbdPaymentOption body has one of payment option': () =>
      Boolean(responseBody && responseBody.length && responseBody.length == 1),
    'GPS MBD Service buildMbdPaymentOption the payment option has the expected CI fiscal code': () => responseBody[0].transfer[0].organizationFiscalCode === "00000000000"
  });

  postcondition();
}

export function teardown(data) {
  // After All
  // teardown code
}
