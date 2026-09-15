package it.gov.pagopa.mbd.gps.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConfigCacheServiceTest {

  private static final String SUB_KEY = "dummy-sub-key";
  @Mock private ApiConfigCacheClient apiConfigCacheClient;
  @InjectMocks private ConfigCacheService configCacheService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(configCacheService, "ocpSubKey", SUB_KEY);
  }

  @Test
  @DisplayName("onStart - Success: Cache initialization on application startup")
  void onStart_Success() {
    ConfigDataV1 mockResponse = createMockConfigData();
    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(mockResponse);

    assertDoesNotThrow(() -> configCacheService.onStart());
    verify(apiConfigCacheClient, times(1)).getCache(eq(SUB_KEY), anyList());
  }

  @Test
  @DisplayName("onStart - KO: Throws IllegalStateException on initial fetch error (Fail-Fast)")
  void onStart_ExceptionHandled() {
    when(apiConfigCacheClient.getCache(anyString(), anyList()))
        .thenThrow(new RuntimeException("Connection error"));

    assertThrows(IllegalStateException.class, () -> configCacheService.onStart());
  }

  @Test
  @DisplayName("getCreditorInstitutions - Success: Returns data from cache if already present")
  void getCreditorInstitutions_AlreadyInCache() {
    ConfigDataV1 mockResponse = createMockConfigData();
    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(mockResponse);

    // Popola prima la cache via onStart()
    configCacheService.onStart();

    // Recupera i dati dalla cache
    Map<String, CreditorInstitution> result1 = configCacheService.getCreditorInstitutions();
    Map<String, CreditorInstitution> result2 = configCacheService.getCreditorInstitutions();

    assertNotNull(result1);
    assertNotNull(result2);
    assertEquals(result1, result2);
    verify(apiConfigCacheClient, times(1)).getCache(anyString(), anyList());
  }

  @Test
  @DisplayName("getCreditorInstitutions - KO: Throws AppException when cache is unavailable")
  void getCreditorInstitutions_ThrowsException_WhenCacheNotAvailable() {
    // Nessuno stubbing necessario per apiConfigCacheClient poiche' la cache in memoria e' vuota
    assertThrows(AppException.class, () -> configCacheService.getCreditorInstitutions());
  }

  @Test
  @DisplayName("checkAndUpdateCache - Success: Cache updated using a valid CacheUpdateEvent")
  void checkAndUpdateCache_WithValidEvent() {
    // Popola la cache iniziale
    ConfigDataV1 initialData = createMockConfigData();
    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(initialData);
    configCacheService.onStart();

    // Prepara evento con versione piu' recente
    CacheUpdateEvent event = new CacheUpdateEvent();
    event.setCacheVersion("v2");
    event.setVersion("10");

    ConfigDataV1 updatedData = createMockConfigData();
    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(updatedData);

    var snapshot = configCacheService.checkAndUpdateCache(event);

    assertNotNull(snapshot);
    verify(apiConfigCacheClient, times(2)).getCache(anyString(), anyList());
  }

  @Test
  @DisplayName("checkAndUpdateCache - Ignores update event if incoming version is older")
  void checkAndUpdateCache_IgnoreOlderEvent() {
    CacheUpdateEvent event1 = new CacheUpdateEvent();
    event1.setCacheVersion("v1");
    event1.setVersion("10");

    ConfigDataV1 data = createMockConfigData();
    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(data);
    configCacheService.checkAndUpdateCache(event1);

    // Invia evento con versione precedente (es. 5)
    CacheUpdateEvent olderEvent = new CacheUpdateEvent();
    olderEvent.setCacheVersion("v1");
    olderEvent.setVersion("5");

    configCacheService.checkAndUpdateCache(olderEvent);

    verify(apiConfigCacheClient, times(1)).getCache(anyString(), anyList());
  }

  @Test
  @DisplayName("checkAndUpdateCache - Compares non-numeric string versions correctly")
  void checkAndUpdateCache_NonNumericVersions() {
    CacheUpdateEvent event1 = new CacheUpdateEvent();
    event1.setCacheVersion("v1");
    event1.setVersion("A");

    ConfigDataV1 data = createMockConfigData();
    when(apiConfigCacheClient.getCache(anyString(), anyList())).thenReturn(data);
    configCacheService.checkAndUpdateCache(event1);

    CacheUpdateEvent event2 = new CacheUpdateEvent();
    event2.setCacheVersion("v1");
    event2.setVersion("B");

    configCacheService.checkAndUpdateCache(event2);

    verify(apiConfigCacheClient, times(2)).getCache(anyString(), anyList());
  }

  private ConfigDataV1 createMockConfigData() {
    ConfigDataV1 configData = new ConfigDataV1();
    Map<String, CreditorInstitution> map = new HashMap<>();
    map.put("77777777777", new CreditorInstitution());
    configData.setCreditorInstitutions(map);
    return configData;
  }
}
