package it.gov.pagopa.mbd.gps.service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import it.gov.pagopa.mbd.gps.service.exception.AppError;
import it.gov.pagopa.mbd.gps.service.exception.AppException;
import it.gov.pagopa.mbd.gps.service.model.partner.PaDemandPaymentNoticeRequest;
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

  @MockBean private MbdGpsService mbdGpsService;

  @Test
  @DisplayName("POST /mbd/paymentOption - Success (200 OK)")
  void createPaymentOption_Success() throws Exception {
    String xmlRequest = createDummyXmlRequest();

    String mockResponseXml =
        "<paDemandPaymentNoticeResponse><outcome>OK</outcome><fiscalCodePA>77777777777</fiscalCodePA></paDemandPaymentNoticeResponse>";

    when(mbdGpsService.createDebtPosition(any(PaDemandPaymentNoticeRequest.class)))
        .thenReturn(mockResponseXml);

    mockMvc
        .perform(
            post("/mbd/paymentOption")
                .contentType(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .content(xmlRequest))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));

    verify(mbdGpsService).createDebtPosition(any(PaDemandPaymentNoticeRequest.class));
  }

  @Test
  @DisplayName("POST /mbd/paymentOption - KO: malformed XML (400 Bad Request)")
  void createPaymentOption_BadRequest() throws Exception {
    mockMvc
        .perform(
            post("/mbd/paymentOption")
                .contentType(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .content("<paDemandPaymentNoticeRequest><idPA>"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("POST /mbd/paymentOption - KO: Ente Not Exist (404 Not Found)")
  void createPaymentOption_NotFound() throws Exception {
    String xmlRequest = createDummyXmlRequest();

    when(mbdGpsService.createDebtPosition(any(PaDemandPaymentNoticeRequest.class)))
        .thenThrow(new AppException(AppError.CREDITOR_INSTITUTION_NOT_FOUND));

    mockMvc
        .perform(
            post("/mbd/paymentOption")
                .contentType(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .content(xmlRequest))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /mbd/paymentOption - KO: internal Error (500 Internal Server Error)")
  void createPaymentOption_InternalServerError() throws Exception {
    String xmlRequest = createDummyXmlRequest();

    when(mbdGpsService.createDebtPosition(any(PaDemandPaymentNoticeRequest.class)))
        .thenThrow(new RuntimeException("Generic Error"));

    mockMvc
        .perform(
            post("/mbd/paymentOption")
                .contentType(MediaType.APPLICATION_XML)
                .accept(MediaType.APPLICATION_XML)
                .content(xmlRequest))
        .andExpect(status().isInternalServerError());
  }

  private String createDummyXmlRequest() {
    return """
            <PaDemandPaymentNoticeRequest xmlns="http://pagopa-api.pagopa.gov.it/pa/paForNode.xsd">
                                             <idPA>77777777777</idPA>
                                             <idBrokerPA>77777777777</idBrokerPA>
                                             <idStation>77777777777_01</idStation>
                                             <idServizio>00005</idServizio>
                                             <idSoggettoServizio>50000</idSoggettoServizio>
                                             <datiSpecificiServizioRequest>PG1hcmNhRGFCb2xsbyB4bWxucz0iaHR0cDovL3d3dy5hZ2VuemlhZW50cmF0ZS5nb3YuaXQvMjAxNC9NYXJjYURhQm9sbG8iIHhzaTpzY2hlbWFMb2NhdGlvbj0iaHR0cDovL3BhZ29wYS1hcGkucGFnb3BhLmdvdi5pdC9wYS9NYXJjYURhQm9sbG8ueHNkIiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MQWNjZXB0LWluc3RhbmNlIj4KICA8YW1vdW50PjE2LjAwPC9hbW91bnQ+CiAgPGZpc2NhbENvZGU+Nzc3Nzc3Nzc3Nzc8L2Zpc2NhbENvZGU+CiAgPHByb3ZpbmNlPk1JPC9wcm92aW5jZT4KICA8ZG9jdW1lbnRIYXNoPmFmNFdZeVNZT0NhNlhnaytCeXhJdnh1YVBzeDFKZXJSZ2lwUDF4ZU04Ykk9PC9kb2N1bWVudEhhc2g+CiAgPGRlYnRvcj4KICAJPHVuaXF1ZUlkZW50aWZpZXI+CiAgCQk8ZW50aXR5VW5pcXVlSWRlbnRpZmllclR5cGU+RjwvZW50aXR5VW5pcXVlSWRlbnRpZmllclR5cGU+CgkJPGVudGl0eVVuaXF1ZUlkZW50aWZpZXJWYWx1ZT5SU1NNUkE4NVQxMEg1MDFaPC9lbnRpdHlVbmlxdWVJZGVudGlmaWVyVmFsdWU+CiAgCTwvdW5pcXVlSWRlbnRpZmllcj4KICAJPGVtYWlsPm1hcmlvLnJvc3NpQGV4YW1wbGUuaXQ8L2VtYWlsPgogIDwvZGVidG9yPgo8L21hcmNhRGFCb2xsbz4=</datiSpecificiServizioRequest>
                                           </PaDemandPaymentNoticeRequest>
        """;
  }
}
