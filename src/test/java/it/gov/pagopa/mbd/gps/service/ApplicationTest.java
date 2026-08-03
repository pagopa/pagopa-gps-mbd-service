package it.gov.pagopa.mbd.gps.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
@TestPropertySource(
        properties = {
                "apiConfigCacheClient.url=http://localhost:8080",
                "service.gpd.host=http://localhost:8080"
        })
class ApplicationTest {

    @Test
    void contextLoads() {
        assertTrue(true);
    }
}
