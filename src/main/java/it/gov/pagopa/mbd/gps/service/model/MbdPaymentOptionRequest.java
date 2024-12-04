package it.gov.pagopa.mbd.gps.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Model class that holds MBD GPS request */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MbdPaymentOptionRequest {

  @Valid
  @Schema(
      description = "MBD payment option's properties",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private MbdPaymentOptionRequestProperties properties;
}
