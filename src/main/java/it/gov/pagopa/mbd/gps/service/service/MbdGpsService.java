package it.gov.pagopa.mbd.gps.service.service;

import it.gov.pagopa.mbd.gps.service.client.GpdClient;
import it.gov.pagopa.mbd.gps.service.exception.AppError;
import it.gov.pagopa.mbd.gps.service.exception.AppException;
import it.gov.pagopa.mbd.gps.service.model.*;
import it.gov.pagopa.mbd.gps.service.model.cache.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.model.client.*;
import it.gov.pagopa.mbd.gps.service.model.marcadabollo.TipoMarcaDaBollo;
import it.gov.pagopa.mbd.gps.service.model.partner.*;
import it.gov.pagopa.noticenumber.model.NoticeNumberGenerationResponse;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling MBD payment options and creating debt positions in the GPD
 * system.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MbdGpsService {

  public static final String SERVICE_TYPE = "EBOLLO";
  private static final String MBD_PREFIX = "MBD";
  private static final String TRANSFER_STAMP_TYPE = "01";
  private static final String TRANSFER_ID = "1";
  private static final String REMITTANCE_INFORMATION_PATTERN = "/RFB/%s/CNR/%s/TXT/%s";
  private static final String ENTITY_UID_TYPE_ELEMENT = "entityUniqueIdentifierType";
  private static final String ENTITY_UID_VALUE_ELEMENT = "entityUniqueIdentifierValue";
  private static final ZoneId ROME_ZONE_ID = ZoneId.of("Europe/Rome");

  private final ConfigCacheService configCacheService;
  private final GpdClient gpdClient;
  private final NoticeNumberGeneratorService noticeNumberGeneratorService;
  private final Validator validator;

  private final ObjectFactory factory = new ObjectFactory();

  private static final JAXBContext MARCA_DA_BOLLO_CONTEXT = createMarcaDaBolloContext();

  private static JAXBContext createMarcaDaBolloContext() {
    try {
      return JAXBContext.newInstance(TipoMarcaDaBollo.class);
    } catch (JAXBException e) {
      throw new IllegalStateException("Unable to initialize marcaDaBollo JAXB context", e);
    }
  }

  @Value("${mbd.payment-position.duedate-days}")
  private int dueDateDays;

  @Value("${mbd.payment-position.category}")
  private String category;

  @Value("${mbd.payment-position.description}")
  private String description;

  @Value("${mbd.payment-position.remittance-information}")
  private String remittanceInformation;

  public JAXBElement<PaDemandPaymentNoticeResponse> createDebtPosition(
          PaDemandPaymentNoticeRequest request) {
    try {
      TipoMarcaDaBollo marcaDaBollo = unmarshalMarcaDaBollo(request.getDatiSpecificiServizioRequest());
      log.warn("Marca da bollo unmarshal result: {}", marcaDaBollo);
      try {
        validateMarcaDaBollo(marcaDaBollo);
      } catch (AppException e) {
        log.error("Validation failed for marcaDaBollo: {}", e.getMessage());
        return factory.createPaDemandPaymentNoticeResponse(createPaDemandPaymentNoticeKOResponse(request.getIdPA(), "PPT_SINTASSI_EXTRAXSD", e.getMessage()));
      }
      String ciFiscalCode = marcaDaBollo.getFiscalCode();
      CreditorInstitution creditor = configCacheService.getCreditorInstitutions().get(ciFiscalCode);
      if (creditor == null) {
        return factory.createPaDemandPaymentNoticeResponse(createPaDemandPaymentNoticeKOResponse(request.getIdPA(), "PAA_ID_DOMINIO_ERRATO", "Creditor Institution not configured in pagoPA"));
      }

      NoticeNumberGenerationResponse response = noticeNumberGeneratorService.generateNoticeNumber(ciFiscalCode);

      String formattedRemittanceInformation =
              String.format(
                      REMITTANCE_INFORMATION_PATTERN,
                      response.getNoticeNumber(),
                      marcaDaBollo.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue(),
                      this.remittanceInformation);

      PaymentPositionModelV3 mappingRequest =
                buildPaymentPositionRequest(
                      marcaDaBollo,
                      creditor.getBusinessName(),
                      response.getNoticeNumber(),
                      formattedRemittanceInformation);

      PaymentPositionModelV3 gpdResponse =
              gpdClient.createDebtPosition(marcaDaBollo.getFiscalCode(), mappingRequest, true, SERVICE_TYPE);

      return factory.createPaDemandPaymentNoticeResponse(createPaDemandPaymentNoticeResponse(gpdResponse));

    } catch (AppException e) {
      log.error("AppException: error processing PaDemandPaymentNoticeRequest", e);
      return factory.createPaDemandPaymentNoticeResponse(createPaDemandPaymentNoticeKOResponse(request.getIdPA(), "PAA_SYSTEM_ERROR", "Error processing PaDemandPaymentNoticeRequest XML"));
    } catch (Exception e) {
      log.error("Exception: error processing PaDemandPaymentNoticeRequest XML", e);
      return factory.createPaDemandPaymentNoticeResponse(createPaDemandPaymentNoticeKOResponse(request.getIdPA(), "PAA_SYSTEM_ERROR", "Error processing PaDemandPaymentNoticeRequest XML"));
    }
  }

  private void validateMarcaDaBollo(TipoMarcaDaBollo marcaDaBollo) {
    Set<ConstraintViolation<TipoMarcaDaBollo>> violations = validator.validate(marcaDaBollo);
    if (!violations.isEmpty()) {
      String errorMessage = violations.stream()
              .map(v -> v.getPropertyPath() + ": " + v.getMessage())
              .collect(Collectors.joining("; "));

      log.error("Validation failed for marcaDaBollo: {}", errorMessage);
      throw new AppException(AppError.BAD_REQUEST, errorMessage);
    }
  }

  private TipoMarcaDaBollo unmarshalMarcaDaBollo(byte[] datiSpecificiServizio)
          throws JAXBException, XMLStreamException {
    Unmarshaller unmarshaller = MARCA_DA_BOLLO_CONTEXT.createUnmarshaller();
    XMLStreamReader reader = createMarcaDaBolloReader(datiSpecificiServizio);
    try {
      JAXBElement<TipoMarcaDaBollo> element =
              unmarshaller.unmarshal(reader, TipoMarcaDaBollo.class);
      return element.getValue();
    } finally {
      reader.close();
    }
  }

  /**
   * Builds a secure, namespace-aware {@link XMLStreamReader} for the marcaDaBollo payload.
   *
   * <p>The {@code entityUniqueIdentifierType} and {@code entityUniqueIdentifierValue} elements are
   * declared unqualified in {@code paForNode.xsd}; their namespace is stripped so the payload
   * unmarshals correctly even when the document exposes a single default namespace.
   */
  private XMLStreamReader createMarcaDaBolloReader(byte[] datiSpecificiServizio)
          throws XMLStreamException {
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    XMLStreamReader baseReader =
            inputFactory.createXMLStreamReader(new ByteArrayInputStream(datiSpecificiServizio));
    return new StreamReaderDelegate(baseReader) {
      @Override
      public String getNamespaceURI() {
        String localName = getLocalName();
        if (ENTITY_UID_TYPE_ELEMENT.equals(localName)
                || ENTITY_UID_VALUE_ELEMENT.equals(localName)) {
          return "";
        }
        return super.getNamespaceURI();
      }
    };
  }

  private PaDemandPaymentNoticeResponse createPaDemandPaymentNoticeResponse(
          PaymentPositionModelV3 gpsResponse) throws DatatypeConfigurationException {

    var result = factory.createPaDemandPaymentNoticeResponse();
    result.setOutcome(StOutcome.OK);

    var paymentOption = gpsResponse.getPaymentOption().get(0);
    var installment = paymentOption.getInstallments().get(0);
    var transfer = installment.getTransfer().get(0);

    result.setFiscalCodePA(transfer.getOrganizationFiscalCode());

    CtQrCode ctQrCode = factory.createCtQrCode();
    ctQrCode.setFiscalCode(transfer.getOrganizationFiscalCode());
    ctQrCode.setNoticeNumber(installment.getNav());
    result.setQrCode(ctQrCode);

    result.setCompanyName(gpsResponse.getCompanyName());
    result.setOfficeName(gpsResponse.getCompanyName());
    result.setPaymentDescription(paymentOption.getDescription());

    CtPaymentOptionsDescriptionListPA ctPaymentOptionsDescriptionListPA =
            factory.createCtPaymentOptionsDescriptionListPA();

    CtPaymentOptionDescriptionPA ctPaymentOptionDescriptionPA =
            factory.createCtPaymentOptionDescriptionPA();

    boolean ccp =
            installment
                    .getTransfer()
                    .stream()
                    .noneMatch(elem -> elem.getPostalIban() == null || elem.getPostalIban().isBlank());
    ctPaymentOptionDescriptionPA.setAllCCP(ccp);

    // Converte l'importo da centesimi in Euro per la risposta Nodo
    BigDecimal amountInEuro = BigDecimal.valueOf(installment.getAmount()).divide(BigDecimal.valueOf(100));
    ctPaymentOptionDescriptionPA.setAmount(amountInEuro);

    var date = installment.getDueDate();
    if (date != null) {
      ctPaymentOptionDescriptionPA.setDueDate(
              DatatypeFactory.newInstance().newXMLGregorianCalendar(String.valueOf(date)));
    }

    ctPaymentOptionDescriptionPA.setOptions(StAmountOption.EQ);
    ctPaymentOptionDescriptionPA.setDetailDescription(installment.getDescription());

    ctPaymentOptionsDescriptionListPA.setPaymentOptionDescription(ctPaymentOptionDescriptionPA);
    result.setPaymentList(ctPaymentOptionsDescriptionListPA);

    return result;
  }

  private PaDemandPaymentNoticeResponse createPaDemandPaymentNoticeKOResponse(String idPA, String faultCode, String faultDescription) {

    var result = factory.createPaDemandPaymentNoticeResponse();
    result.setOutcome(StOutcome.KO);
    CtFaultBean fault = factory.createCtFaultBean();
    fault.setId(idPA);
    fault.setFaultCode(faultCode);
    fault.setFaultString(faultDescription);
    result.setFault(fault); 
    return result;
  }

  private PaymentPositionModelV3 buildPaymentPositionRequest(
          TipoMarcaDaBollo marcaDaBollo,
          String businessName,
          String nav,
          String remittanceInformation) {
    String debtorFiscalCode = marcaDaBollo.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue();
    long amountInCents = marcaDaBollo.getAmount().longValue() * 100L;

    PaymentPositionModelV3 paymentPosition = new PaymentPositionModelV3();
    paymentPosition.setIupd(
            String.format("%s%d_%s", MBD_PREFIX, LocalDate.now(ROME_ZONE_ID).getYear(), nav));
    paymentPosition.setPayStandIn(false);
    paymentPosition.setCompanyName(businessName);

    PaymentOptionModelV3 paymentOption = new PaymentOptionModelV3();
    paymentOption.setDescription(description);
    paymentOption.setSwitchToExpired(true);

    DebtorModel debtorModel = new DebtorModel();
    debtorModel.setType("G".equals(marcaDaBollo.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierType().value()) ? Type.G : Type.F);
    debtorModel.setFiscalCode(debtorFiscalCode);
    debtorModel.setFullName(marcaDaBollo.getDebtor().getFullName());
    debtorModel.setProvince(marcaDaBollo.getDebtor().getProvince());
    debtorModel.setEmail(marcaDaBollo.getDebtor().getEmail());
    paymentOption.setDebtor(debtorModel);

    InstallmentModel installment = new InstallmentModel();
    installment.setNav(nav);
    installment.setIuv(nav.substring(1));
    installment.setAmount(amountInCents);
    installment.setDescription(description);
    installment.setDueDate(LocalDateTime.now(ZoneOffset.UTC).plusDays(dueDateDays));

    TransferModel transfer = new TransferModel();
    transfer.setIdTransfer(TRANSFER_ID);
    transfer.setAmount(amountInCents);
    transfer.setOrganizationFiscalCode(marcaDaBollo.getFiscalCode());
    transfer.setRemittanceInformation(remittanceInformation);
    transfer.setCategory(category);
    transfer.setStamp(
            Stamp.builder()
                    .stampType(TRANSFER_STAMP_TYPE)
                    .hashDocument(new String(marcaDaBollo.getDocumentHash()))
                    .provincialResidence(marcaDaBollo.getDebtor().getProvince())
                    .build());
    transfer.setCompanyName(businessName);

    installment.getTransfer().add(transfer);
    paymentOption.addInstallment(installment);
    paymentPosition.addPaymentOption(paymentOption);

    return paymentPosition;
  }
}