package it.gov.pagopa.mdb.gps.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MdbPaymentOptionResponse {

    @Valid
    @Schema(description = "MDB payment option", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@Valid PaymentOption> paymentOption;
}