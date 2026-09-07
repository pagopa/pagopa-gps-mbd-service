import http from 'k6/http';
import encoding from 'k6/encoding';

export function createPaymentOption(baseUrl, subkey) {
  const url = `${baseUrl}/mbd/paymentOption`;

  const datiSpecificiInnerXml = `<service>` +
      `<amount>100</amount>` +
      `<debtorName>Mario</debtorName>` +
      `<debtorSurname>Rossi</debtorSurname>` +
      `<debtorEmail>mario.rossi@example.com</debtorEmail>` +
      `<debtorFiscalCode>RSSMRA85T10H501Z</debtorFiscalCode>` +
      `<ciFiscalCode>77777777777</ciFiscalCode>` +
      `<debtorProvince>MI</debtorProvince>` +
      `<documentHash>AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</documentHash>` +
      `</service>`;

  const base64DatiSpecifici = encoding.b64encode(datiSpecificiInnerXml);

  const xmlPayload = `<?xml version="1.0" encoding="UTF-8"?>` +
      `<paDemandPaymentNoticeRequest>` +
      `<idPA>77777777777</idPA>` +
      `<idBrokerPA>77777777777</idBrokerPA>` +
      `<idStation>station1</idStation>` +
      `<datiSpecificiServizioRequest>${base64DatiSpecifici}</datiSpecificiServizioRequest>` +
      `</paDemandPaymentNoticeRequest>`;

  const params = {
    headers: {
      'Content-Type': 'application/xml',
      'Accept': 'application/xml',
      'Ocp-Apim-Subscription-Key': subkey,
      'X-Request-Id': 'k6-test-request-12345',
    },
  };

  return http.post(url, xmlPayload, params);
}