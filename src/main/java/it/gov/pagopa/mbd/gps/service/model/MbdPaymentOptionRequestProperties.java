package it.gov.pagopa.mbd.gps.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Model class that holds MBD service specific data */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MbdPaymentOptionRequestProperties {

  @NotNull(message = "amount is required")
  @Schema(description = "MBD amount", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long amount;

  @NotBlank
  @Schema(description = "Debtor's name", requiredMode = Schema.RequiredMode.REQUIRED)
  private String firstName;

  @NotBlank
  @Schema(description = "Debtor's last name", requiredMode = Schema.RequiredMode.REQUIRED)
  private String lastName;

  @NotBlank
  @Schema(description = "Debtor's fiscal code", requiredMode = Schema.RequiredMode.REQUIRED)
  private String fiscalCode;

  @NotBlank
  @Schema(description = "Debtor's residence province", requiredMode = Schema.RequiredMode.REQUIRED)
  private String provincialResidence;

  @NotBlank
  @Size(min = 44, max = 44)
  @Schema(description = "MBD document's hash", requiredMode = Schema.RequiredMode.REQUIRED)
  private String documentHash;
}
