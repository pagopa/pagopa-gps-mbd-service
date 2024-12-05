const axios = require("axios");

axios.defaults.headers.common['Ocp-Apim-Subscription-Key'] = process.env.SUBKEY || ""; // for all requests
if (process.env.CANARY) {
  axios.defaults.headers.common['X-Canary'] = 'canary' // for all requests
}

function post(url, body) {
  return axios.post(url, body, {
    headers: {
      'Content-Type': 'application/json',
      'accept': 'application/json',
    }
  })
  .then(res => {
    return res;
  })
  .catch(error => {
    return error.response;
  });
}

module.exports = {post}
