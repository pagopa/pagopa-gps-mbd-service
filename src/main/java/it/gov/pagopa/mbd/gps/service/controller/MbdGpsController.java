package it.gov.pagopa.mbd.gps.service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.mbd.gps.service.model.ProblemJson;
import it.gov.pagopa.mbd.gps.service.model.partner.PaDemandPaymentNoticeRequest;
import it.gov.pagopa.mbd.gps.service.model.partner.PaDemandPaymentNoticeResponse;
import it.gov.pagopa.mbd.gps.service.service.MbdGpsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.JAXBElement;
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
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content =
                            @Content(
                                    mediaType = MediaType.APPLICATION_XML_VALUE,
                                    schema = @Schema(implementation = PaDemandPaymentNoticeResponse.class))),
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
            description = "Parses PaDemandPaymentNoticeRequest XML, generates notice number, creates debt position on GPD Core V3, and returns PaDemandPaymentNoticeResponse.",
            security = {@SecurityRequirement(name = "ApiKey")})
    public ResponseEntity<JAXBElement<PaDemandPaymentNoticeResponse>> createPaymentOption(
            @RequestBody @NotNull @Valid PaDemandPaymentNoticeRequest request) {
        var response = mbdGpsService.createDebtPosition(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}