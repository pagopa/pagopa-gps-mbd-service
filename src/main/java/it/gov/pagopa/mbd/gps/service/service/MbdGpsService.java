package it.gov.pagopa.mbd.gps.service.service;

import it.gov.pagopa.mbd.gps.service.client.GpdClient;
import it.gov.pagopa.mbd.gps.service.exception.AppException;
import it.gov.pagopa.mbd.gps.service.exception.MarcaDaBolloValidationException;
import it.gov.pagopa.mbd.gps.service.model.cache.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.model.client.*;
import it.gov.pagopa.mbd.gps.service.model.enumeration.AppErrorCode;
import it.gov.pagopa.mbd.gps.service.model.marcadabollo.TipoMarcaDaBollo;
import it.gov.pagopa.mbd.gps.service.model.partner.*;
import it.gov.pagopa.noticenumber.model.NoticeNumberGenerationResponse;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import jakarta.xml.bind.JAXBException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.regex.Pattern;
import javax.xml.stream.XMLStreamException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
  private static final Pattern DEBTOR_FISCAL_CODE_PATTERN =
      Pattern.compile("^[A-Z]{6}\\d{2}[A-Z]\\d{2}[A-Z]\\d{3}[A-Z]$|^\\d{11}$");
  private static final Pattern DEBTOR_EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
  private static final Pattern PA_FISCAL_CODE_PATTERN = Pattern.compile("^\\d{11}$");

  private final ConfigCacheService configCacheService;
  private final GpdClient gpdClient;
  private final NoticeNumberGeneratorService noticeNumberGeneratorService;
  private final ObjectFactory factory = new ObjectFactory();
  private final MbdXmlService mbdXmlService;

  @Value("${mbd.payment-position.duedate-days}")
  private int dueDateDays;

  @Value("${mbd.payment-position.category}")
  private String category;

  @Value("${mbd.payment-position.description}")
  private String description;

  @Value("${mbd.payment-position.remittance-information}")
  private String remittanceInformation;

  public String createDebtPosition(PaDemandPaymentNoticeRequest request) {
    try {
      log.debug(
          "Processing paDemandPaymentNoticeRequest XML: {}",
          mbdXmlService.marshal(factory.createPaDemandPaymentNoticeRequest(request)));

      TipoMarcaDaBollo marcaDaBollo =
          mbdXmlService.unmarshalMarcaDaBollo(request.getDatiSpecificiServizioRequest());
      checkMarcaDaBollo(marcaDaBollo);

      String ciFiscalCode = marcaDaBollo.getFiscalCode();
      CreditorInstitution creditor = configCacheService.getCreditorInstitutions().get(ciFiscalCode);
      if (creditor == null) {
        return mbdXmlService.marshal(
            factory.createPaDemandPaymentNoticeResponse(
                createPaDemandPaymentNoticeKOResponse(
                    request.getIdPA(),
                    "PAA_ID_DOMINIO_ERRATO",
                    "Creditor Institution not configured in pagoPA")));
      }

      NoticeNumberGenerationResponse response =
          noticeNumberGeneratorService.generateNoticeNumber(ciFiscalCode);

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

      log.debug("Debt Position body {}", mappingRequest);

      PaymentPositionModelV3 gpdResponse =
          gpdClient.createDebtPosition(
              marcaDaBollo.getFiscalCode(), mappingRequest, true, SERVICE_TYPE);

      return mbdXmlService.marshal(
          factory.createPaDemandPaymentNoticeResponse(
              createPaDemandPaymentNoticeResponse(gpdResponse, formattedRemittanceInformation)));
    } catch (MarcaDaBolloValidationException e) {
      log.error("Validation failed for marcaDaBollo", e);
      return mbdXmlService.marshal(
          factory.createPaDemandPaymentNoticeResponse(
              createPaDemandPaymentNoticeKOResponse(
                  request.getIdPA(),
                  AppErrorCode.PPT_SINTASSI_EXTRAXSD.getCode(),
                  e.getMessage())));
    } catch (JAXBException | XMLStreamException e) {
      log.error("XSD/XML Validation failed for marcaDaBollo", e);
      String details = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      return mbdXmlService.marshal(
          factory.createPaDemandPaymentNoticeResponse(
              createPaDemandPaymentNoticeKOResponse(
                  request.getIdPA(), AppErrorCode.PPT_SINTASSI_EXTRAXSD.getCode(), details)));
    } catch (AppException e) {
      log.error("AppException: error processing PaDemandPaymentNoticeRequest", e);
      return mbdXmlService.marshal(
          factory.createPaDemandPaymentNoticeResponse(
              createPaDemandPaymentNoticeKOResponse(
                  request.getIdPA(), AppErrorCode.PAA_SYSTEM_ERROR.getCode(), e.getMessage())));
    } catch (Exception e) {
      log.error("Exception: error processing PaDemandPaymentNoticeRequest XML", e);
      return mbdXmlService.marshal(
          factory.createPaDemandPaymentNoticeResponse(
              createPaDemandPaymentNoticeKOResponse(
                  request.getIdPA(),
                  AppErrorCode.PAA_SYSTEM_ERROR.getCode(),
                  e.getMessage() != null ? e.getMessage() : "Unexpected system error")));
    }
  }

  void checkMarcaDaBollo(TipoMarcaDaBollo marcaDaBollo) {
    if (marcaDaBollo == null) {
      throw new MarcaDaBolloValidationException("marcaDaBollo is required");
    }

    if (marcaDaBollo.getAmount() == null
        || marcaDaBollo.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new MarcaDaBolloValidationException(
          "amount: Amount is required and must be greater than zero");
    }

    if (marcaDaBollo.getDebtor() == null) {
      throw new MarcaDaBolloValidationException("debtor: Debtor information is required");
    }

    if (marcaDaBollo.getDebtor().getUniqueIdentifier() == null
        || StringUtils.isBlank(
            marcaDaBollo.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue())
        || !DEBTOR_FISCAL_CODE_PATTERN
            .matcher(
                marcaDaBollo.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue())
            .matches()) {
      throw new MarcaDaBolloValidationException(
          "debtor: Entity unique identifier value must be a valid Fiscal Code (16 chars) or VAT number (11 digits)");
    }

    if (StringUtils.isBlank(marcaDaBollo.getDebtor().getFullName())) {
      throw new MarcaDaBolloValidationException("debtor: Debtor full name is required");
    }

    if (StringUtils.isBlank(marcaDaBollo.getDebtor().getEmail())
        || !DEBTOR_EMAIL_PATTERN.matcher(marcaDaBollo.getDebtor().getEmail()).matches()) {
      throw new MarcaDaBolloValidationException(
          "debtor: Debtor email is required and must be a valid email address");
    }

    if (StringUtils.isBlank(marcaDaBollo.getFiscalCode())
        || !PA_FISCAL_CODE_PATTERN.matcher(marcaDaBollo.getFiscalCode()).matches()) {
      throw new MarcaDaBolloValidationException(
          "fiscalCode: Creditor Institution fiscal code must be an 11-digit number");
    }

    if (StringUtils.isBlank(marcaDaBollo.getProvince())
        || marcaDaBollo.getProvince().trim().length() != 2) {
      throw new MarcaDaBolloValidationException(
          "province: Debtor residence province must be exactly 2 characters long");
    }

    if (marcaDaBollo.getDocumentHash() == null || marcaDaBollo.getDocumentHash().length == 0) {
      throw new MarcaDaBolloValidationException("documentHash: Document hash is required");
    }
  }

  private PaDemandPaymentNoticeResponse createPaDemandPaymentNoticeResponse(
      PaymentPositionModelV3 gpsResponse, String formattedRemittanceInformation) {

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

    result.setPaymentDescription(formattedRemittanceInformation);
    result.setCompanyName(gpsResponse.getCompanyName());

    CtPaymentOptionsDescriptionListPA ctPaymentOptionsDescriptionListPA =
        factory.createCtPaymentOptionsDescriptionListPA();

    CtPaymentOptionDescriptionPA ctPaymentOptionDescriptionPA =
        factory.createCtPaymentOptionDescriptionPA();

    boolean ccp =
        installment.getTransfer().stream()
            .noneMatch(elem -> elem.getPostalIban() == null || elem.getPostalIban().isBlank());
    ctPaymentOptionDescriptionPA.setAllCCP(ccp);

    BigDecimal amountInEuro =
        BigDecimal.valueOf(installment.getAmount())
            .divide(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
    ctPaymentOptionDescriptionPA.setAmount(amountInEuro);

    ctPaymentOptionDescriptionPA.setOptions(StAmountOption.EQ);
    ctPaymentOptionDescriptionPA.setDetailDescription(formattedRemittanceInformation);

    ctPaymentOptionsDescriptionListPA.setPaymentOptionDescription(ctPaymentOptionDescriptionPA);
    result.setPaymentList(ctPaymentOptionsDescriptionListPA);

    return result;
  }

  private PaDemandPaymentNoticeResponse createPaDemandPaymentNoticeKOResponse(
      String idPA, String faultCode, String faultDescription) {

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
    String debtorFiscalCode =
        marcaDaBollo.getDebtor().getUniqueIdentifier().getEntityUniqueIdentifierValue();
    long amountInCents = marcaDaBollo.getAmount().longValue() * 100L;

    PaymentPositionModelV3 paymentPosition = new PaymentPositionModelV3();
    paymentPosition.setIupd(
        String.format("%s%d_%s", MBD_PREFIX, LocalDate.now(ZoneOffset.UTC).getYear(), nav));
    paymentPosition.setPayStandIn(false);
    paymentPosition.setCompanyName(businessName);

    PaymentOptionModelV3 paymentOption = new PaymentOptionModelV3();
    paymentOption.setDescription(description);
    paymentOption.setSwitchToExpired(true);

    DebtorModel debtorModel = new DebtorModel();
    debtorModel.setType(
        "G"
                .equals(
                    marcaDaBollo
                        .getDebtor()
                        .getUniqueIdentifier()
                        .getEntityUniqueIdentifierType()
                        .value())
            ? Type.G
            : Type.F);
    debtorModel.setFiscalCode(debtorFiscalCode);
    debtorModel.setFullName(marcaDaBollo.getDebtor().getFullName());
    debtorModel.setProvince(marcaDaBollo.getProvince());
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
            .provincialResidence(marcaDaBollo.getProvince())
            .build());
    transfer.setCompanyName(businessName);

    installment.getTransfer().add(transfer);
    paymentOption.addInstallment(installment);
    paymentPosition.addPaymentOption(paymentOption);

    return paymentPosition;
  }
}
