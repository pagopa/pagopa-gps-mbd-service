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

  @NotNull(message = "Amount is required")
  @Schema(description = "MBD amount", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long amount;

  @NotBlank(message = "Debtor first name must be not empty")
  @Schema(description = "Debtor's name", requiredMode = Schema.RequiredMode.REQUIRED)
  private String debtorName;

  @NotBlank(message = "Debtor last name must be not empty")
  @Schema(description = "Debtor's last name", requiredMode = Schema.RequiredMode.REQUIRED)
  private String debtorSurname;

  @NotBlank(message = "Debtor email must be not empty")
  @Schema(description = "Debtor's email", requiredMode = Schema.RequiredMode.REQUIRED)
  private String debtorEmail;

  @NotBlank(message = "Debtor fiscal code must be not empty")
  @Schema(description = "Debtor's fiscal code", requiredMode = Schema.RequiredMode.REQUIRED)
  private String debtorFiscalCode;

  @NotBlank(message = "Fiscal code must be not empty")
  @Schema(description = "Creditor Institution's fiscal code", requiredMode = Schema.RequiredMode.REQUIRED)
  private String ciFiscalCode;

  @NotBlank(message = "Residence province must be not empty")
  @Schema(description = "Debtor's residence province", requiredMode = Schema.RequiredMode.REQUIRED)
  private String debtorProvince;

  @NotBlank(message = "MBD document's hash must be not empty")
  @Size(min = 44, max = 44, message = "MBD document's hash must be exactly 44 characters long")
  @Schema(description = "MBD document's hash", requiredMode = Schema.RequiredMode.REQUIRED)
  private String documentHash;
}
