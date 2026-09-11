package it.gov.pagopa.mbd.gps.service.model.client;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InstallmentModel implements Serializable {
  private String nav;
  private String iuv;
  private Long amount;
  private String description;
  private LocalDateTime dueDate;
  private long fee;
  private long notificationFee;
  private InstallmentStatus status;
  private List<TransferModel> transfer = new ArrayList<>();
  private List<InstallmentMetadataModel> installmentMetadata = new ArrayList<>();
}
