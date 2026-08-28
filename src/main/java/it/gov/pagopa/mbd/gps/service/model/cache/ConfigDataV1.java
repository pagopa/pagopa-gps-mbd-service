package it.gov.pagopa.mbd.gps.service.model.cache;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.*;

/** ConfigDataV1 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigDataV1 {

  @JsonProperty("version")
  @Builder.Default
  private String version = null;

  @JsonProperty("creditorInstitutions")
  @Valid
  private Map<String, CreditorInstitution> creditorInstitutions = new HashMap<>();
}
