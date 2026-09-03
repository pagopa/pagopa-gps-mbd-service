package it.gov.pagopa.mbd.gps.service.model;

import java.math.BigDecimal;
import javax.xml.datatype.XMLGregorianCalendar;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ctPaymentOptionDescriptionPA", propOrder = {
        "amount",
        "options",
        "dueDate",
        "detailDescription",
        "allCCP"
})
public class CtPaymentOptionDescriptionPA {

    @XmlElement(required = true)
    private BigDecimal amount;

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    private StAmountOption options;

    @XmlSchemaType(name = "date")
    private XMLGregorianCalendar dueDate;

    private String detailDescription;

    @XmlElement(name = "allCCP")
    private boolean allCCP;
}