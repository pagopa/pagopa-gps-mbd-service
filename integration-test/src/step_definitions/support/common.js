// integration-test/src/step_definitions/support/common.js
const axios = require("axios");

if (process.env.CANARY) {
    axios.defaults.headers.common['X-Canary'] = 'canary';
}

function post(url, body) {
    return axios.post(url, body, {
        headers: {
            'content-type': 'application/xml',
            'accept': 'application/xml',
            'Ocp-Apim-Subscription-Key': process.env.SUBKEY || 'test'
        }
    })
        .then(res => res)
        .catch(error => error.response);
}

module.exports = { post };