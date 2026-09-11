package it.gov.pagopa.mbd.gps.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.mbd.gps.service.client.GpdClient;
import it.gov.pagopa.mbd.gps.service.model.cache.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.model.client.InstallmentModel;
import it.gov.pagopa.mbd.gps.service.model.client.PaymentOptionModelV3;
import it.gov.pagopa.mbd.gps.service.model.client.PaymentPositionModelV3;
import it.gov.pagopa.mbd.gps.service.model.client.TransferModel;
import it.gov.pagopa.mbd.gps.service.model.marcadabollo.TipoMarcaDaBollo;
import it.gov.pagopa.mbd.gps.service.model.partner.PaDemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.gps.service.model.partner.PaDemandPaymentNoticeResponse;
import it.gov.pagopa.mbd.gps.service.model.partner.StOutcome;
import it.gov.pagopa.noticenumber.model.NoticeNumberGenerationResponse;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MbdGpsServiceTest {

  @Mock private ConfigCacheService configCacheService;

  @Mock private GpdClient gpdClient;

  @Mock private NoticeNumberGeneratorService noticeNumberGeneratorService;

  private MbdGpsService mbdGpsService;

  private static final String CI_FISCAL_CODE = "77777777777";
  private static final String NAV = "311111111111111111";

  @BeforeEach
  void setUp() {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    mbdGpsService =
        new MbdGpsService(configCacheService, gpdClient, noticeNumberGeneratorService, validator);
    ReflectionTestUtils.setField(mbdGpsService, "dueDateDays", 30);
    ReflectionTestUtils.setField(mbdGpsService, "category", "9/0101108TS/");
    ReflectionTestUtils.setField(mbdGpsService, "description", "Marca da bollo digitale");
    ReflectionTestUtils.setField(
        mbdGpsService, "remittanceInformation", "Pagamento marca da bollo");
  }

  @Test
  @DisplayName("createDebtPosition - Success: generates NAV and creates debt position in GPD V3")
  void createDebtPosition_Success() {
    PaDemandPaymentNoticeRequest request = buildRequest(validMarcaDaBolloXml());

    Map<String, CreditorInstitution> creditorInstitutions = new HashMap<>();
    CreditorInstitution creditor =
        CreditorInstitution.builder().businessName("Comune di Test").build();
    creditorInstitutions.put(CI_FISCAL_CODE, creditor);
    when(configCacheService.getCreditorInstitutions()).thenReturn(creditorInstitutions);

    NoticeNumberGenerationResponse navResponse = new NoticeNumberGenerationResponse();
    navResponse.setNoticeNumber(NAV);
    when(noticeNumberGeneratorService.generateNoticeNumber(CI_FISCAL_CODE)).thenReturn(navResponse);

    when(gpdClient.createDebtPosition(
            eq(CI_FISCAL_CODE), any(PaymentPositionModelV3.class), eq(true), anyString()))
        .thenReturn(buildGpdResponse());

    PaDemandPaymentNoticeResponse response =
        unmarshalResponse(mbdGpsService.createDebtPosition(request));

    assertThat(response.getOutcome()).isEqualTo(StOutcome.OK);
    assertThat(response.getFiscalCodePA()).isEqualTo(CI_FISCAL_CODE);
    assertThat(response.getQrCode().getNoticeNumber()).isEqualTo(NAV);

    verify(configCacheService).getCreditorInstitutions();
    verify(noticeNumberGeneratorService).generateNoticeNumber(CI_FISCAL_CODE);
    verify(gpdClient)
        .createDebtPosition(
            eq(CI_FISCAL_CODE), any(PaymentPositionModelV3.class), eq(true), anyString());
  }

  @Test
  @DisplayName("createDebtPosition - KO: invalid marcaDaBollo payload (PPT_SINTASSI_EXTRAXSD)")
  void createDebtPosition_InvalidPayload() {
    PaDemandPaymentNoticeRequest request = buildRequest(invalidMarcaDaBolloXml());

    PaDemandPaymentNoticeResponse response =
        unmarshalResponse(mbdGpsService.createDebtPosition(request));

    assertThat(response.getOutcome()).isEqualTo(StOutcome.KO);
    assertThat(response.getFault().getFaultCode()).isEqualTo("PPT_SINTASSI_EXTRAXSD");

    verify(configCacheService, never()).getCreditorInstitutions();
  }

  @Test
  @DisplayName(
      "createDebtPosition - KO: Creditor Institution not configured (PAA_ID_DOMINIO_ERRATO)")
  void createDebtPosition_CreditorInstitutionNotFound() {
    PaDemandPaymentNoticeRequest request = buildRequest(validMarcaDaBolloXml());

    when(configCacheService.getCreditorInstitutions()).thenReturn(new HashMap<>());

    PaDemandPaymentNoticeResponse response =
        unmarshalResponse(mbdGpsService.createDebtPosition(request));

    assertThat(response.getOutcome()).isEqualTo(StOutcome.KO);
    assertThat(response.getFault().getFaultCode()).isEqualTo("PAA_ID_DOMINIO_ERRATO");
  }

  @Test
  @DisplayName("createDebtPosition - KO: unexpected error while calling GPD (PAA_SYSTEM_ERROR)")
  void createDebtPosition_UnexpectedError() {
    PaDemandPaymentNoticeRequest request = buildRequest(validMarcaDaBolloXml());

    Map<String, CreditorInstitution> creditorInstitutions = new HashMap<>();
    creditorInstitutions.put(
        CI_FISCAL_CODE, CreditorInstitution.builder().businessName("Comune di Test").build());
    when(configCacheService.getCreditorInstitutions()).thenReturn(creditorInstitutions);

    when(noticeNumberGeneratorService.generateNoticeNumber(CI_FISCAL_CODE))
        .thenThrow(new RuntimeException("NAV generation failed"));

    PaDemandPaymentNoticeResponse response =
        unmarshalResponse(mbdGpsService.createDebtPosition(request));

    assertThat(response.getOutcome()).isEqualTo(StOutcome.KO);
    assertThat(response.getFault().getFaultCode()).isEqualTo("PAA_SYSTEM_ERROR");
  }

  @Test
  void testUnmarshalMarcaDaBollo() throws Exception {
    String base64Xml =
        "PG1hcmNhRGFCb2xsbyB4bWxucz0iaHR0cDovL3d3dy5hZ2VuemlhZW50cmF0ZS5nb3YuaXQvMjAxNC9NYXJjYURhQm9sbG8iPgogIDxhbW91bnQ+MTYuMDA8L2Ftb3VudD4KICA8ZGVidG9yPgogICAgPHVuaXF1ZUlkZW50aWZpZXI+CiAgICAgIDxlbnRpdHlVbmlxdWVJZGVudGlmaWVyVHlwZT5GPC9lbnRpdHlVbmlxdWVJZGVudGlmaWVyVHlwZT4KICAgICAgPGVudGl0eVVuaXF1ZUlkZW50aWZpZXJWYWx1ZT5QTFRQUFAwMFIxMEg1MDFPPC9lbnRpdHlVbmlxdWVJZGVudGlmaWVyVmFsdWU+CiAgICA8L3VuaXF1ZUlkZW50aWZpZXI+CiAgICA8ZnVsbE5hbWU+UGlwcG8gUGx1dG88L2Z1bGxOYW1lPgogICAgPGVtYWlsPnBpcHBvLnBsdXRvQHBhcGVyaW5vLml0PC9lbWFpbD4KICA8L2RlYnRvcj4KICA8ZmlzY2FsQ29kZT43Nzc3Nzc3Nzc3NzwvZmlzY2FsQ29kZT4KICA8cHJvdmluY2U+TUk8L3Byb3ZpbmNlPgogIDxkb2N1bWVudEhhc2g+YWY0V1l5U1lPQ2E2WGdrK0J5eEl2eHVhUHN4MUplclJnaXBQMXhlTThiST08L2RvY3VtZW50SGFzaD4KPC9tYXJjYURhQm9sbG8+";

    byte[] xmlBytes = Base64.getDecoder().decode(base64Xml);
    TipoMarcaDaBollo result = mbdGpsService.unmarshalMarcaDaBollo(xmlBytes);

    assertNotNull(result);
    assertEquals(new BigDecimal("16.00"), result.getAmount());
  }

  @Test
  @DisplayName("createDebtPosition - KO: XML con elemento sconosciuto (PPT_SINTASSI_EXTRAXSD)")
  void createDebtPosition_UnknownXsdElement() {
    String xmlWithUnknownElement =
        """
        <marcaDaBollo xmlns="http://www.agenziaentrate.gov.it/2014/MarcaDaBollo">
          <amount>16.00</amount>
          <unsupportedTag>VALORE_NON_VALIDO</unsupportedTag>
          <debtor>
            <uniqueIdentifier>
              <entityUniqueIdentifierType>F</entityUniqueIdentifierType>
              <entityUniqueIdentifierValue>RSSMRA85T10H501Z</entityUniqueIdentifierValue>
            </uniqueIdentifier>
            <fullName>Mario Rossi</fullName>
            <email>mario.rossi@example.com</email>
          </debtor>
          <fiscalCode>%s</fiscalCode>
          <province>MI</province>
          <documentHash>47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=</documentHash>
        </marcaDaBollo>
        """
            .formatted(CI_FISCAL_CODE);

    PaDemandPaymentNoticeRequest request = buildRequest(xmlWithUnknownElement);
    PaDemandPaymentNoticeResponse response =
        unmarshalResponse(mbdGpsService.createDebtPosition(request));

    assertThat(response.getOutcome()).isEqualTo(StOutcome.KO);
    assertThat(response.getFault().getFaultCode()).isEqualTo("PPT_SINTASSI_EXTRAXSD");
    assertThat(response.getFault().getFaultString()).contains("unsupportedTag");

    verify(configCacheService, never()).getCreditorInstitutions();
    verify(gpdClient, never())
        .createDebtPosition(anyString(), any(PaymentPositionModelV3.class), eq(true), anyString());
  }

  private PaDemandPaymentNoticeResponse unmarshalResponse(String xml) {
    try {
      JAXBContext context =
          JAXBContext.newInstance(
              PaDemandPaymentNoticeResponse.class.getPackageName(),
              PaDemandPaymentNoticeResponse.class.getClassLoader());
      Unmarshaller unmarshaller = context.createUnmarshaller();
      return unmarshaller
          .unmarshal(new StreamSource(new StringReader(xml)), PaDemandPaymentNoticeResponse.class)
          .getValue();
    } catch (JAXBException e) {
      throw new IllegalStateException("Unable to unmarshal PaDemandPaymentNoticeResponse", e);
    }
  }

  private PaDemandPaymentNoticeRequest buildRequest(String marcaDaBolloXml) {
    PaDemandPaymentNoticeRequest request = new PaDemandPaymentNoticeRequest();
    request.setIdPA(CI_FISCAL_CODE);
    request.setIdBrokerPA(CI_FISCAL_CODE);
    request.setIdStation("station1");
    request.setIdServizio("EBOLLO");
    request.setIdSoggettoServizio(CI_FISCAL_CODE);
    request.setDatiSpecificiServizioRequest(marcaDaBolloXml.getBytes(StandardCharsets.UTF_8));
    return request;
  }

  private String validMarcaDaBolloXml() {
    // FIX: Rispettato l'ordine XSD ufficiale (amount -> debtor -> fiscalCode -> province ->
    // documentHash)
    return """
        <marcaDaBollo xmlns="http://www.agenziaentrate.gov.it/2014/MarcaDaBollo">
          <amount>16.00</amount>
          <debtor>
            <uniqueIdentifier>
              <entityUniqueIdentifierType>F</entityUniqueIdentifierType>
              <entityUniqueIdentifierValue>RSSMRA85T10H501Z</entityUniqueIdentifierValue>
            </uniqueIdentifier>
            <fullName>Mario Rossi</fullName>
            <email>mario.rossi@example.com</email>
          </debtor>
          <fiscalCode>%s</fiscalCode>
          <province>MI</province>
          <documentHash>47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=</documentHash>
        </marcaDaBollo>
        """
        .formatted(CI_FISCAL_CODE);
  }

  /**
   * Amount is zero (must be positive) and debtor's fullName is missing: violates two constraints.
   */
  private String invalidMarcaDaBolloXml() {
    return """
        <marcaDaBollo xmlns="http://www.agenziaentrate.gov.it/2014/MarcaDaBollo">
          <amount>0</amount>
          <province>MI</province>
          <fiscalCode>%s</fiscalCode>
          <documentHash>47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=</documentHash>
          <debtor>
            <uniqueIdentifier>
              <entityUniqueIdentifierType>F</entityUniqueIdentifierType>
              <entityUniqueIdentifierValue>RSSMRA85T10H501Z</entityUniqueIdentifierValue>
            </uniqueIdentifier>
            <fullName></fullName>
            <email>mario.rossi@example.com</email>
          </debtor>
        </marcaDaBollo>
        """
        .formatted(CI_FISCAL_CODE);
  }

  private PaymentPositionModelV3 buildGpdResponse() {
    PaymentPositionModelV3 gpdResponse = new PaymentPositionModelV3();
    gpdResponse.setIupd("MBD2025_" + NAV);
    gpdResponse.setCompanyName("Comune di Test");

    PaymentOptionModelV3 paymentOption = new PaymentOptionModelV3();
    paymentOption.setDescription("Marca da bollo digitale");

    InstallmentModel installment = new InstallmentModel();
    installment.setNav(NAV);
    installment.setAmount(1600L);

    TransferModel transfer = new TransferModel();
    transfer.setOrganizationFiscalCode(CI_FISCAL_CODE);

    installment.getTransfer().add(transfer);
    paymentOption.getInstallments().add(installment);
    gpdResponse.getPaymentOption().add(paymentOption);
    return gpdResponse;
  }
}
