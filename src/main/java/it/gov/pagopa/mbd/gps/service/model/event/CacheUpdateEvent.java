package it.gov.pagopa.mbd.gps.service.model.event;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheUpdateEvent {

  private String cacheVersion;
  private String version;
  private String timestamp;
}
