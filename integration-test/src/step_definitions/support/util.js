function buildRequestBody(amount, ciFiscalCode, debtorProvince, documentHash) {
    return {
        "properties": {
            "amount": amount,
            "debtorName": "ANONYMOUS",
            "debtorSurname": "ANONYMOUS",
            "debtorFiscalCode": "11111111111111111",
            "debtorEmail": "email@test.it",
            "ciFiscalCode": ciFiscalCode,
            "debtorProvince": debtorProvince,
            "documentHash": documentHash
        }
    }
}

module.exports = { buildRequestBody }