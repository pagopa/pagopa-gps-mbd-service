package it.gov.pagopa.mbd.gps.service.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.gov.pagopa.mbd.gps.service.model.cache.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.service.ConfigCacheService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@TestPropertySource(
    properties = {
      "apiConfigCacheClient.url=http://localhost:8080",
      "service.gpd.host=http://localhost:8080"
    })
class HomeControllerTest {

  @Autowired private MockMvc mvc;

  @MockBean private ConfigCacheService configCacheService;

  @BeforeEach
  void setUp() {
    Map<String, CreditorInstitution> map = new HashMap<>();
    map.put("77777777777", new CreditorInstitution());

    when(configCacheService.getCreditorInstitutions()).thenReturn(map);
  }

  @Test
  void healthCheckTestSuccess() throws Exception {
    mvc.perform(get("/info")).andExpect(status().isOk());
  }

  @Test
  void homeTestSuccess() throws Exception {
    mvc.perform(get("/"))
        .andExpect(status().is3xxRedirection()); // Usato "/" al posto di stringa vuota
  }
}
