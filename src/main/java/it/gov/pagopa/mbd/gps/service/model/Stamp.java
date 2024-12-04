package it.gov.pagopa.mbd.gps.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Model class that holds MBD stamp data */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stamp {

  @NotBlank
  @Size(max = 72)
  @Schema(
      description =
          "Document hash type is stBase64Binary72 as described in https://github.com/pagopa/pagopa-api.",
      requiredMode = Schema.RequiredMode.REQUIRED)
  // Stamp generally get as input a base64sha256, that is the SHA256 hash of a given string encoded
  // with Base64.
  // It is not equivalent to base64encode(sha256(“test”)), if sha256() returns a hexadecimal
  // representation.
  // The result should normally be 44 characters, to be compliant with as-is it was extended to 72
  private String hashDocument;

  @NotBlank
  @Size(min = 2, max = 2)
  @Schema(
      description = "The type of the stamp",
      minLength = 2,
      maxLength = 2,
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String stampType;

  @NotBlank
  @Pattern(regexp = "[A-Z]{2}")
  @Schema(
      description = "The province of residence",
      example = "RM",
      pattern = "[A-Z]{2,2}",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String provincialResidence;
}
