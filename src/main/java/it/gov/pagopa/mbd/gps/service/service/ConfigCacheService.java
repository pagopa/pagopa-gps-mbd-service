package it.gov.pagopa.mbd.gps.service.service;

import it.gov.pagopa.mbd.gps.service.client.ApiConfigCacheClient;
import it.gov.pagopa.mbd.gps.service.exception.AppError;
import it.gov.pagopa.mbd.gps.service.exception.AppException;
import it.gov.pagopa.mbd.gps.service.model.cache.ConfigDataV1;
import it.gov.pagopa.mbd.gps.service.model.cache.CreditorInstitution;
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

/** Service class responsible for managing the cache of creditor institutions. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigCacheService {

  private final ApiConfigCacheClient apiConfigCacheClient;
  private final AtomicReference<CacheSnapshot> cacheRef = new AtomicReference<>();
  private final ReentrantLock refreshLock = new ReentrantLock();

  @Value("${apiConfigCacheClient.ocpSubKey}")
  private String ocpSubKey;

  /**
   * Application startup listener. Forces an immediate cache load. If the cache cannot be loaded on
   * startup, it throws an exception to fail Kubernetes probes and prevent the pod from serving bad
   * traffic.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void onStart() {
    log.info("[MBD GPS Service] Performing mandatory initial cache load...");

    CacheSnapshot current = cacheRef.get();
    if (current != null && current.data != null && !current.data.isEmpty()) {
      log.info("[MBD GPS Service] Cache already initialized at startup - skipping refresh");
      return;
    }

    CacheSnapshot initialSnapshot = checkAndUpdateCache(null);

    if (initialSnapshot == null || initialSnapshot.data == null || initialSnapshot.data.isEmpty()) {
      log.error("[MBD GPS Service] Critical Error: Mandatory initial cache load failed.");
      throw new IllegalStateException(
          "Failed to load initial configuration cache. Pod startup aborted.");
    }

    log.info(
        "[MBD GPS Service] Initial cache loaded successfully. Total items: {}",
        initialSnapshot.data.size());
  }

  /**
   * Fast, in-memory reader for creditor institutions. Throws AppException if the cache is
   * empty/unavailable.
   */
  public Map<String, CreditorInstitution> getCreditorInstitutions() {
    CacheSnapshot current = cacheRef.get();
    if (current != null && current.data != null) {
      return current.data;
    }

    log.error("[MBD GPS Service] Cache requested but not available in memory.");
    throw new AppException(AppError.CACHE_NOT_AVAILABLE, "Configuration data not available");
  }

  /** Thread-safe check & update logic using double-checked locking and version guards. */
  public CacheSnapshot checkAndUpdateCache(CacheUpdateEvent event) {
    CacheSnapshot current = cacheRef.get();
    if (!needsRefresh(current, event)) {
      return current;
    }

    refreshLock.lock();
    try {
      current = cacheRef.get();
      if (!needsRefresh(current, event)) {
        return current;
      }
      return executeRefresh(event, current);
    } catch (Exception e) {
      log.error("[MBD GPS Service] Error updating api-config cache: {}", e.getMessage(), e);
      if (current != null && current.data != null) {
        log.warn(
            "[MBD GPS Service] Exception occurred during refresh. Fallback to serving previous valid cache.");
        return current;
      }
      return null;
    } finally {
      refreshLock.unlock();
    }
  }

  private CacheSnapshot executeRefresh(CacheUpdateEvent event, CacheSnapshot current) {
    String incomingCacheVersion = getCacheVersion(event);
    String incomingEventVersion = getEventVersion(event);
    String servedEventVersion = getServedEventVersion(current);

    if (shouldSkipUpdate(
        event, current, incomingCacheVersion, incomingEventVersion, servedEventVersion)) {
      log.info(
          "[MBD GPS Service] Skipping cache update - event version is not newer (incoming={}, served={})",
          incomingEventVersion,
          servedEventVersion);
      return current;
    }

    log.info(
        "[MBD GPS Service] Refreshing cache from ApiConfig Client (Trigger: {})...",
        incomingCacheVersion != null ? incomingCacheVersion : "Initial/Manual");

    ConfigDataV1 response =
        apiConfigCacheClient.getCache(ocpSubKey, List.of("creditorInstitutions"));

    if (response == null || response.getCreditorInstitutions() == null) {
      log.warn(
          "[MBD GPS Service] ApiConfig Cache returned null or empty creditorInstitutions payload.");
      return hasValidData(current) ? current : null;
    }

    String finalCacheVer =
        resolveVersion(incomingCacheVersion, current != null ? current.cacheVersion : null);
    String finalEventVer =
        resolveVersion(incomingEventVersion, current != null ? current.eventVersion : null);

    CacheSnapshot newSnapshot =
        new CacheSnapshot(finalCacheVer, finalEventVer, response.getCreditorInstitutions());

    cacheRef.set(newSnapshot);
    log.info(
        "[MBD GPS Service] Cache updated successfully. Total items: {}", newSnapshot.data.size());
    return newSnapshot;
  }

  private String getCacheVersion(CacheUpdateEvent event) {
    return event != null ? event.getCacheVersion() : null;
  }

  private String getEventVersion(CacheUpdateEvent event) {
    return event != null ? event.getVersion() : null;
  }

  private String getServedEventVersion(CacheSnapshot current) {
    return current != null ? current.eventVersion : null;
  }

  private boolean hasValidData(CacheSnapshot snapshot) {
    return snapshot != null && snapshot.data != null;
  }

  private String resolveVersion(String primary, String fallback) {
    return primary != null ? primary : fallback;
  }

  private boolean shouldSkipUpdate(
      CacheUpdateEvent event,
      CacheSnapshot current,
      String incomingCacheVersion,
      String incomingEventVersion,
      String servedEventVersion) {
    return event != null
        && current != null
        && incomingCacheVersion != null
        && incomingCacheVersion.equals(current.cacheVersion)
        && !isNewer(incomingEventVersion, servedEventVersion);
  }

  private boolean needsRefresh(CacheSnapshot current, CacheUpdateEvent evt) {
    if (current == null || current.data == null) {
      return true;
    }

    if (evt == null) {
      return false;
    }

    if (current.cacheVersion == null
        || evt.getCacheVersion() == null
        || !evt.getCacheVersion().equals(current.cacheVersion)) {
      return true;
    }

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

  private static class CacheSnapshot {
    final String cacheVersion;
    final String eventVersion;
    final Map<String, CreditorInstitution> data;

    CacheSnapshot(String cacheVersion, String eventVersion, Map<String, CreditorInstitution> data) {
      this.cacheVersion = cacheVersion;
      this.eventVersion = eventVersion;
      this.data = data;
    }
  }
}
