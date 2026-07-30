package it.gov.pagopa.mbd.gps.service.model.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TransferModel implements Serializable {
  private static final long serialVersionUID = 5593063492841435180L;

  private String idTransfer;
  private Long amount;
  private String organizationFiscalCode;
  private String remittanceInformation;
  private String category;
  private String iban; // null per e.Bollo 2.0
  private String postalIban; // null per e.Bollo 2.0
  private Stamp stamp; // Oggetto specifico Marca da Bollo Digitale
  private String companyName;
  private List<TransferMetadataModel> transferMetadata = new ArrayList<>();
}
