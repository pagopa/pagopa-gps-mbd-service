package it.gov.pagopa.mbd.gps.service.service;

import it.gov.pagopa.mbd.gps.service.client.ApiConfigCacheClient;
import it.gov.pagopa.mbd.gps.service.exception.AppError;
import it.gov.pagopa.mbd.gps.service.exception.AppException;
import it.gov.pagopa.mbd.gps.service.model.client.ConfigDataV1;
import it.gov.pagopa.mbd.gps.service.model.client.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.model.event.CacheUpdateEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for managing the cache of creditor institutions. It retrieves and
 * updates the cache from the API when necessary, ensuring thread safety and consistency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigCacheService {

  private final ApiConfigCacheClient apiConfigCacheClient;

  @Value("${apiConfigCacheClient.ocpSubKey}")
  private String ocpSubKey;

  private static class CacheSnapshot {
    String cacheVersion;
    String eventVersion;
    Map<String, CreditorInstitution> data;

    CacheSnapshot(String cacheVersion, String eventVersion, Map<String, CreditorInstitution> data) {
      this.cacheVersion = cacheVersion;
      this.eventVersion = eventVersion;
      this.data = data;
    }
  }

  private final AtomicReference<CacheSnapshot> cacheRef = new AtomicReference<>();
  private final ReentrantLock refreshLock = new ReentrantLock();

  /**
   * This method is called when the application is ready. It attempts to retrieve the cache for the
   * first time. If an error occurs during this process, it logs the error message.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void onStart() {
    try {
      checkAndUpdateCache(null);
    } catch (Exception e) {
      log.error("[MBD GPS Service] Error on first cache retrieval: {}", e.getMessage());
    }
  }

  /**
   * Retrieves the current cache of creditor institutions. If the cache is not available, it
   * attempts to refresh it. If the cache is still not available after the refresh attempt, it
   * throws an AppException.
   *
   * @return a map of creditor institutions keyed by their fiscal code
   * @throws AppException if the cache is not available
   */
  public Map<String, CreditorInstitution> getCreditorInstitutions() {
    CacheSnapshot current = cacheRef.get();
    if (current != null && current.data != null) {
      return current.data;
    }

    CacheSnapshot updated = checkAndUpdateCache(null);
    if (updated == null || updated.data == null) {
      throw new AppException(AppError.CACHE_NOT_AVAILABLE, "Configuration data not available");
    }
    return updated.data;
  }

  /**
   * Checks if the cache needs to be refreshed based on the provided event. If a refresh is needed,
   * it retrieves the latest configuration data from the API and updates the cache.
   *
   * @param event the cache update event that may trigger a refresh; can be null
   * @return the current or updated cache snapshot
   */
  public CacheSnapshot checkAndUpdateCache(CacheUpdateEvent event) {
    CacheSnapshot current = cacheRef.get();

    if (current != null && !needsRefresh(current, event)) {
      return current;
    }

    refreshLock.lock();
    try {
      current = cacheRef.get();
      if (current != null && !needsRefresh(current, event)) {
        return current;
      }

      log.info("Refreshing cache...");
      ConfigDataV1 response =
          apiConfigCacheClient.getCache(ocpSubKey, List.of("creditorInstitutions"));

      if (response != null && response.getCreditorInstitutions() != null) {
        String incomingCacheVersion =
            event != null
                ? event.getCacheVersion()
                : (current != null ? current.cacheVersion : null);
        String incomingEventVersion =
            event != null ? event.getVersion() : (current != null ? current.eventVersion : null);

        CacheSnapshot newSnapshot =
            new CacheSnapshot(
                incomingCacheVersion, incomingEventVersion, response.getCreditorInstitutions());

        cacheRef.set(newSnapshot);
        log.info("[MBD GPS Service] Cache updated successfully. Size: {}", newSnapshot.data.size());
        return newSnapshot;
      }

      return current;
    } finally {
      refreshLock.unlock();
    }
  }

  private boolean needsRefresh(CacheSnapshot current, CacheUpdateEvent evt) {
    if (current.data == null) return true;
    if (evt == null)
      return false; // Se non c'è l'evento e la cache ha già dati, non serve rinfrescare
    if (current.cacheVersion == null) return true;
    if (evt.getCacheVersion() == null || !evt.getCacheVersion().equals(current.cacheVersion))
      return true;

    return isNewer(evt.getVersion(), current.eventVersion);
  }

  private boolean isNewer(String a, String b) {
    if (a == null && b == null) return false;
    if (a == null) return false;
    if (b == null) return true;
    try {
      return new java.math.BigInteger(a).compareTo(new java.math.BigInteger(b)) > 0;
    } catch (NumberFormatException e) {
      return a.compareTo(b) > 0;
    }
  }
}
