package it.gov.pagopa.mbd.gps.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Model class that holds MBD transfer data */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

  @NotBlank(message = "id transfer is required")
  @Schema(
      description = "Transfer's id",
      allowableValues = {"1", "2", "3", "4", "5"},
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String idTransfer;

  @NotNull(message = "amount is required")
  @Schema(description = "Transfer's amount", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long amount;

  @Schema(
      description = "Fiscal code related to the organization targeted by this transfer.",
      example = "00000000000")
  private String organizationFiscalCode;

  @NotBlank(message = "remittance information is required")
  @Size(
      max = 140,
      message =
          "remittance information must be compliant to EACT FORMATTING RULES, up to 140 chars")
  @Schema(
      description = "Transfer's remittance information",
      requiredMode = Schema.RequiredMode.REQUIRED)
  // https://docs.pagopa.it/saci/specifiche-attuative-dei-codici-identificativi-di-versamento-riversamento-e-rendicontazione/operazione-di-trasferimento-fondi
  private String remittanceInformation; // causale

  @Valid
  @Schema(description = "Transfer's stamp", requiredMode = Schema.RequiredMode.REQUIRED)
  private Stamp stamp;
}
