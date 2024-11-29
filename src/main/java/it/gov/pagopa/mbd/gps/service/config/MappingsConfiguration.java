package it.gov.pagopa.mbd.gps.service.config;

import it.gov.pagopa.mbd.gps.service.mapper.ConvertMbdPaymentOptionRequestToMbdPaymentOptionResponse;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequest;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingsConfiguration {

    @Bean
    ModelMapper modelMapper(ConvertMbdPaymentOptionRequestToMbdPaymentOptionResponse mdbPaymentOptionResponseConverter) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        mapper.createTypeMap(MbdPaymentOptionRequest.class, MbdPaymentOptionResponse.class)
                .setConverter(mdbPaymentOptionResponseConverter);
        return mapper;
    }
}
