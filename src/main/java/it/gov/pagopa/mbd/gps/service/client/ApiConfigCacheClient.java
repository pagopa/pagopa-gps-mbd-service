package it.gov.pagopa.mbd.gps.service.client;

import it.gov.pagopa.mbd.gps.service.model.client.ConfigDataV1;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Client for the api-config-cache services
 */
@FeignClient(name = "apiConfigCacheClient", url = "${apiConfigCacheClient.url}")
public interface ApiConfigCacheClient {

    /**
     * Retrieve cache from the provided service
     *
     * @param keys list of strings to be used as filter for provided data
     * @return required cache data
     */
    @GetMapping("/cache")
    ConfigDataV1 getCache(
            @RequestHeader("Ocp-Apim-Subscription-Key") String subKey,
            @RequestParam("keys") List<String> keys);
}
