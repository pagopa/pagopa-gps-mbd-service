package it.gov.pagopa.mbd.gps.service.service;

import it.gov.pagopa.mbd.gps.service.client.GpdClient;
import it.gov.pagopa.mbd.gps.service.exception.AppError;
import it.gov.pagopa.mbd.gps.service.exception.AppException;
import it.gov.pagopa.mbd.gps.service.model.*;
import it.gov.pagopa.mbd.gps.service.model.cache.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.model.client.*;
import it.gov.pagopa.mbd.gps.service.model.partner.*;
import it.gov.pagopa.noticenumber.model.NoticeNumberGenerationResponse;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

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
  private static final String TEXT_XML_NODE = "#text";
  private static final ZoneId ROME_ZONE_ID = ZoneId.of("Europe/Rome");

  private final ConfigCacheService configCacheService;
  private final GpdClient gpdClient;
  private final NoticeNumberGeneratorService noticeNumberGeneratorService;
  private final Validator validator;

  private final ObjectFactory factory = new ObjectFactory();

  @Value("${mbd.payment-position.duedate-days}")
  private int dueDateDays;

  @Value("${mbd.payment-position.category}")
  private String category;

  @Value("${mbd.payment-position.description}")
  private String description;

  @Value("${mbd.payment-position.remittance-information}")
  private String remittanceInformation;

  public PaDemandPaymentNoticeResponse createDebtPosition(
          PaDemandPaymentNoticeRequest request) {
    try {
      // crea QUI

      List<ServicePropertyModel> serviceProperties = mapDatiSpecificiServizio(request);
      MbdPaymentOptionRequestProperties requestProperties = extractProperties(serviceProperties);
      validateProperties(requestProperties);
      String ciFiscalCode = requestProperties.getCiFiscalCode();
      CreditorInstitution creditor = configCacheService.getCreditorInstitutions().get(ciFiscalCode);
      if (creditor == null) {
        throw new AppException(
                AppError.CREDITOR_INSTITUTION_NOT_FOUND,
                "Creditor Institution not registered in api-config");
      }

      NoticeNumberGenerationResponse response =
              noticeNumberGeneratorService.generateNoticeNumber(ciFiscalCode);

      String formattedRemittanceInformation =
              String.format(
                      REMITTANCE_INFORMATION_PATTERN,
                      response.getNoticeNumber(),
                      requestProperties.getDebtorFiscalCode(),
                      this.remittanceInformation);

      PaymentPositionModelV3 mappingRequest =
              buildPaymentPositionRequest(
                      requestProperties,
                      creditor.getBusinessName(),
                      response.getNoticeNumber(),
                      formattedRemittanceInformation);

      PaymentPositionModelV3 gpdResponse =
              gpdClient.createDebtPosition(
                      requestProperties.getCiFiscalCode(), mappingRequest, true, SERVICE_TYPE);

      return createPaDemandPaymentNoticeResponse(gpdResponse);

    } catch (AppException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error processing PaDemandPaymentNoticeRequest XML", e);
      throw new AppException(AppError.INTERNAL_SERVER_ERROR, "Error processing PaDemandPaymentNoticeRequest XML", e);
    }
  }

  private void validateProperties(MbdPaymentOptionRequestProperties properties) {
    Set<ConstraintViolation<MbdPaymentOptionRequestProperties>> violations = validator.validate(properties);
    if (!violations.isEmpty()) {
      String errorMessage = violations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining("; "));

      log.error("Validation failed for MbdPaymentOptionRequestProperties: {}", errorMessage);
      throw new AppException(AppError.BAD_REQUEST, errorMessage);
    }
  }

  private List<ServicePropertyModel> mapDatiSpecificiServizio(
          PaDemandPaymentNoticeRequest request)
          throws ParserConfigurationException, SAXException, IOException {

    DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
    xmlFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    DocumentBuilder builder = xmlFactory.newDocumentBuilder();
    var document =
            builder.parse(new ByteArrayInputStream(request.getDatiSpecificiServizioRequest()));

    var nodes = document.getElementsByTagName("marcaDaBollo").item(0).getChildNodes();
    List<ServicePropertyModel> attributes = new ArrayList<>(nodes.getLength());
    for (int i = 0; i < nodes.getLength(); i++) {
      var node = nodes.item(i);
      if (!TEXT_XML_NODE.equals(node.getNodeName())) {
        var name = node.getNodeName();
        var value = node.getTextContent();
        attributes.add(ServicePropertyModel.builder().name(name).value(value).build());
      }
    }
    return attributes;
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

  private MbdPaymentOptionRequestProperties extractProperties(List<ServicePropertyModel> attributes) {
    var builder = MbdPaymentOptionRequestProperties.builder();
    for (ServicePropertyModel attr : attributes) {
      switch (attr.getName()) {
        case "amount" -> builder.amount(Long.parseLong(attr.getValue()));
        case "debtorName" -> builder.debtorName(attr.getValue());
        case "debtorSurname" -> builder.debtorSurname(attr.getValue());
        case "debtorEmail" -> builder.debtorEmail(attr.getValue());
        case "debtorFiscalCode" -> builder.debtorFiscalCode(attr.getValue());
        case "ciFiscalCode" -> builder.ciFiscalCode(attr.getValue());
        case "debtorProvince" -> builder.debtorProvince(attr.getValue());
        case "documentHash" -> builder.documentHash(attr.getValue());
        default -> log.debug("Tag not mapped: {}", attr.getName());
      }
    }
    return builder.build();
  }

  private PaymentPositionModelV3 buildPaymentPositionRequest(
          MbdPaymentOptionRequestProperties requestProperties,
          String businessName,
          String nav,
          String remittanceInformation) {
    String debtorFiscalCode = requestProperties.getDebtorFiscalCode();
    long amountInCents = requestProperties.getAmount() * 100L;

    PaymentPositionModelV3 paymentPosition = new PaymentPositionModelV3();
    paymentPosition.setIupd(
            String.format("%s%d_%s", MBD_PREFIX, LocalDate.now(ROME_ZONE_ID).getYear(), nav));
    paymentPosition.setPayStandIn(false);
    paymentPosition.setCompanyName(businessName);

    PaymentOptionModelV3 paymentOption = new PaymentOptionModelV3();
    paymentOption.setDescription(description);
    paymentOption.setSwitchToExpired(true);

    DebtorModel debtorModel = new DebtorModel();
    debtorModel.setType(debtorFiscalCode.length() == 11 ? Type.G : Type.F);
    debtorModel.setFiscalCode(debtorFiscalCode);
    debtorModel.setFullName(
            StringUtils.isBlank(requestProperties.getDebtorName())
                    ? requestProperties.getDebtorSurname()
                    : String.format(
                    "%s %s", requestProperties.getDebtorName(), requestProperties.getDebtorSurname()));
    debtorModel.setProvince(requestProperties.getDebtorProvince());
    debtorModel.setEmail(requestProperties.getDebtorEmail());
    paymentOption.setDebtor(debtorModel);

    InstallmentModel installment = new InstallmentModel();
    installment.setNav(nav);
    installment.setIuv(nav.substring(1));
    installment.setAmount(amountInCents);
    installment.setDescription(description);
    installment.setDueDate(LocalDateTime.now(ROME_ZONE_ID).plusDays(dueDateDays));

    TransferModel transfer = new TransferModel();
    transfer.setIdTransfer(TRANSFER_ID);
    transfer.setAmount(amountInCents);
    transfer.setOrganizationFiscalCode(requestProperties.getCiFiscalCode());
    transfer.setRemittanceInformation(remittanceInformation);
    transfer.setCategory(category);
    transfer.setStamp(
            Stamp.builder()
                    .stampType(TRANSFER_STAMP_TYPE)
                    .hashDocument(requestProperties.getDocumentHash())
                    .provincialResidence(requestProperties.getDebtorProvince())
                    .build());
    transfer.setCompanyName(businessName);

    installment.getTransfer().add(transfer);
    paymentOption.addInstallment(installment);
    paymentPosition.addPaymentOption(paymentOption);

    return paymentPosition;
  }
}