package it.gov.pagopa.mbd.gps.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.gov.pagopa.mbd.gps.service.client.ApiConfigCacheClient;
import it.gov.pagopa.mbd.gps.service.exception.AppException;
import it.gov.pagopa.mbd.gps.service.model.cache.ConfigDataV1;
import it.gov.pagopa.mbd.gps.service.model.cache.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.model.event.CacheUpdateEvent;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConfigCacheServiceTest {

  @Mock private ApiConfigCacheClient apiConfigCacheClient;

  private ConfigCacheService configCacheService;

  @BeforeEach
  void setUp() {
    configCacheService = new ConfigCacheService(apiConfigCacheClient);
    ReflectionTestUtils.setField(configCacheService, "ocpSubKey", "test-key");
  }

  @Test
  @DisplayName("onStart & checkAndUpdateCache - Success: Initial cache load on application startup")
  void onStart_Success() {
    ConfigDataV1 mockData = new ConfigDataV1();
    Map<String, CreditorInstitution> map = new HashMap<>();
    map.put("77777777777", CreditorInstitution.builder().businessName("Comune Test").build());
    mockData.setCreditorInstitutions(map);

    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(mockData);

    configCacheService.onStart();

    assertThat(configCacheService.getCreditorInstitutions()).containsKey("77777777777");
    configCacheService.onStart();
  }

  @Test
  @DisplayName("onStart - KO: Throws IllegalStateException when initial cache load returns empty")
  void onStart_FailureAbortsStartup() {
    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(null);

    assertThatThrownBy(() -> configCacheService.onStart())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to load initial configuration cache");
  }

  @Test
  @DisplayName("getCreditorInstitutions - KO: Throws AppException when cache is not initialized")
  void getCreditorInstitutions_UninitializedCache() {
    assertThatThrownBy(() -> configCacheService.getCreditorInstitutions())
        .isInstanceOf(AppException.class)
        .hasMessageContaining("Configuration data not available");
  }

  @Test
  @DisplayName("checkAndUpdateCache - Success: Handles CacheUpdateEvent with version comparison")
  void checkAndUpdateCache_WithUpdateEvent() {
    ConfigDataV1 mockData = new ConfigDataV1();
    Map<String, CreditorInstitution> map = new HashMap<>();
    map.put("77777777777", CreditorInstitution.builder().businessName("Comune Test").build());
    mockData.setCreditorInstitutions(map);

    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(mockData);

    CacheUpdateEvent event1 = new CacheUpdateEvent();
    event1.setCacheVersion("v1");
    event1.setVersion("10");
    configCacheService.checkAndUpdateCache(event1);

    configCacheService.checkAndUpdateCache(event1);

    CacheUpdateEvent event2 = new CacheUpdateEvent();
    event2.setCacheVersion("v1");
    event2.setVersion("20");
    configCacheService.checkAndUpdateCache(event2);

    assertThat(configCacheService.getCreditorInstitutions()).isNotNull();
  }

  @Test
  @DisplayName(
      "checkAndUpdateCache - Fallback: Exception during refresh retains previous valid cache")
  void checkAndUpdateCache_ExceptionFallback() {
    ConfigDataV1 mockData = new ConfigDataV1();
    Map<String, CreditorInstitution> map = new HashMap<>();
    map.put("77777777777", CreditorInstitution.builder().businessName("Comune Test").build());
    mockData.setCreditorInstitutions(map);

    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(mockData);
    configCacheService.onStart();

    when(apiConfigCacheClient.getCache(anyString(), anyList()))
        .thenThrow(new RuntimeException("API Config unreachable"));

    CacheUpdateEvent newEvent = new CacheUpdateEvent();
    newEvent.setCacheVersion("v2");
    newEvent.setVersion("1");

    configCacheService.checkAndUpdateCache(newEvent);

    assertThat(configCacheService.getCreditorInstitutions()).containsKey("77777777777");
  }

  @Test
  @DisplayName(
      "checkAndUpdateCache - Edge Cases: tests cacheVersion mismatch and event version comparison")
  void checkAndUpdateCache_VersionEdgeCases() {
    ConfigDataV1 mockData = new ConfigDataV1();
    Map<String, CreditorInstitution> map = new HashMap<>();
    map.put("77777777777", CreditorInstitution.builder().businessName("Comune Test").build());
    mockData.setCreditorInstitutions(map);

    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(mockData);

    // Inizializza cache
    CacheUpdateEvent event1 = new CacheUpdateEvent();
    event1.setCacheVersion("v1");
    event1.setVersion("10");
    configCacheService.checkAndUpdateCache(event1);

    // Evento con cacheVersion diversa -> forza il refresh
    CacheUpdateEvent eventDifferentCacheVer = new CacheUpdateEvent();
    eventDifferentCacheVer.setCacheVersion("v2");
    eventDifferentCacheVer.setVersion("5");
    configCacheService.checkAndUpdateCache(eventDifferentCacheVer);

    // Evento con version non numerica -> test fallback compareTo
    CacheUpdateEvent eventNonNumericVer = new CacheUpdateEvent();
    eventNonNumericVer.setCacheVersion("v2");
    eventNonNumericVer.setVersion("abc");
    configCacheService.checkAndUpdateCache(eventNonNumericVer);

    assertThat(configCacheService.getCreditorInstitutions()).isNotNull();
  }
}
