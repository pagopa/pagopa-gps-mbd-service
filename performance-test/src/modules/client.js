import http from 'k6/http';

const subKey = `${__ENV.API_SUBSCRIPTION_KEY}`;

export function postToGPSMBDService(url, ciFiscalCode) {
  let headers = {
    'Ocp-Apim-Subscription-Key': subKey,
    "Content-Type": "application/json"
  };

  return http.post(url, JSON.stringify(buildRequestBody(ciFiscalCode)), { headers, responseType: "text" });
}

function buildRequestBody(ciFiscalCode) {
  return {
    "properties": {
      "amount": 16,
      "debtorName": "ANONYMOUS",
      "debtorSurname": "ANONYMOUS",
      "debtorFiscalCode": "11111111111111111",
      "debtorEmail": "email@test.it",
      "ciFiscalCode": ciFiscalCode,
      "debtorProvince": "PR",
      "documentHash": "1trA5qyjSZNwiwtGG46dyjRpL16TFgGCFvnfFzQrFHbB"
    }
  }
}