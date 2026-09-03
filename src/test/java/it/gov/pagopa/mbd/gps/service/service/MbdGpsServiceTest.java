package it.gov.pagopa.mbd.gps.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.mbd.gps.service.client.GpdClient;
import it.gov.pagopa.mbd.gps.service.model.PaDemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.gps.service.model.PaDemandPaymentNoticeResponse;
import it.gov.pagopa.mbd.gps.service.model.StOutcome;
import it.gov.pagopa.mbd.gps.service.model.cache.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.model.client.InstallmentModel;
import it.gov.pagopa.mbd.gps.service.model.client.PaymentOptionModelV3;
import it.gov.pagopa.mbd.gps.service.model.client.PaymentPositionModelV3;
import it.gov.pagopa.mbd.gps.service.model.client.TransferModel;
import it.gov.pagopa.noticenumber.model.NoticeNumberGenerationResponse;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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

  @Mock private jakarta.validation.Validator validator;

  @InjectMocks private MbdGpsService mbdGpsService;

  @Test
  @DisplayName("createDebtPosition - Success: generating NAV and creating debtor position in GPD V3")
  void createDebtPosition_Success() {
    // 1. Arrange Request JAXB
    PaDemandPaymentNoticeRequest request = new PaDemandPaymentNoticeRequest();
    request.setIdPA("77777777777");
    request.setIdBrokerPA("77777777777");
    request.setIdStation("station1");

    String innerXml = """
        <service>
          <amount>16</amount>
          <debtorName>Mario</debtorName>
          <debtorSurname>Rossi</debtorSurname>
          <debtorEmail>mario.rossi@example.com</debtorEmail>
          <debtorFiscalCode>RSSMRA85T10H501Z</debtorFiscalCode>
          <ciFiscalCode>77777777777</ciFiscalCode>
          <debtorProvince>MI</debtorProvince>
          <documentHash>47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=</documentHash>
        </service>
        """;
    request.setDatiSpecificiServizioRequest(innerXml.getBytes(StandardCharsets.UTF_8));

    // Mock ConfigCacheService
    Map<String, CreditorInstitution> creditorInstitutions = new HashMap<>();
    CreditorInstitution creditor = new CreditorInstitution();
    creditor.setBusinessName("Comune di Test");
    creditorInstitutions.put("77777777777", creditor);
    when(configCacheService.getCreditorInstitutions()).thenReturn(creditorInstitutions);

    // Mock NoticeNumberGeneratorService
    NoticeNumberGenerationResponse navResponse = new NoticeNumberGenerationResponse();
    navResponse.setNoticeNumber("311111111111111111");
    when(noticeNumberGeneratorService.generateNoticeNumber("77777777777")).thenReturn(navResponse);

    // Mock GpdClient
    PaymentPositionModelV3 gpdResponse = new PaymentPositionModelV3();
    gpdResponse.setIupd("MBD_77777777777_178463133495622");
    gpdResponse.setCompanyName("Comune di Test");

    PaymentOptionModelV3 paymentOption = new PaymentOptionModelV3();
    paymentOption.setDescription("Marca da bollo digitale");

    InstallmentModel installment = new InstallmentModel();
    installment.setNav("311111111111111111");
    installment.setAmount(1600L);

    TransferModel transfer = new TransferModel();
    transfer.setOrganizationFiscalCode("77777777777");

    installment.getTransfer().add(transfer);
    paymentOption.getInstallments().add(installment);
    gpdResponse.getPaymentOption().add(paymentOption);

    when(gpdClient.createDebtPosition(
            eq("77777777777"), any(PaymentPositionModelV3.class), eq(true), anyString()))
            .thenReturn(gpdResponse);

    // 2. Act
    PaDemandPaymentNoticeResponse response = mbdGpsService.createDebtPosition(request);

    // 3. Assert
    assertNotNull(response);
    assertEquals(StOutcome.OK, response.getOutcome());
    assertEquals("77777777777", response.getFiscalCodePA());
    assertNotNull(response.getQrCode());
    assertEquals("311111111111111111", response.getQrCode().getNoticeNumber());

    verify(configCacheService).getCreditorInstitutions();
    verify(noticeNumberGeneratorService).generateNoticeNumber("77777777777");
    verify(gpdClient).createDebtPosition(eq("77777777777"), any(PaymentPositionModelV3.class), eq(true), anyString());
  }
}