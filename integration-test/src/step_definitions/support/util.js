// integration-test/src/step_definitions/support/util.js

function buildRequestBody(amount, fiscalCode, province, documentHash, debtorName, debtorSurname, ciFiscalCode = "77777777777", debtorEmail = "mario.rossi@example.com") {

    const amountVal = (amount !== null && amount !== undefined) ? amount : '';
    const fcVal = fiscalCode || '';
    const provVal = province || '';
    const hashVal = documentHash !== undefined ? documentHash : "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";
    const nameVal = debtorName || '';
    const surnameVal = debtorSurname || '';
    const emailVal = debtorEmail || '';
    const ciVal = ciFiscalCode || '';

    let innerXml = '<service>';
    if (amountVal !== '') innerXml += `<amount>${amountVal}</amount>`;
    if (nameVal !== '') innerXml += `<debtorName>${nameVal}</debtorName>`;
    if (surnameVal !== '') innerXml += `<debtorSurname>${surnameVal}</debtorSurname>`;
    if (emailVal !== '') innerXml += `<debtorEmail>${emailVal}</debtorEmail>`;
    if (fcVal !== '') innerXml += `<debtorFiscalCode>${fcVal}</debtorFiscalCode>`;
    innerXml += `<ciFiscalCode>${ciVal}</ciFiscalCode>`;
    if (provVal !== '') innerXml += `<debtorProvince>${provVal}</debtorProvince>`;
    innerXml += `<documentHash>${hashVal}</documentHash>`;
    innerXml += '</service>';

    const base64InnerXml = Buffer.from(innerXml).toString('base64');

    return `<?xml version="1.0" encoding="UTF-8"?>
<paDemandPaymentNoticeRequest xmlns="http://pagopa.gov.it/mbd/service">
    <idPA>${ciVal}</idPA>
    <idBrokerPA>${ciVal}</idBrokerPA>
    <idStation>station1</idStation>
    <datiSpecificiServizioRequest>${base64InnerXml}</datiSpecificiServizioRequest>
</paDemandPaymentNoticeRequest>`;
}

module.exports = { buildRequestBody };