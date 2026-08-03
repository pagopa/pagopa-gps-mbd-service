package it.gov.pagopa.mbd.gps.service.model.event;

import it.gov.pagopa.mbd.gps.service.model.client.ConfigDataV1;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigCacheData {

  private String cacheVersion;
  // version declared by the api-config-cache payload (ConfigDataV1.version)
  private String version;
  // Kafka event version (CacheUpdateEvent.version)
  private String eventVersion;
  private ConfigDataV1 configDataV1;

  // Compact index to resolve stationCode without retaining the creditorInstitutionStations payload.
  // Structure: creditorInstitutionCode -> (segregationCode -> stationCode)
  private java.util.Map<String, java.util.Map<Long, String>> stationCodeByCiAndSeg;
}
