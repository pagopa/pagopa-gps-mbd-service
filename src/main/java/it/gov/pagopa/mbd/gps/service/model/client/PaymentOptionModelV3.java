package it.gov.pagopa.mbd.gps.service.model.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PaymentOptionModelV3 implements Serializable {

    private String description;
    private LocalDateTime validityDate;
    private LocalDateTime retentionDate;
    private Boolean switchToExpired;
    private DebtorModel debtor;
    private List<InstallmentModel> installments = new ArrayList<>();

    public void addInstallment(InstallmentModel installment) {
        this.installments.add(installment);
    }
}
