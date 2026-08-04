package it.gov.pagopa.mbd.gps.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequest;
import it.gov.pagopa.mbd.gps.service.model.ProblemJson;
import it.gov.pagopa.mbd.gps.service.service.MbdGpsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "MBD GPS")
@RequiredArgsConstructor
public class MbdGpsController {

  private final MbdGpsService mbdGpsService;

  /**
   * Endpoint to create an MBD debt position. Generates the Notice Number (NAV) and creates the debt
   * position on GPD Core V3.
   *
   * @param mbdPaymentOptionRequest the request body containing the debt position details
   * @return ResponseEntity with status 201 CREATED if the debt position is successfully created
   */
  @PostMapping("/mbd/paymentOption")
  @ResponseStatus(HttpStatus.OK)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class))),
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
      summary = "Create MBD debt position and payment option",
      description = "Generates NAV and creates the corresponding debt position on GPD Core V3.",
      security = {@SecurityRequirement(name = "ApiKey")})
  public ResponseEntity<String> createPaymentOption(
      @RequestBody @NotNull @Valid MbdPaymentOptionRequest mbdPaymentOptionRequest) {
    var response = mbdGpsService.createDebtPosition(mbdPaymentOptionRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
