package it.gov.pagopa.mdb.gps.service.config;


import it.gov.pagopa.mdb.gps.service.mapper.ConvertMdbPaymentOptionRequestToMdbPaymentOptionResponse;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionRequest;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingsConfiguration {

  @Bean
  ModelMapper modelMapper() {
    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

    mapper.createTypeMap(MdbPaymentOptionRequest.class, MdbPaymentOptionResponse.class)
            .setConverter(new ConvertMdbPaymentOptionRequestToMdbPaymentOptionResponse());
    return mapper;
  }
}
