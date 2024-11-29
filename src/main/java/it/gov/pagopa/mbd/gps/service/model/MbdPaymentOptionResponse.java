package it.gov.pagopa.mbd.gps.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model class that holds MBD GPS response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MbdPaymentOptionResponse {

    @Valid
    @Schema(description = "MBD payment option", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@Valid PaymentOption> paymentOption;
}