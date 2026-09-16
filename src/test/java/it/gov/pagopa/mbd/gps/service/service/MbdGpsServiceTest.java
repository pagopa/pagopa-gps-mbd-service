package it.gov.pagopa.mbd.gps.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import it.gov.pagopa.mbd.gps.service.client.GpdClient;
import it.gov.pagopa.mbd.gps.service.exception.MarcaDaBolloValidationException;
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
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    mbdGpsService = new MbdGpsService(configCacheService, gpdClient, noticeNumberGeneratorService);
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
    String xml =
        """
            <mbd:marcaDaBollo xmlns:mbd="http://pagopa-api.pagopa.gov.it/pa/MarcaDaBollo">
              <amount>16.00</amount>
              <debtor>
                <uniqueIdentifier>
                  <entityUniqueIdentifierType>F</entityUniqueIdentifierType>
                  <entityUniqueIdentifierValue>PLTPPP00R10H501O</entityUniqueIdentifierValue>
                </uniqueIdentifier>
                <fullName>Pippo Pluto</fullName>
                <email>pippo.pluto@paperino.it</email>
              </debtor>
              <fiscalCode>77777777777</fiscalCode>
              <province>MI</province>
              <documentHash>af4WYySYOCa6Xgk+ByxIvxuaPsx1JerRgipP1xeM8bI=</documentHash>
            </mbd:marcaDaBollo>
            """;

    byte[] xmlBytes = xml.getBytes(StandardCharsets.UTF_8);
    TipoMarcaDaBollo result = mbdGpsService.unmarshalMarcaDaBollo(xmlBytes);

    assertNotNull(result);
    assertEquals(new BigDecimal("16.00"), result.getAmount());
  }

  @Test
  @DisplayName("createDebtPosition - KO: malformed XML content in datiSpecificiServizioRequest")
  void createDebtPosition_MalformedXmlContent() {
    PaDemandPaymentNoticeRequest request = buildRequest("<<<XML_NON_VALIDO>>>");

    PaDemandPaymentNoticeResponse response =
        unmarshalResponse(mbdGpsService.createDebtPosition(request));

    assertThat(response.getOutcome()).isEqualTo(StOutcome.KO);
    assertThat(response.getFault().getFaultCode()).isEqualTo("PPT_SINTASSI_EXTRAXSD");

    verify(configCacheService, never()).getCreditorInstitutions();
  }

  @Test
  @DisplayName("checkMarcaDaBollo - KO: debtor is null")
  void checkMarcaDaBollo_NullDebtor() {
    TipoMarcaDaBollo marcaDaBollo = new TipoMarcaDaBollo();
    marcaDaBollo.setAmount(new BigDecimal("16.00"));
    marcaDaBollo.setDebtor(null);

    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(marcaDaBollo))
        .isInstanceOf(MarcaDaBolloValidationException.class);
  }

  @Test
  @DisplayName("createDebtPosition - KO: GPD client throws AppException (PAA_SYSTEM_ERROR)")
  void createDebtPosition_GpdClientException() {
    PaDemandPaymentNoticeRequest request = buildRequest(validMarcaDaBolloXml());

    Map<String, CreditorInstitution> creditorInstitutions = new HashMap<>();
    creditorInstitutions.put(
        CI_FISCAL_CODE, CreditorInstitution.builder().businessName("Comune di Test").build());
    when(configCacheService.getCreditorInstitutions()).thenReturn(creditorInstitutions);

    NoticeNumberGenerationResponse navResponse = new NoticeNumberGenerationResponse();
    navResponse.setNoticeNumber(NAV);
    when(noticeNumberGeneratorService.generateNoticeNumber(CI_FISCAL_CODE)).thenReturn(navResponse);

    when(gpdClient.createDebtPosition(
            eq(CI_FISCAL_CODE), any(PaymentPositionModelV3.class), eq(true), anyString()))
        .thenThrow(new RuntimeException("GPD connection failed"));

    PaDemandPaymentNoticeResponse response =
        unmarshalResponse(mbdGpsService.createDebtPosition(request));

    assertThat(response.getOutcome()).isEqualTo(StOutcome.KO);
    assertThat(response.getFault().getFaultCode()).isEqualTo("PAA_SYSTEM_ERROR");
  }

  @Test
  @DisplayName("createDebtPosition - KO: null response from GPD client")
  void createDebtPosition_NullGpdResponse() {
    PaDemandPaymentNoticeRequest request = buildRequest(validMarcaDaBolloXml());

    Map<String, CreditorInstitution> creditorInstitutions = new HashMap<>();
    creditorInstitutions.put(
        CI_FISCAL_CODE, CreditorInstitution.builder().businessName("Comune di Test").build());
    when(configCacheService.getCreditorInstitutions()).thenReturn(creditorInstitutions);

    NoticeNumberGenerationResponse navResponse = new NoticeNumberGenerationResponse();
    navResponse.setNoticeNumber(NAV);
    when(noticeNumberGeneratorService.generateNoticeNumber(CI_FISCAL_CODE)).thenReturn(navResponse);

    when(gpdClient.createDebtPosition(
            eq(CI_FISCAL_CODE), any(PaymentPositionModelV3.class), eq(true), anyString()))
        .thenReturn(null);

    PaDemandPaymentNoticeResponse response =
        unmarshalResponse(mbdGpsService.createDebtPosition(request));

    assertThat(response.getOutcome()).isEqualTo(StOutcome.KO);
    assertThat(response.getFault().getFaultCode()).isEqualTo("PAA_SYSTEM_ERROR");
  }

  @Test
  @DisplayName("unmarshalMarcaDaBollo - KO: invalid XML bytes throw UnmarshalException")
  void unmarshalMarcaDaBollo_InvalidXml() {
    byte[] invalidBytes = "<invalid>xml</invalid>".getBytes(StandardCharsets.UTF_8);

    assertThatThrownBy(() -> mbdGpsService.unmarshalMarcaDaBollo(invalidBytes))
        .isInstanceOf(jakarta.xml.bind.UnmarshalException.class);
  }

  @Test
  @DisplayName("createDebtPosition - KO: GPD response contains empty paymentOptions list")
  void createDebtPosition_EmptyPaymentOptions() {
    PaDemandPaymentNoticeRequest request = buildRequest(validMarcaDaBolloXml());

    Map<String, CreditorInstitution> creditorInstitutions = new HashMap<>();
    creditorInstitutions.put(
        CI_FISCAL_CODE, CreditorInstitution.builder().businessName("Comune di Test").build());
    when(configCacheService.getCreditorInstitutions()).thenReturn(creditorInstitutions);

    NoticeNumberGenerationResponse navResponse = new NoticeNumberGenerationResponse();
    navResponse.setNoticeNumber(NAV);
    when(noticeNumberGeneratorService.generateNoticeNumber(CI_FISCAL_CODE)).thenReturn(navResponse);

    PaymentPositionModelV3 emptyGpdResponse = new PaymentPositionModelV3();
    when(gpdClient.createDebtPosition(
            eq(CI_FISCAL_CODE), any(PaymentPositionModelV3.class), eq(true), anyString()))
        .thenReturn(emptyGpdResponse);

    PaDemandPaymentNoticeResponse response =
        unmarshalResponse(mbdGpsService.createDebtPosition(request));

    assertThat(response.getOutcome()).isEqualTo(StOutcome.KO);
    assertThat(response.getFault().getFaultCode()).isEqualTo("PAA_SYSTEM_ERROR");
  }

  @Test
  @DisplayName("checkMarcaDaBollo - KO: debtor uniqueIdentifier is null or invalid pattern")
  void checkMarcaDaBollo_InvalidUniqueIdentifier() throws Exception {
    String xmlInvalidCf =
        """
      <mbd:marcaDaBollo xmlns:mbd="http://pagopa-api.pagopa.gov.it/pa/MarcaDaBollo">
        <amount>16.00</amount>
        <debtor>
          <uniqueIdentifier>
            <entityUniqueIdentifierType>F</entityUniqueIdentifierType>
            <entityUniqueIdentifierValue>INVALID_CF_123</entityUniqueIdentifierValue>
          </uniqueIdentifier>
          <fullName>Mario Rossi</fullName>
          <email>mario.rossi@example.com</email>
        </debtor>
        <fiscalCode>77777777777</fiscalCode>
        <province>MI</province>
        <documentHash>47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=</documentHash>
      </mbd:marcaDaBollo>
      """;

    TipoMarcaDaBollo mb =
        mbdGpsService.unmarshalMarcaDaBollo(xmlInvalidCf.getBytes(StandardCharsets.UTF_8));

    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(mb))
        .isInstanceOf(MarcaDaBolloValidationException.class);
  }

  @Test
  @DisplayName("checkMarcaDaBollo - KO: marcaDaBollo is null or amount is null/zero/negative")
  void checkMarcaDaBollo_NullAndAmountValidationBranches() {
    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(null))
        .isInstanceOf(MarcaDaBolloValidationException.class)
        .hasMessageContaining("marcaDaBollo is required");

    TipoMarcaDaBollo mbNullAmount = new TipoMarcaDaBollo();
    mbNullAmount.setAmount(null);
    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(mbNullAmount))
        .isInstanceOf(MarcaDaBolloValidationException.class)
        .hasMessageContaining("Amount is required");

    TipoMarcaDaBollo mbZeroAmount = new TipoMarcaDaBollo();
    mbZeroAmount.setAmount(BigDecimal.ZERO);
    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(mbZeroAmount))
        .isInstanceOf(MarcaDaBolloValidationException.class)
        .hasMessageContaining("Amount is required");
  }

  @Test
  @DisplayName("checkMarcaDaBollo - KO: debtor fullName empty spaces or invalid email")
  void checkMarcaDaBollo_InvalidNameAndEmailBranches() throws Exception {
    TipoMarcaDaBollo mb =
        mbdGpsService.unmarshalMarcaDaBollo(
            validMarcaDaBolloXml().getBytes(StandardCharsets.UTF_8));

    mb.getDebtor().setFullName("   ");
    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(mb))
        .isInstanceOf(MarcaDaBolloValidationException.class)
        .hasMessageContaining("Debtor full name is required");

    mb.getDebtor().setFullName("Mario Rossi");
    mb.getDebtor().setEmail("email-non-valida");
    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(mb))
        .isInstanceOf(MarcaDaBolloValidationException.class)
        .hasMessageContaining("Debtor email is required");
  }

  @Test
  @DisplayName("checkMarcaDaBollo - KO: debtor uniqueIdentifier value is blank or null")
  void checkMarcaDaBollo_BlankUniqueIdentifierValue() throws Exception {
    TipoMarcaDaBollo mb =
        mbdGpsService.unmarshalMarcaDaBollo(
            validMarcaDaBolloXml().getBytes(StandardCharsets.UTF_8));

    // Ramo 1: Value blank/spaces
    mb.getDebtor().getUniqueIdentifier().setEntityUniqueIdentifierValue("   ");
    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(mb))
        .isInstanceOf(MarcaDaBolloValidationException.class);

    // Ramo 2: Value null
    mb.getDebtor().getUniqueIdentifier().setEntityUniqueIdentifierValue(null);
    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(mb))
        .isInstanceOf(MarcaDaBolloValidationException.class);
  }

  @Test
  @DisplayName("checkMarcaDaBollo - KO: fiscalCode or province or documentHash empty checks")
  void checkMarcaDaBollo_OtherFieldsValidation() throws Exception {
    TipoMarcaDaBollo mb =
        mbdGpsService.unmarshalMarcaDaBollo(
            validMarcaDaBolloXml().getBytes(StandardCharsets.UTF_8));

    // Ramo fiscalCode vuoto
    mb.setFiscalCode("   ");
    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(mb))
        .isInstanceOf(MarcaDaBolloValidationException.class);

    // Ramo province vuoto
    mb.setFiscalCode("77777777777");
    mb.setProvince("   ");
    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(mb))
        .isInstanceOf(MarcaDaBolloValidationException.class);

    // Ramo documentHash vuoto/null
    mb.setProvince("MI");
    mb.setDocumentHash(new byte[0]);
    assertThatThrownBy(() -> mbdGpsService.checkMarcaDaBollo(mb))
        .isInstanceOf(MarcaDaBolloValidationException.class);
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
    return """
      <mbd:marcaDaBollo xmlns:mbd="http://pagopa-api.pagopa.gov.it/pa/MarcaDaBollo">
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
      </mbd:marcaDaBollo>
      """
        .formatted(CI_FISCAL_CODE);
  }

  private String invalidMarcaDaBolloXml() {
    return """
      <mbd:marcaDaBollo xmlns:mbd="http://pagopa-api.pagopa.gov.it/pa/MarcaDaBollo">
        <amount>0.00</amount>
        <debtor>
          <uniqueIdentifier>
            <entityUniqueIdentifierType>F</entityUniqueIdentifierType>
            <entityUniqueIdentifierValue>RSSMRA85T10H501Z</entityUniqueIdentifierValue>
          </uniqueIdentifier>
          <fullName></fullName>
          <email>mario.rossi@example.com</email>
        </debtor>
        <fiscalCode>%s</fiscalCode>
        <province>MI</province>
        <documentHash>47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=</documentHash>
      </mbd:marcaDaBollo>
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
