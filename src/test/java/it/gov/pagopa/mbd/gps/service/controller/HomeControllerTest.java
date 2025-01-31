package it.gov.pagopa.mbd.gps.service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
class HomeControllerTest {

  @Autowired private MockMvc mvc;

  @Test
  void healthCheckTestSuccess() throws Exception {
    mvc.perform(get("/info")).andExpect(status().isOk());
  }

  @Test
  void homeTestSuccess() throws Exception {
    mvc.perform(get("")).andExpect(status().is3xxRedirection());
  }
}
