package it.gov.pagopa.mbd.gps.service.annotation;

import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequestProperties;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MbdDebtorValidatorTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("checkPhysicalPerson - Success: Physical person with surname and name is empty")
    void shouldFailWhenPhysicalPersonHasNoName() {
        MbdPaymentOptionRequestProperties request = MbdPaymentOptionRequestProperties.builder()
                .amount(16L)
                .debtorFiscalCode("MRRNSR75R05H501I")
                .debtorName(null)
                .debtorSurname("Rossi")
                .debtorEmail("mario.rossi@email.it")
                .ciFiscalCode("77777777777")
                .debtorProvince("RM")
                .documentHash("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                .build();

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("checkPhysicalPerson - Success: Physical person with surname and name is not empty")
    void shouldPassWhenPhysicalPersonHasName() {
        MbdPaymentOptionRequestProperties request = MbdPaymentOptionRequestProperties.builder()
                .amount(16L)
                .debtorFiscalCode("MRRNSR75R05H501I")
                .debtorName("Mario")
                .debtorSurname("Rossi")
                .debtorEmail("mario.rossi@email.it")
                .ciFiscalCode("77777777777")
                .debtorProvince("RM")
                .documentHash("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                .build();

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("checkLegalPerson - Success: Legal person with surname and name is empty")
    void shouldPassWhenLegalEntityHasNoName() {
        MbdPaymentOptionRequestProperties request = MbdPaymentOptionRequestProperties.builder()
                .amount(16L)
                .debtorFiscalCode("12345678901")
                .debtorName(null)
                .debtorSurname("Acme S.r.l.")
                .debtorEmail("acme@email.it")
                .ciFiscalCode("77777777777")
                .debtorProvince("RM")
                .documentHash("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                .build();

        assertTrue(validator.validate(request).isEmpty());
    }
}