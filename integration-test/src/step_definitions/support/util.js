function buildRequestBody(amount, fiscalCode, province, documentHash, debtorName, debtorSurname, ciFiscalCode = "77777777777") {
    return {
        properties: {
            amount: amount,
            debtorName: debtorName,
            debtorSurname: debtorSurname,
            debtorEmail: "mario.rossi@example.com",
            debtorFiscalCode: fiscalCode,
            ciFiscalCode: ciFiscalCode,
            debtorProvince: province,
            documentHash: documentHash || "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU="
        }
    };
}

module.exports = { buildRequestBody };