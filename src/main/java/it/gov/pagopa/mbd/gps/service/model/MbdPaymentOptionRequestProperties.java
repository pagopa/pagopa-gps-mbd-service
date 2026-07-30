package it.gov.pagopa.mbd.gps.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.mbd.gps.service.annotation.ValidMbdDebtor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
@ValidMbdDebtor
public class MbdPaymentOptionRequestProperties {

  @NotNull(message = "Amount is required")
  @Schema(description = "MBD amount", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long amount;

  @Schema(description = "Debtor first name. Required for Physical Persons, optional/null for Legal Entities", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String debtorName;

  @NotBlank(message = "Debtor last name must be not empty")
  @Schema(description = "Debtor's last name", requiredMode = Schema.RequiredMode.REQUIRED)
  private String debtorSurname;

  @NotBlank(message = "Debtor email must be not empty")
  @Schema(description = "Debtor's email", requiredMode = Schema.RequiredMode.REQUIRED)
  private String debtorEmail;

  @NotBlank(message = "Debtor fiscal code must be not empty")
  @Schema(description = "Debtor's fiscal code", requiredMode = Schema.RequiredMode.REQUIRED)
  @Pattern(regexp = "^(?:[A-Za-z]{6}[0-9]{2}[A-Za-z][0-9]{2}[A-Za-z][0-9]{3}[A-Za-z]|[0-9]{11})$", message = "Invalid ciFiscalCode format. Must be a valid Italian Fiscal Code (16 characters) or VAT number (11 digits)")
  private String debtorFiscalCode;

  @NotBlank(message = "CI Fiscal code must be not empty")
  @Schema(description = "Creditor Institution's Italian Fiscal Code", requiredMode = Schema.RequiredMode.REQUIRED)
  private String ciFiscalCode;

  @NotBlank(message = "Residence province must be not empty")
  @Schema(description = "Debtor's residence province", requiredMode = Schema.RequiredMode.REQUIRED)
  private String debtorProvince;

  @NotBlank(message = "MBD document's hash must be not empty")
  @Size(min = 44, max = 44, message = "MBD document's hash must be exactly 44 characters long")
  @Schema(description = "MBD document's hash", requiredMode = Schema.RequiredMode.REQUIRED)
  @Pattern(
          regexp = "^[A-Za-z0-9+/]{43}=$|^[A-Za-z0-9+/]{42}==$",
          message = "Invalid hashDocument format. Must be a valid Base64 SHA-256 string"
  )
  private String documentHash;
}
