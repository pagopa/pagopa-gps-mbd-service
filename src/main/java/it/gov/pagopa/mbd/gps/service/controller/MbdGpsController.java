package it.gov.pagopa.mbd.gps.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.mbd.gps.service.model.PaDemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.gps.service.model.PaDemandPaymentNoticeResponse;
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
@RequestMapping
@Tag(name = "MBD GPS")
@RequiredArgsConstructor
public class MbdGpsController {

    private final MbdGpsService mbdGpsService;

    /**
     * Endpoint to create an MBD debt position from Nodo dei Pagamenti XML request.
     * Generates the Notice Number (NAV), creates the debt position on GPD Core V3,
     * and returns the XML response expected by PagoPA.
     *
     * @param request the request body containing the PaDemandPaymentNoticeRequest XML
     * @return ResponseEntity with status 201 CREATED and PaDemandPaymentNoticeResponse body
     */
    @PostMapping(
            value = "/mbd/paymentOption",
            consumes = MediaType.APPLICATION_XML_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Created",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_XML_VALUE,
                                    schema = @Schema(implementation = PaDemandPaymentNoticeResponse.class))),
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
            description = "Parses PaDemandPaymentNoticeRequest XML, generates NAV, creates debt position on GPD Core V3, and returns PaDemandPaymentNoticeResponse.",
            security = {@SecurityRequirement(name = "ApiKey")})
    public ResponseEntity<PaDemandPaymentNoticeResponse> createPaymentOption(
            @RequestBody @NotNull @Valid PaDemandPaymentNoticeRequest request) {
        var response = mbdGpsService.createDebtPosition(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}