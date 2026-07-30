package it.gov.pagopa.mbd.gps.service.model.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.HashMap;
import java.util.Map;

/**
 * ConfigDataV1
 */
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

