package it.gov.pagopa.mbd.gps.service.mapper;

import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequest;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequestProperties;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionResponse;
import it.gov.pagopa.mbd.gps.service.model.PaymentOption;
import it.gov.pagopa.mbd.gps.service.model.Stamp;
import it.gov.pagopa.mbd.gps.service.model.Transfer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Converter class that specify how to convert a {@link MbdPaymentOptionRequest} instance to a
 * {@link MbdPaymentOptionResponse} instance
 */
@Component
public class ConvertMbdPaymentOptionRequestToMbdPaymentOptionResponse
    implements Converter<MbdPaymentOptionRequest, MbdPaymentOptionResponse> {

  private static final boolean IS_PARTIAL_PAYMENT = false;
  private static final String ID_TRANSFER = "1";
  private static final String STAMP_TYPE = "st";

  @Value("${mbd.payment-option.description}")
  private String description;

  @Value("${mbd.transfer.remittance-information}")
  private String remittanceInformation;

  @Value("${mbd.payment-option.due-date-delta}")
  private int dueDateDelta;

  @Value("${mbd.payment-option.due-date-delta-time-unit}")
  private ChronoUnit dueDateDeltaTimeUnit;

  @Value("${mbd.payment-option.retention-date-delta}")
  private int retentionDateDelta;

  @Value("${mbd.payment-option.retention-date-delta-time-unit}")
  private ChronoUnit retentionDateDeltaTimeUnit;

  @Override
  public MbdPaymentOptionResponse convert(
      MappingContext<MbdPaymentOptionRequest, MbdPaymentOptionResponse> context) {
    MbdPaymentOptionRequestProperties model = context.getSource().getProperties();

    Transfer transfer =
        Transfer.builder()
            .amount(model.getAmount())
            .idTransfer(ID_TRANSFER)
            .organizationFiscalCode(model.getFiscalCode())
            .remittanceInformation(remittanceInformation)
            .stamp(
                Stamp.builder()
                    .hashDocument(model.getDocumentHash())
                    .provincialResidence(model.getProvincialResidence())
                    .stampType(STAMP_TYPE)
                    .build())
            .build();

    PaymentOption paymentOption =
        PaymentOption.builder()
            .firstName(model.getFirstName())
            .lastName(model.getLastName())
            .amount(model.getAmount())
            .description(description)
            .dueDate(LocalDateTime.now().plus(dueDateDelta, dueDateDeltaTimeUnit))
            .retentionDate(LocalDateTime.now().plus(retentionDateDelta, retentionDateDeltaTimeUnit))
            .isPartialPayment(IS_PARTIAL_PAYMENT)
            .transfer(Collections.singletonList(transfer))
            .build();

    return MbdPaymentOptionResponse.builder()
        .paymentOption(Collections.singletonList(paymentOption))
        .build();
  }
}
