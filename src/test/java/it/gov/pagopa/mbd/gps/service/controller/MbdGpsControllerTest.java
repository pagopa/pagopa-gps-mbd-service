package it.gov.pagopa.mbd.gps.service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequest;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequestProperties;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionResponse;
import it.gov.pagopa.mbd.gps.service.model.PaymentOption;
import it.gov.pagopa.mbd.gps.service.model.Transfer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@SpringBootTest
class MbdGpsControllerTest {

  @Autowired private MockMvc mvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void buildMbdPaymentOptionTestSuccess() throws Exception {
    MbdPaymentOptionRequest request = buildMbdPaymentOptionRequest();

    MvcResult result =
        mvc.perform(
                post("/mbd/paymentOption")
                    .content(objectMapper.writeValueAsString(request))
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().is2xxSuccessful())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();

    String json = result.getResponse().getContentAsString();

    assertNotNull(json);

    MbdPaymentOptionResponse response =
        objectMapper.readValue(json, MbdPaymentOptionResponse.class);

    assertNotNull(response);
    assertNotNull(response.getPaymentOption());
    assertEquals(1, response.getPaymentOption().size());

    PaymentOption paymentOption = response.getPaymentOption().get(0);
    assertEquals(request.getProperties().getAmount(), paymentOption.getAmount());
    assertNotNull(paymentOption.getDescription());
    assertNotNull(paymentOption.getDueDate());
    assertNotNull(paymentOption.getRetentionDate());
    assertFalse(paymentOption.getIsPartialPayment());
    assertNotNull(paymentOption.getTransfer());
    assertEquals(1, paymentOption.getTransfer().size());

    Transfer transfer = paymentOption.getTransfer().get(0);
    assertEquals(request.getProperties().getAmount(), transfer.getAmount());
    assertEquals(request.getProperties().getFiscalCode(), transfer.getOrganizationFiscalCode());
    assertEquals("1", transfer.getIdTransfer());
    assertNotNull(transfer.getRemittanceInformation());
    assertNotNull(transfer.getStamp());
    assertEquals(
        request.getProperties().getProvincialResidence(),
        transfer.getStamp().getProvincialResidence());
    assertEquals(request.getProperties().getDocumentHash(), transfer.getStamp().getHashDocument());
    assertEquals("st", transfer.getStamp().getStampType());
  }

  @Test
  void buildMbdPaymentOptionTestFailAmountMissing() throws Exception {
    MbdPaymentOptionRequest request = buildMbdPaymentOptionRequest();
    request.getProperties().setAmount(null);

    mvc.perform(
            post("/mbd/paymentOption")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void buildMbdPaymentOptionTestFailFirstNameMissing() throws Exception {
    MbdPaymentOptionRequest request = buildMbdPaymentOptionRequest();
    request.getProperties().setFirstName(null);

    mvc.perform(
            post("/mbd/paymentOption")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void buildMbdPaymentOptionTestFailLastNameMissing() throws Exception {
    MbdPaymentOptionRequest request = buildMbdPaymentOptionRequest();
    request.getProperties().setLastName(null);

    mvc.perform(
            post("/mbd/paymentOption")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void buildMbdPaymentOptionTestFailFiscalCodeMissing() throws Exception {
    MbdPaymentOptionRequest request = buildMbdPaymentOptionRequest();
    request.getProperties().setFiscalCode(null);

    mvc.perform(
            post("/mbd/paymentOption")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void buildMbdPaymentOptionTestFailProvincialResidenceMissing() throws Exception {
    MbdPaymentOptionRequest request = buildMbdPaymentOptionRequest();
    request.getProperties().setProvincialResidence(null);

    mvc.perform(
            post("/mbd/paymentOption")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void buildMbdPaymentOptionTestFailDocumentHashMissing() throws Exception {
    MbdPaymentOptionRequest request = buildMbdPaymentOptionRequest();
    request.getProperties().setDocumentHash(null);

    mvc.perform(
            post("/mbd/paymentOption")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void buildMbdPaymentOptionTestFailDocumentHashWrongSize() throws Exception {
    MbdPaymentOptionRequest request = buildMbdPaymentOptionRequest();
    request.getProperties().setDocumentHash("asdfsdf");

    mvc.perform(
            post("/mbd/paymentOption")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }

  private MbdPaymentOptionRequest buildMbdPaymentOptionRequest() {
    return MbdPaymentOptionRequest.builder()
        .properties(
            MbdPaymentOptionRequestProperties.builder()
                .amount(16L)
                .firstName("Mario")
                .lastName("Rossi")
                .fiscalCode("0000000000000000")
                .provincialResidence("AS")
                .documentHash("1trA5qyjSZNwiwtGG46dyjRpL16TFgGCFvnfFzQrFHbB")
                .build())
        .build();
  }
}
