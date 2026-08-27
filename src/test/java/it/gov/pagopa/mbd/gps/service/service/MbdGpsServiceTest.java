package it.gov.pagopa.mbd.gps.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.mbd.gps.service.client.GpdClient;
import it.gov.pagopa.mbd.gps.service.model.DebtPositionResponse;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequest;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequestProperties;
import it.gov.pagopa.mbd.gps.service.model.cache.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.model.client.PaymentPositionModelV3;
import it.gov.pagopa.noticenumber.model.NoticeNumberGenerationResponse;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MbdGpsServiceTest {

  @Mock private GpdClient gpdClient;

  @Mock private ConfigCacheService configCacheService;

  @Mock private NoticeNumberGeneratorService noticeNumberGeneratorService;

  @InjectMocks private MbdGpsService mbdGpsService;

  private MbdPaymentOptionRequest request;

  @BeforeEach
  void setUp() {
    request = createDummyRequest();
  }

  @Test
  @DisplayName(
      "createDebtPosition - Success: generating NAV and creating debtor position in GPD V3")
  void createDebtPosition_Success() {
    String fiscalCode = "77777777777";
    String noticeNumber = "352178463133495622";
    String businessName = "Comune di Test";

    CreditorInstitution ci = new CreditorInstitution();
    ci.setBusinessName(businessName);
    Map<String, CreditorInstitution> mockCreditorInstitutions = new HashMap<>();
    mockCreditorInstitutions.put(fiscalCode, ci);
    when(configCacheService.getCreditorInstitutions()).thenReturn(mockCreditorInstitutions);

    when(noticeNumberGeneratorService.generateNoticeNumber(fiscalCode))
        .thenReturn(new NoticeNumberGenerationResponse(noticeNumber));

    PaymentPositionModelV3 gpdResponse = new PaymentPositionModelV3();
    gpdResponse.setIupd("MBD_77777777777_178463133495622");

    when(gpdClient.createDebtPosition(
            eq(fiscalCode), any(PaymentPositionModelV3.class), eq(true), anyString()))
        .thenReturn(gpdResponse);

    // Act
    DebtPositionResponse response = mbdGpsService.createDebtPosition(request);

    // Assert
    assertNotNull(response);
    assertEquals(noticeNumber, response.getNoticeNumber());
    assertEquals(businessName, response.getCompanyName());

    String expectedDescriptionPattern =
        String.format(
            "/RFB/%s/CNR/%s/TXT/", noticeNumber, request.getProperties().getDebtorFiscalCode());
    assertNotNull(response.getDescription());
    assertEquals(true, response.getDescription().startsWith(expectedDescriptionPattern));

    verify(configCacheService).getCreditorInstitutions();
    verify(noticeNumberGeneratorService).generateNoticeNumber(fiscalCode);
    verify(gpdClient)
        .createDebtPosition(
            eq(fiscalCode), any(PaymentPositionModelV3.class), eq(true), anyString());
  }

  private MbdPaymentOptionRequest createDummyRequest() {
    MbdPaymentOptionRequest req = new MbdPaymentOptionRequest();
    MbdPaymentOptionRequestProperties props = new MbdPaymentOptionRequestProperties();
    props.setAmount(16L);
    props.setDebtorName("Mario");
    props.setDebtorSurname("Rossi");
    props.setDebtorEmail("mario.rossi@example.com");
    props.setDebtorFiscalCode("RSSMRA85T10H501Z");
    props.setCiFiscalCode("77777777777");
    props.setDebtorProvince("MI");
    props.setDocumentHash("47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
    req.setProperties(props);
    return req;
  }
}
