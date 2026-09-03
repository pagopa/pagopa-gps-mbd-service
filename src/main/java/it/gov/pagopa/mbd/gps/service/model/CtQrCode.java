package it.gov.pagopa.mbd.gps.service.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctQrCode", propOrder = {
        "fiscalCode",
        "noticeNumber"
})
public class CtQrCode {

    @XmlElement(required = true)
    private String fiscalCode;

    @XmlElement(required = true)
    private String noticeNumber;
}