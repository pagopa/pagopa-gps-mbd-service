package it.gov.pagopa.mbd.gps.service.model;

import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

    public ObjectFactory() {}

    public PaDemandPaymentNoticeResponse createPaDemandPaymentNoticeResponse() {
        return new PaDemandPaymentNoticeResponse();
    }

    public CtQrCode createCtQrCode() {
        return new CtQrCode();
    }

    public CtPaymentOptionsDescriptionListPA createCtPaymentOptionsDescriptionListPA() {
        return new CtPaymentOptionsDescriptionListPA();
    }

    public CtPaymentOptionDescriptionPA createCtPaymentOptionDescriptionPA() {
        return new CtPaymentOptionDescriptionPA();
    }
}