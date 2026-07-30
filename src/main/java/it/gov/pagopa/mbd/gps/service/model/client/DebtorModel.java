package it.gov.pagopa.mbd.gps.service.model.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class DebtorModel implements Serializable {
    private Type type;
    private String fiscalCode;
    private String fullName;
    private String streetName;
    private String civicNumber;
    private String postalCode;
    private String city;
    private String province;
    private String region;
    private String country;
    private String email;
    private String phone;
}
