package it.gov.pagopa.mbd.gps.service.model.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/** CreditorInstitution */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditorInstitution {

  @JsonProperty("business_name")
  private String businessName = null;
}
