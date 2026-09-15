package it.gov.pagopa.mbd.gps.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import it.gov.pagopa.mbd.gps.service.model.cache.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.service.ConfigCacheService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(
        properties = {
                "apiConfigCacheClient.url=http://localhost:8080",
                "service.gpd.host=http://localhost:8080"
        })
class ApplicationTest {

    @MockBean private ConfigCacheService configCacheService;

    @BeforeEach
    void setUp() {
        Map<String, CreditorInstitution> map = new HashMap<>();
        map.put("77777777777", new CreditorInstitution());

        when(configCacheService.getCreditorInstitutions()).thenReturn(map);
    }

    @Test
    void contextLoads() {
        assertTrue(true);
    }
}