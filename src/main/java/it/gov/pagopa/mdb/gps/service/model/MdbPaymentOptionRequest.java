package it.gov.pagopa.mdb.gps.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MdbPaymentOptionRequest {

    @Valid
    @Schema(description = "MDB payment option's properties", requiredMode = Schema.RequiredMode.REQUIRED)
    private MdbPaymentOptionRequestProperties properties;
}