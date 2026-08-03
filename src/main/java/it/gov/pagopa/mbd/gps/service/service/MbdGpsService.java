package it.gov.pagopa.mbd.gps.service.service;

import it.gov.pagopa.mbd.gps.service.client.GpdClient;
import it.gov.pagopa.mbd.gps.service.exception.AppError;
import it.gov.pagopa.mbd.gps.service.exception.AppException;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequest;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequestProperties;
import it.gov.pagopa.mbd.gps.service.model.client.*;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling MBD payment options and creating debt positions in the GPD
 * system.
 */
@Service
@RequiredArgsConstructor
public class MbdGpsService {

  private static final String MBD_PREFIX = "MBD";
  private static final String TRANSFER_STAMP_TYPE = "01";
  private static final String TRANSFER_ID = "1";
  private static final String REMITTANCE_INFORMATION_PATTERN = "/RFB/%s/CNR/%s/TXT/%s";
  private static final ZoneId ROME_ZONE_ID = ZoneId.of("Europe/Rome");

  @Value("${mbd.payment-position.duedate-days}")
  private int dueDateDays;

  @Value("${mbd.payment-position.category}")
  private String category;

  @Value("${mbd.payment-position.description}")
  private String description;

  @Value("${mbd.payment-position.remittance-information}")
  private String remittanceInformation;

  private final ConfigCacheService configCacheService;
  private final GpdClient gpdClient;
  private final NoticeNumberGeneratorService noticeNumberGeneratorService;

  /**
   * Creates a debt position in the GPD system.
   *
   * @param request The MBD payment option request containing the necessary properties for creating
   *     the debt position.
   * @throws AppException if the creditor institution is not registered in the configuration cache.
   */
  public String createDebtPosition(MbdPaymentOptionRequest request) {
    MbdPaymentOptionRequestProperties requestProperties = request.getProperties();
    String ciFiscalCode = requestProperties.getCiFiscalCode();

    var creditor = configCacheService.getCreditorInstitutions().get(ciFiscalCode);
    if (creditor == null) {
      throw new AppException(
          AppError.CREDITOR_INSTITUTION_NOT_FOUND,
          "Creditor Institution not registered in api-config");
    }

    var response = noticeNumberGeneratorService.generateNoticeNumber(ciFiscalCode);
    var mappingRequest =
        buildPaymentPositionRequest(
            requestProperties, creditor.getBusinessName(), response.getNoticeNumber());

    var gpdResponse =
        gpdClient.createDebtPosition(requestProperties.getCiFiscalCode(), mappingRequest, true);
    return gpdResponse.getIupd();
  }

  /**
   * Builds a PaymentPositionModelV3 object.
   *
   * @param requestProperties The properties of the MBD payment option request.
   * @param businessName The business name of the creditor institution.
   * @param nav The notice number (NAV) generated for the debt position.
   * @return A PaymentPositionModelV3 object representing the debt position to be created in the GPD
   *     system.
   */
  private PaymentPositionModelV3 buildPaymentPositionRequest(
      MbdPaymentOptionRequestProperties requestProperties, String businessName, String nav) {
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
        StringUtils.isEmpty(requestProperties.getDebtorName())
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
    transfer.setRemittanceInformation(
        String.format(
            REMITTANCE_INFORMATION_PATTERN, nav, debtorFiscalCode, remittanceInformation));
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
