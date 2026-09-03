package it.gov.pagopa.mbd.gps.service.model;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "stAmountOption")
@XmlEnum
public enum StAmountOption {
    EQ,
    LS,
    GT;

    public String value() {
        return name();
    }

    public static StAmountOption fromValue(String v) {
        return valueOf(v);
    }
}