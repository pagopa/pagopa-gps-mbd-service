package it.gov.pagopa.mbd.gps.service.consumer;

import it.gov.pagopa.mbd.gps.service.model.event.CacheUpdateEvent;
import it.gov.pagopa.mbd.gps.service.service.ConfigCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigCacheUpdatesConsumer {

    private final ConfigCacheService configCacheService;

    @KafkaListener(
            topics = "${kafka.topic.nodo-dei-pagamenti-cache}",
            groupId = "${kafka.consumer.group-id}"
    )
    public void consume(CacheUpdateEvent event) {
        if (event == null) {
            log.warn("[MBD GPS Service] Received null cache update event - skipping");
            return;
        }

        log.info("[MBD GPS Service] Received update event with cacheVersion {} and version {}",
                event.getCacheVersion(), event.getVersion());

        try {
            configCacheService.checkAndUpdateCache(event);
        } catch (Exception e) {
            log.error(
                    "[MBD GPS Service] Cache update failed (cacheVersion={}, version={}). Keeping previous snapshot. Cause: {}",
                    event.getCacheVersion(), event.getVersion(), e.getMessage(), e
            );
        }
    }
}
