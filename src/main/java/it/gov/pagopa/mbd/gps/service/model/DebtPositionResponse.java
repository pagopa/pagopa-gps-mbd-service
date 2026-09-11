package it.gov.pagopa.mbd.gps.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DebtPositionResponse {

  private String noticeNumber;
  private String companyName;
  private String description;
}
