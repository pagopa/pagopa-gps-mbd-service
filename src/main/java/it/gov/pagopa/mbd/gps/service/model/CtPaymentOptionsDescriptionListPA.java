package it.gov.pagopa.mbd.gps.service.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctPaymentOptionsDescriptionListPA", propOrder = {
        "paymentOptionDescription"
})
public class CtPaymentOptionsDescriptionListPA {

    @XmlElement(required = true)
    private CtPaymentOptionDescriptionPA paymentOptionDescription;
}