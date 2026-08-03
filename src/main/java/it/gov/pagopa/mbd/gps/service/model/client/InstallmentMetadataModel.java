package it.gov.pagopa.mbd.gps.service.model.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class InstallmentMetadataModel implements Serializable {
    private String key;
    private String value;
}
