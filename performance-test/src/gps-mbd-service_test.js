import { check, sleep } from 'k6';
import { createPaymentOption } from './modules/client.js';

const envName = __ENV.ENVIRONMENT || 'local';
const environment = JSON.parse(open(`./environments/${envName}.environment.json`));

let testType = __ENV.TEST_TYPE || 'smoke';
testType = testType.replace(/^.*[\/\\]/, '').replace(/\.json$/, '');

const typeConfig = JSON.parse(open(`./test-types/${testType}.json`));

export const options = typeConfig;

export default function () {
  const baseUrl = environment.baseUrl || environment.gps_mbd_service_host || 'http://localhost:8080';
  const subkey = __ENV.API_SUBSCRIPTION_KEY;

  const response = createPaymentOption(baseUrl, subkey);

  check(response, {
    'status is 201': (r) => r.status === 201,
    'body is not empty': (r) => r.body && r.body.length > 0,
  });

  sleep(1);
}