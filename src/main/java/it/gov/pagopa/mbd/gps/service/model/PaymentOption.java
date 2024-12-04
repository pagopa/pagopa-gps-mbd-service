package it.gov.pagopa.mbd.gps.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Model class that holds MBD payment option data */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOption {

  @NotBlank
  @Schema(description = "Debtor's name", requiredMode = Schema.RequiredMode.REQUIRED)
  private String firstName;

  @NotBlank
  @Schema(description = "Debtor's last name", requiredMode = Schema.RequiredMode.REQUIRED)
  private String lastName;

  @NotNull(message = "amount is required")
  @Schema(description = "Payment option's amount", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long amount;

  @NotBlank(message = "payment option description is required")
  @Size(max = 140) // compliant to paForNode.xsd
  @Schema(description = "Payment option's description", requiredMode = Schema.RequiredMode.REQUIRED)
  private String description;

  @NotNull(message = "is partial payment is required")
  @Schema(
      description = "Payment option's is partial payment flag",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Boolean isPartialPayment;

  @NotNull(message = "due date is required")
  @Schema(description = "Payment option's due date", requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDateTime dueDate;

  @Schema(
      description = "Payment option's retention date",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDateTime retentionDate;

  @Valid
  @Schema(
      description = "Payment option's transfer list",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private List<@Valid Transfer> transfer;
}
