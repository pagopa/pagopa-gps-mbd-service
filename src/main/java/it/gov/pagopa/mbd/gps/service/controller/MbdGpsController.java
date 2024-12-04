package it.gov.pagopa.mbd.gps.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequest;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionResponse;
import it.gov.pagopa.mbd.gps.service.model.ProblemJson;
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
@RequestMapping(
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "MBD GPS")
public class MbdGpsController {

  private final ModelMapper modelMapper;

  @Autowired
  public MbdGpsController(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  /**
   * Map MBD service specific data into payment option model
   *
   * @param mbdPaymentOptionRequest MBD data
   * @return the mapped model
   */
  @PostMapping("/mbd/paymentOption")
  @ResponseStatus(HttpStatus.CREATED)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = MbdPaymentOptionResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "404",
            description = "Not found",
            content = @Content(schema = @Schema(implementation = ProblemJson.class))),
        @ApiResponse(
            responseCode = "429",
            description = "Too many requests",
            content = @Content(schema = @Schema())),
        @ApiResponse(
            responseCode = "500",
            description = "Service unavailable",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProblemJson.class)))
      })
  @Operation(
      summary = "Build MBD payment option model",
      security = {@SecurityRequirement(name = "ApiKey")})
  public @Valid MbdPaymentOptionResponse buildMdbPaymentOption(
      @RequestBody @NotNull @Valid MbdPaymentOptionRequest mbdPaymentOptionRequest) {
    return this.modelMapper.map(mbdPaymentOptionRequest, MbdPaymentOptionResponse.class);
  }
}
