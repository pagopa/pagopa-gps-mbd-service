package it.gov.pagopa.mbd.gps.service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mbd.gps.service.exception.AppError;
import it.gov.pagopa.mbd.gps.service.exception.AppException;
import it.gov.pagopa.mbd.gps.service.model.DebtPositionResponse;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequest;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequestProperties;
import it.gov.pagopa.mbd.gps.service.service.MbdGpsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = MbdGpsController.class)
@ActiveProfiles("local")
class MbdGpsControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private MbdGpsService mbdGpsService;

  @Test
  @DisplayName("POST /mbd/paymentOption - Success (200 OK)")
  void createPaymentOption_Success() throws Exception {
    MbdPaymentOptionRequest request = createDummyRequest();

    String noticeNumber = "311111111111111111";
    String fiscalCode = request.getProperties().getDebtorFiscalCode();
    String remittanceInformation =
        String.format("/RFB/%s/CNR/%s/TXT/Marca da bollo digitale", noticeNumber, fiscalCode);

    DebtPositionResponse mockResponse =
        DebtPositionResponse.builder()
            .noticeNumber(noticeNumber)
            .companyName("Comune di Test")
            .description(remittanceInformation)
            .build();

    when(mbdGpsService.createDebtPosition(any(MbdPaymentOptionRequest.class)))
        .thenReturn(mockResponse);

    mockMvc
        .perform(
            post("/mbd/paymentOption")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    verify(mbdGpsService).createDebtPosition(any(MbdPaymentOptionRequest.class));
  }

  @Test
  @DisplayName("POST /mbd/paymentOption - KO: malformed JSON (400 Bad Request)")
  void createPaymentOption_BadRequest() throws Exception {
    mockMvc
        .perform(
            post("/mbd/paymentOption")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"properties\": {"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /mbd/paymentOption - KO: Ente Not Exist (404 Not Found)")
  void createPaymentOption_NotFound() throws Exception {
    MbdPaymentOptionRequest request = createDummyRequest();

    when(mbdGpsService.createDebtPosition(any(MbdPaymentOptionRequest.class)))
        .thenThrow(new AppException(AppError.CREDITOR_INSTITUTION_NOT_FOUND));

    mockMvc
        .perform(
            post("/mbd/paymentOption")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /mbd/paymentOption - KO: internal Error (500 Internal Server Error)")
  void createPaymentOption_InternalServerError() throws Exception {
    MbdPaymentOptionRequest request = createDummyRequest();

    when(mbdGpsService.createDebtPosition(any(MbdPaymentOptionRequest.class)))
        .thenThrow(new RuntimeException("Generic Error"));

    mockMvc
        .perform(
            post("/mbd/paymentOption")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isInternalServerError());
  }

  private MbdPaymentOptionRequest createDummyRequest() {
    MbdPaymentOptionRequest request = new MbdPaymentOptionRequest();
    MbdPaymentOptionRequestProperties props = new MbdPaymentOptionRequestProperties();
    props.setAmount(16L);
    props.setDebtorName("Mario");
    props.setDebtorSurname("Rossi");
    props.setDebtorEmail("mario.rossi@example.com");
    props.setDebtorFiscalCode("RSSMRA85T10H501Z");
    props.setCiFiscalCode("77777777777");
    props.setDebtorProvince("MI");
    props.setDocumentHash("47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
    request.setProperties(props);
    return request;
  }
}
