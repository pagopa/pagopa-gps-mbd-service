package it.gov.pagopa.mbd.gps.service.service;

import it.gov.pagopa.mbd.gps.service.client.GpdClient;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequest;
import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequestProperties;
import it.gov.pagopa.mbd.gps.service.model.client.CreditorInstitution;
import it.gov.pagopa.mbd.gps.service.model.client.PaymentPositionModelV3;
import it.gov.pagopa.noticenumber.model.NoticeNumberGenerationResponse;
import it.gov.pagopa.noticenumber.service.NoticeNumberGeneratorService; // 🟢 Importazione della libreria
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MbdGpsServiceTest {

    @Mock
    private GpdClient gpdClient;

    @Mock
    private ConfigCacheService configCacheService;

    @Mock
    private NoticeNumberGeneratorService noticeNumberGeneratorService;

    @InjectMocks
    private MbdGpsService mbdGpsService;

    private MbdPaymentOptionRequest request;

    @BeforeEach
    void setUp() {
        request = createDummyRequest();
    }

    @Test
    @DisplayName("createDebtPosition - Success: generating NAV and creating debtor position in GPD V3")
    void createDebtPosition_Success() {
        String fiscalCode = "77777777777";

        CreditorInstitution ci = new CreditorInstitution();
        Map<String, CreditorInstitution> mockCreditorInstitutions = new HashMap<>();
        mockCreditorInstitutions.put(fiscalCode, ci);
        when(configCacheService.getCreditorInstitutions()).thenReturn(mockCreditorInstitutions);

        when(noticeNumberGeneratorService.generateNoticeNumber(eq(fiscalCode)))
                .thenReturn(new NoticeNumberGenerationResponse("352178463133495622"));

        PaymentPositionModelV3 gpdResponse = new PaymentPositionModelV3();
        gpdResponse.setIupd("MBD_77777777777_178463133495622");

        when(gpdClient.createDebtPosition(eq(fiscalCode), any(PaymentPositionModelV3.class), eq(true)))
                .thenReturn(gpdResponse);

        // Act
        String iupd = mbdGpsService.createDebtPosition(request);

        // Assert
        assertNotNull(iupd);
        assertEquals("MBD_77777777777_178463133495622", iupd);
        verify(configCacheService).getCreditorInstitutions();
        verify(noticeNumberGeneratorService).generateNoticeNumber(fiscalCode);
        verify(gpdClient).createDebtPosition(eq(fiscalCode), any(PaymentPositionModelV3.class), eq(true));
    }

    private MbdPaymentOptionRequest createDummyRequest() {
        MbdPaymentOptionRequest req = new MbdPaymentOptionRequest();
        MbdPaymentOptionRequestProperties props = new MbdPaymentOptionRequestProperties();
        props.setAmount(16L);
        props.setDebtorName("Mario");
        props.setDebtorSurname("Rossi");
        props.setDebtorEmail("mario.rossi@example.com");
        props.setDebtorFiscalCode("RSSMRA85T10H501Z");
        props.setCiFiscalCode("77777777777");
        props.setDebtorProvince("MI");
        props.setDocumentHash("47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
        req.setProperties(props);
        return req;
    }
}