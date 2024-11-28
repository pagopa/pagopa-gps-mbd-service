package it.gov.pagopa.mdb.gps.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionRequest;
import it.gov.pagopa.mdb.gps.service.model.MdbPaymentOptionResponse;
import it.gov.pagopa.mdb.gps.service.model.ProblemJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/mbd/paymentOption", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "MDB GPS")
public class MdbGpsController {

    private final ModelMapper modelMapper;

    @Autowired
    public MdbGpsController(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Map MDB service specific data into payment option model
     *
     * @param mdbPaymentOptionRequest MDB data
     * @return the mapped model
     */
    @PostMapping("")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = MdbPaymentOptionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))
    })
    @Operation(summary = "Create MDB payment option", security = {@SecurityRequirement(name = "ApiKey")})
    public @Valid MdbPaymentOptionResponse createMdbPaymentOption(
            @RequestBody @NotNull @Valid MdbPaymentOptionRequest mdbPaymentOptionRequest
    ) {
        return this.modelMapper.map(mdbPaymentOptionRequest, MdbPaymentOptionResponse.class);
    }
}