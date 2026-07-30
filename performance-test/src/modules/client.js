import http from 'k6/http';

export function createPaymentOption(baseUrl, subkey) {
  const url = `${baseUrl}/mbd/paymentOption`;

  const payload = JSON.stringify({
    properties: {
      amount: 100,
      debtorName: "Mario",
      debtorSurname: "Rossi",
      debtorEmail: "mario.rossi@example.com",
      debtorFiscalCode: "RSSMRA85T10H501Z",
      ciFiscalCode: "77777777777",
      debtorProvince: "MI",
      documentHash: "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU="
    }
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Ocp-Apim-Subscription-Key': subkey,
    },
  };

  return http.post(url, payload, params);
}