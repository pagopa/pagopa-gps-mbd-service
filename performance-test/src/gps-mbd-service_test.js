import { check, sleep } from 'k6';
import {SharedArray} from 'k6/data';
import { createPaymentOption } from './modules/client.js';

const varsArray = new SharedArray('vars', function () {
    return JSON.parse(open(`${__ENV.VARS}`)).environment;
});

// workaround to use shared array (only array should be used)
const vars = varsArray[0];

let testType = __ENV.TEST_TYPE || 'smoke';
testType = testType.replace(/^.*[\/\\]/, '').replace(/\.json$/, '');

const typeConfig = JSON.parse(open(`./test-types/${testType}.json`));

export const options = typeConfig;

export default function () {
  const baseUrl = vars.baseUrl || vars.gps_mbd_service_host;
  const subkey = __ENV.API_SUBSCRIPTION_KEY;

  const response = createPaymentOption(baseUrl, subkey);

  check(response, {
    'status is 201': (r) => r.status === 201,
    'body is not empty': (r) => r.body && r.body.length > 0,
  });

  sleep(1);
}