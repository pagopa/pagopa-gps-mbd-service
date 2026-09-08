import http from 'k6/http';
import encoding from 'k6/encoding';

export function createPaymentOption(baseUrl, subkey) {
  const url = `${baseUrl}/mbd/paymentOption`;

  const datiSpecificiInnerXml =
    `<marcaDaBollo xmlns="http://www.agenziaentrate.gov.it/2014/MarcaDaBollo" xsi:schemaLocation="http://pagopa-api.pagopa.gov.it/pa/MarcaDaBollo.xsd" xmlns:xsi="http://www.w3.org/2001/XMLAccept-instance">` +
    `<amount>16.00</amount>` +
    `<debtor>` +
    `<uniqueIdentifier>` +
    `<entityUniqueIdentifierType>F</entityUniqueIdentifierType>` +
    `<entityUniqueIdentifierValue>RSSMRA85T10H501Z</entityUniqueIdentifierValue>` +
    `</uniqueIdentifier>` +
    `<fullName>Mario Rossi</fullName>` +
    `<province>MI</province>` +
    `<email>mario.rossi@example.com</email>` +
    `</debtor>` +
    `<fiscalCode>77777777777</fiscalCode>` +
    `<documentHash>af4WYySYOCa6Xgk+ByxIvxuaPsx1JerRgipP1xeM8bI=</documentHash>` +
    `</marcaDaBollo>`;

  const base64DatiSpecifici = encoding.b64encode(datiSpecificiInnerXml);

  const xmlPayload = `<?xml version="1.0" encoding="UTF-8"?>` +
      `<paDemandPaymentNoticeRequest>` +
      `<idPA>77777777777</idPA>` +
      `<idBrokerPA>77777777777</idBrokerPA>` +
      `<idStation>station1</idStation>` +
      `<idServizio>00005</idServizio>` +
      `<idSoggettoServizio>50000</idSoggettoServizio>` +
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