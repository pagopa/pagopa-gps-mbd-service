package it.gov.pagopa.mbd.gps.service.model.client;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InstallmentMetadataModel implements Serializable {
  private String key;
  private String value;
}
