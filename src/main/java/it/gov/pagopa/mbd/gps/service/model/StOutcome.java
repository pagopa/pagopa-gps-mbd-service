package it.gov.pagopa.mbd.gps.service.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "stOutcome")
@XmlEnum
public enum StOutcome {
    OK,
    KO;

    public String value() {
        return name();
    }

    public static StOutcome fromValue(String v) {
        return valueOf(v);
    }
}