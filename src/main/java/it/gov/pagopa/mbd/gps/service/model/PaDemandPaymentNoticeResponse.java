package it.gov.pagopa.mbd.gps.service.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "paDemandPaymentNoticeResponse", propOrder = {
        "outcome",
        "fiscalCodePA",
        "companyName",
        "officeName",
        "paymentDescription",
        "paymentList",
        "qrCode"
})
@XmlRootElement(name = "paDemandPaymentNoticeResponse")
public class PaDemandPaymentNoticeResponse {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    private StOutcome outcome;

    private String fiscalCodePA;
    private String companyName;
    private String officeName;
    private String paymentDescription;
    private CtPaymentOptionsDescriptionListPA paymentList;
    private CtQrCode qrCode;
}