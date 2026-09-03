package it.gov.pagopa.mbd.gps.service.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "paDemandPaymentNoticeRequest", propOrder = {
        "idPA",
        "idBrokerPA",
        "idStation",
        "datiSpecificiServizioRequest"
})
@XmlRootElement(name = "paDemandPaymentNoticeRequest")
public class PaDemandPaymentNoticeRequest {

    @XmlElement(required = true)
    private String idPA;

    @XmlElement(required = true)
    private String idBrokerPA;

    @XmlElement(required = true)
    private String idStation;

    @XmlElement(required = true)
    private byte[] datiSpecificiServizioRequest;
}