function buildRequestBody(amount, fiscalCode, province, documentHash, debtorFullName, ciFiscalCode = "77777777777", debtorEmail = "mario.rossi@example.com") {

    const amountVal = (amount !== null && amount !== undefined) ? amount : '';
    const fcVal = fiscalCode || '';
    const provVal = province || '';
    const hashVal = documentHash !== undefined ? documentHash : "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=";
    const fullNameVal = debtorFullName !== undefined && debtorFullName !== null ? debtorFullName : '';
    const emailVal = debtorEmail !== undefined && debtorEmail !== null ? debtorEmail : '';
    const ciVal = ciFiscalCode || "77777777777";
    const entityType = fcVal.length === 11 ? 'G' : 'F';

    let innerXml = '<marcaDaBollo xmlns="http://www.agenziaentrate.gov.it/2014/MarcaDaBollo">';

    if (amountVal !== '') {
        innerXml += `<amount>${amountVal}</amount>`;
    }

    innerXml += '<debtor>';
    innerXml += '<uniqueIdentifier>';
    innerXml += `<entityUniqueIdentifierType>${entityType}</entityUniqueIdentifierType>`;
    if (fcVal !== '') {
        innerXml += `<entityUniqueIdentifierValue>${fcVal}</entityUniqueIdentifierValue>`;
    }
    innerXml += '</uniqueIdentifier>';

    if (fullNameVal !== '') {
        innerXml += `<fullName>${fullNameVal}</fullName>`;
    }
    if (provVal !== '') {
        innerXml += `<province>${provVal}</province>`;
    }
    if (emailVal !== '') {
        innerXml += `<email>${emailVal}</email>`;
    }
    innerXml += '</debtor>';

    if (ciVal !== '') {
        innerXml += `<fiscalCode>${ciVal}</fiscalCode>`;
    }
    if (hashVal !== '') {
        innerXml += `<documentHash>${hashVal}</documentHash>`;
    }

    innerXml += '</marcaDaBollo>';

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