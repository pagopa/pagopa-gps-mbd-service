function buildRequestBody(amount, fiscalCode, provincialResidence, documentHash) {
    return {
        "properties": {
            "amount": amount,
            "firstName": "ANONYMOUS",
            "lastName": "ANONYMOUS",
            "fiscalCode": fiscalCode,
            "provincialResidence": provincialResidence,
            "documentHash": documentHash
        }
    }
}

module.exports = { buildRequestBody }