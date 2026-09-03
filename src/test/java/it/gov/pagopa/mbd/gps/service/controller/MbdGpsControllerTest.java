//package it.gov.pagopa.mbd.gps.service.controller;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//import it.gov.pagopa.mbd.gps.service.exception.AppError;
//import it.gov.pagopa.mbd.gps.service.exception.AppException;
//import it.gov.pagopa.mbd.gps.service.model.StOutcome;
//import it.gov.pagopa.mbd.gps.service.model.partner.PaDemandPaymentNoticeRequest;
//import it.gov.pagopa.mbd.gps.service.model.partner.PaDemandPaymentNoticeResponse;
//import it.gov.pagopa.mbd.gps.service.service.MbdGpsService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//@WebMvcTest(controllers = MbdGpsController.class)
//@ActiveProfiles("local")
//class MbdGpsControllerTest {
//
//  @Autowired private MockMvc mockMvc;
//
//  @MockBean private MbdGpsService mbdGpsService;
//
//  @Test
//  @DisplayName("POST /mbd/paymentOption - Success (201 Created)")
//  void createPaymentOption_Success() throws Exception {
//    String xmlRequest = createDummyXmlRequest();
//
//    PaDemandPaymentNoticeResponse mockResponse = new PaDemandPaymentNoticeResponse();
//    mockResponse.setOutcome(StOutcome.OK);
//    mockResponse.setFiscalCodePA("77777777777");
//
//    when(mbdGpsService.createDebtPosition(any(PaDemandPaymentNoticeRequest.class)))
//            .thenReturn(mockResponse);
//
//    mockMvc
//            .perform(
//                    post("/mbd/paymentOption")
//                            .contentType(MediaType.APPLICATION_XML)
//                            .accept(MediaType.APPLICATION_XML)
//                            .content(xmlRequest))
//            .andExpect(status().isCreated())
//            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_XML));
//
//    verify(mbdGpsService).createDebtPosition(any(PaDemandPaymentNoticeRequest.class));
//  }
//
//  @Test
//  @DisplayName("POST /mbd/paymentOption - KO: malformed XML (400 Bad Request)")
//  void createPaymentOption_BadRequest() throws Exception {
//    mockMvc
//            .perform(
//                    post("/mbd/paymentOption")
//                            .contentType(MediaType.APPLICATION_XML)
//                            .accept(MediaType.APPLICATION_XML)
//                            .content("<paDemandPaymentNoticeRequest><idPA>"))
//            .andExpect(status().isBadRequest());
//  }
//
//  @Test
//  @DisplayName("POST /mbd/paymentOption - KO: Ente Not Exist (404 Not Found)")
//  void createPaymentOption_NotFound() throws Exception {
//    String xmlRequest = createDummyXmlRequest();
//
//    when(mbdGpsService.createDebtPosition(any(PaDemandPaymentNoticeRequest.class)))
//            .thenThrow(new AppException(AppError.CREDITOR_INSTITUTION_NOT_FOUND));
//
//    mockMvc
//            .perform(
//                    post("/mbd/paymentOption")
//                            .contentType(MediaType.APPLICATION_XML)
//                            .accept(MediaType.APPLICATION_XML)
//                            .content(xmlRequest))
//            .andExpect(status().isNotFound());
//  }
//
//  @Test
//  @DisplayName("POST /mbd/paymentOption - KO: internal Error (500 Internal Server Error)")
//  void createPaymentOption_InternalServerError() throws Exception {
//    String xmlRequest = createDummyXmlRequest();
//
//    when(mbdGpsService.createDebtPosition(any(PaDemandPaymentNoticeRequest.class)))
//            .thenThrow(new RuntimeException("Generic Error"));
//
//    mockMvc
//            .perform(
//                    post("/mbd/paymentOption")
//                            .contentType(MediaType.APPLICATION_XML)
//                            .accept(MediaType.APPLICATION_XML)
//                            .content(xmlRequest))
//            .andExpect(status().isInternalServerError());
//  }
//
//  private String createDummyXmlRequest() {
//    return """
//        <paDemandPaymentNoticeRequest>
//          <idPA>77777777777</idPA>
//          <idBrokerPA>77777777777</idBrokerPA>
//          <idStation>station1</idStation>
//          <datiSpecificiServizioRequest>
//            PHNlcnZpY2U+PGFtb3VudD4xNjwvYW1vdW50PjxkZWJ0b3JOYW1lPk1hcmlvPC9kZWJ0b3JOYW1lPjxkZWJ0b3JTdXJuYW1lPlJvc3NpPC9kZWJ0b3JTdXJuYW1lPjxkZWJ0b3JFbWFpbD5tYXJpby5yb3NzaUBleGFtcGxlLmNvbTwvZGVidG3yRW1haWw+PGRlYnRvckZpc2NhZENvZGU+UlNTTVJBODVUMTBINTAxWjwvZGVidG9yRmlzY2FsQ29kZT48Y2lGaXNjYWxDb2RlPjc3Nzc3Nzc3Nzc3PC9jaUZpc2NhZENvZGU+PGRlYnRvclByb3ZpbmNlPk1JPC9kZWJ0b3JQcm92aW5jZT48ZG9jdW1lbnRIYXNoPjQ3REVRcGo4SEJTYSsvVEltVys1SkNldVFlUmttNU5NcEpXWkczaFN1RlU9PC9kb2N1bWVudEhhc2g+PC9zZXJ2aWNlPg==
//          </datiSpecificiServizioRequest>
//        </paDemandPaymentNoticeRequest>
//        """;
//  }
//}