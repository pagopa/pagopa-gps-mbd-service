package it.gov.pagopa.mdb.gps.service.mapper;

import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionRequest;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionRequestProperties;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionResponse;
import it.gov.pagopa.mdb.gps.service.model.PaymentOption;
import it.gov.pagopa.mdb.gps.service.model.Stamp;
import it.gov.pagopa.mdb.gps.service.model.Transfer;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

/**
 * Converter class that specify how to convert a {@link MdbPaymentOptionRequest} instance to a {@link MdbPaymentOptionResponse} instance
 */
@Component
public class ConvertMdbPaymentOptionRequestToMdbPaymentOptionResponse implements Converter<MdbPaymentOptionRequest, MdbPaymentOptionResponse> {

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
    public MdbPaymentOptionResponse convert(MappingContext<MdbPaymentOptionRequest, MdbPaymentOptionResponse> context) {
        MdbPaymentOptionRequestProperties model = context.getSource().getProperties();

        Transfer transfer = Transfer.builder()
                .amount(model.getAmount())
                .idTransfer(ID_TRANSFER)
                .organizationFiscalCode(model.getFiscalCode())
                .remittanceInformation(remittanceInformation)
                .stamp(Stamp.builder()
                        .hashDocument(model.getDocumentHash())
                        .provincialResidence(model.getProvincialResidence())
                        .stampType(STAMP_TYPE)
                        .build())
                .build();

        PaymentOption paymentOption = PaymentOption.builder()
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .amount(model.getAmount())
                .description(description)
                .dueDate(LocalDateTime.now().plus(dueDateDelta, dueDateDeltaTimeUnit))
                .retentionDate(LocalDateTime.now().plus(retentionDateDelta, retentionDateDeltaTimeUnit))
                .isPartialPayment(IS_PARTIAL_PAYMENT)
                .transfer(Collections.singletonList(transfer))
                .build();

        return MdbPaymentOptionResponse.builder()
                .paymentOption(Collections.singletonList(paymentOption))
                .build();
    }
}