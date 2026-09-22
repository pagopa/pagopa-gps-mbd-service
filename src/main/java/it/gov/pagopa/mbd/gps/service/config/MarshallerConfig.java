package it.gov.pagopa.mbd.gps.service.config;

import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class MarshallerConfig {

  @Bean(name = "partnerJaxb2Marshaller")
  public Jaxb2Marshaller partnerJaxb2Marshaller() {
    Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    jaxb2Marshaller.setPackagesToScan("it.gov.pagopa.mbd.gps.service.model.partner");
    jaxb2Marshaller.setMarshallerProperties(
        Map.of(
            jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
            Boolean.FALSE,
            jakarta.xml.bind.Marshaller.JAXB_ENCODING,
            "UTF-8"));
    return jaxb2Marshaller;
  }

  @Bean(name = "marcaDaBolloJaxb2Marshaller")
  public Jaxb2Marshaller marcaDaBolloJaxb2Marshaller() {
    Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
    jaxb2Marshaller.setPackagesToScan("it.gov.pagopa.mbd.gps.service.model.marcadabollo");
    jaxb2Marshaller.setMarshallerProperties(
        Map.of(
            jakarta.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT,
            Boolean.FALSE,
            jakarta.xml.bind.Marshaller.JAXB_ENCODING,
            "UTF-8"));
    return jaxb2Marshaller;
  }
}
