package it.gov.pagopa.mbd.gps.service.model.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PaymentPositionModelV3 implements Serializable {

    private String iupd;
    private boolean payStandIn = true;
    private String companyName;
    private String officeName;
    private LocalDateTime paymentDate;
    private DebtPositionStatusV3 status;
    private List<PaymentOptionModelV3> paymentOption = new ArrayList<>();

    public void addPaymentOption(PaymentOptionModelV3 paymentOpt) {
        this.paymentOption.add(paymentOpt);
    }
}
