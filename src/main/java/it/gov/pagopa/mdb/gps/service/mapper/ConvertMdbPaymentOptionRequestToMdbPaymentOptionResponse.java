package it.gov.pagopa.mdb.gps.service.mapper;

import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionRequest;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionRequestProperties;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionResponse;
import it.gov.pagopa.mdb.gps.service.model.PaymentOption;
import it.gov.pagopa.mdb.gps.service.model.Stamp;
import it.gov.pagopa.mdb.gps.service.model.Transfer;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Converter class that specify how to convert a {@link MdbPaymentOptionRequest} instance to a {@link MdbPaymentOptionResponse} instance
 */
public class ConvertMdbPaymentOptionRequestToMdbPaymentOptionResponse implements Converter<MdbPaymentOptionRequest, MdbPaymentOptionResponse> {

    private static final boolean IS_PARTIAL_PAYMENT = false;
    private static final String ID_TRANSFER = "1";
    private static final String REMITTANCE_INFORMATION = "Pagamento MDB";
    private static final String STAMP_TYPE = "st";

    @Override
    public MdbPaymentOptionResponse convert(MappingContext<MdbPaymentOptionRequest, MdbPaymentOptionResponse> context) {
        MdbPaymentOptionRequestProperties model = context.getSource().getProperties();

        Transfer transfer = Transfer.builder()
                .amount(model.getAmount())
                .idTransfer(ID_TRANSFER)
                .organizationFiscalCode(model.getFiscalCode())
                .remittanceInformation(REMITTANCE_INFORMATION)
                .stamp(Stamp.builder()
                        .hashDocument(model.getDocumentHash())
                        .provincialResidence(model.getProvincialResidence())
                        .stampType(STAMP_TYPE)
                        .build())
                .build();

        LocalDateTime now = LocalDateTime.now();
        PaymentOption paymentOption = PaymentOption.builder()
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .amount(model.getAmount())
                .description(model.getDescription())
                .dueDate(now)
                .retentionDate(now)
                .isPartialPayment(IS_PARTIAL_PAYMENT)
                .transfer(Collections.singletonList(transfer))
                .build();

        return MdbPaymentOptionResponse.builder()
                .paymentOption(Collections.singletonList(paymentOption))
                .build();
    }
}