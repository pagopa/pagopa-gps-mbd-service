package it.gov.pagopa.mbd.gps.service.client;

import feign.FeignException;
import it.gov.pagopa.mbd.gps.service.model.client.PaymentPositionModelV3;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "gpd", url = "${service.gpd.host}", path = "v3")
public interface GpdClient {

  @Retryable(
      exclude = FeignException.FeignClientException.class,
      maxAttemptsExpression = "${retry.gpd.maxAttempts}",
      backoff = @Backoff(delayExpression = "${retry.gpd.maxDelay}"))
  @PostMapping(value = "/organizations/{organizationfiscalcode}/debtpositions")
  PaymentPositionModelV3 createDebtPosition(
      @PathVariable("organizationfiscalcode") String organizationFiscalCode,
      @RequestBody PaymentPositionModelV3 paymentPositionModel,
      @RequestParam Boolean toPublish);
}
