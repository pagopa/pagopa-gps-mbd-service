package it.gov.pagopa.mdb.gps.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionRequest;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionRequestProperties;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionResponse;
import it.gov.pagopa.mdb.gps.service.model.PaymentOption;
import it.gov.pagopa.mdb.gps.service.model.Transfer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class MdbGpsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMdbPaymentOptionTestSuccess() throws Exception {
        MdbPaymentOptionRequest request = buildMdbPaymentOptionRequest();

        MvcResult result = mvc.perform(post("/mbd/paymentOption")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        String json = result.getResponse().getContentAsString();

        assertNotNull(json);

        MdbPaymentOptionResponse response = objectMapper.readValue(json, MdbPaymentOptionResponse.class);

        assertNotNull(response);
        assertNotNull(response.getPaymentOption());
        assertEquals(1, response.getPaymentOption().size());

        PaymentOption paymentOption = response.getPaymentOption().get(0);
        assertEquals(request.getProperties().getFirstName(), paymentOption.getFirstName());
        assertEquals(request.getProperties().getLastName(), paymentOption.getLastName());
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
        assertEquals(request.getProperties().getProvincialResidence(), transfer.getStamp().getProvincialResidence());
        assertEquals(request.getProperties().getDocumentHash(), transfer.getStamp().getHashDocument());
        assertEquals("st", transfer.getStamp().getStampType());
    }

    @Test
    void createMdbPaymentOptionTestFailAmountMissing() throws Exception {
        MdbPaymentOptionRequest request = buildMdbPaymentOptionRequest();
        request.getProperties().setAmount(null);

        mvc.perform(post("/mbd/paymentOption")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createMdbPaymentOptionTestFailFirstNameMissing() throws Exception {
        MdbPaymentOptionRequest request = buildMdbPaymentOptionRequest();
        request.getProperties().setFirstName(null);

        mvc.perform(post("/mbd/paymentOption")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createMdbPaymentOptionTestFailLastNameMissing() throws Exception {
        MdbPaymentOptionRequest request = buildMdbPaymentOptionRequest();
        request.getProperties().setLastName(null);

        mvc.perform(post("/mbd/paymentOption")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createMdbPaymentOptionTestFailFiscalCodeMissing() throws Exception {
        MdbPaymentOptionRequest request = buildMdbPaymentOptionRequest();
        request.getProperties().setFiscalCode(null);

        mvc.perform(post("/mbd/paymentOption")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createMdbPaymentOptionTestFailProvincialResidenceMissing() throws Exception {
        MdbPaymentOptionRequest request = buildMdbPaymentOptionRequest();
        request.getProperties().setProvincialResidence(null);

        mvc.perform(post("/mbd/paymentOption")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createMdbPaymentOptionTestFailDocumentHashMissing() throws Exception {
        MdbPaymentOptionRequest request = buildMdbPaymentOptionRequest();
        request.getProperties().setDocumentHash(null);

        mvc.perform(post("/mbd/paymentOption")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    @Test
    void createMdbPaymentOptionTestFailDocumentHashWrongSize() throws Exception {
        MdbPaymentOptionRequest request = buildMdbPaymentOptionRequest();
        request.getProperties().setDocumentHash("asdfsdf");

        mvc.perform(post("/mbd/paymentOption")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());

    }

    private MdbPaymentOptionRequest buildMdbPaymentOptionRequest() {
        return MdbPaymentOptionRequest.builder()
                .properties(MdbPaymentOptionRequestProperties.builder()
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