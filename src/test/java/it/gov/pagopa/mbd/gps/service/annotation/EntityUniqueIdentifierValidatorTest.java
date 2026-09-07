package it.gov.pagopa.mbd.gps.service.annotation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import it.gov.pagopa.mbd.gps.service.model.marcadabollo.DebtorInfo;
import it.gov.pagopa.mbd.gps.service.model.partner.CtEntityUniqueIdentifier;
import it.gov.pagopa.mbd.gps.service.model.partner.StEntityUniqueIdentifierType;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

@ExtendWith(MockitoExtension.class)
class EntityUniqueIdentifierValidatorTest {

    private EntityUniqueIdentifierValidator validator;

    @Mock private ConstraintValidatorContext context;

    @Mock private ConstraintViolationBuilder violationBuilder;

    @Mock private ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new EntityUniqueIdentifierValidator();
    }

    private void setupMockViolation() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    }

    private DebtorInfo createDebtor(StEntityUniqueIdentifierType type, String value) {
        DebtorInfo debtor = new DebtorInfo();
        if (type != null || value != null) {
            CtEntityUniqueIdentifier uid = new CtEntityUniqueIdentifier();
            uid.setEntityUniqueIdentifierType(type);
            uid.setEntityUniqueIdentifierValue(value);
            debtor.setUniqueIdentifier(uid);
        }
        return debtor;
    }

    @Test
    void isValid_NullDebtor_ReturnsTrue() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void isValid_MissingOrBlankUniqueIdentifier_ReturnsFalse() {
        setupMockViolation();
        assertFalse(validator.isValid(new DebtorInfo(), context));
        assertFalse(validator.isValid(createDebtor(null, "RSSMRA85T10H501Z"), context));
        assertFalse(validator.isValid(createDebtor(StEntityUniqueIdentifierType.F, null), context));
        assertFalse(validator.isValid(createDebtor(StEntityUniqueIdentifierType.F, ""), context));
        assertFalse(validator.isValid(createDebtor(StEntityUniqueIdentifierType.F, "   "), context));
    }

    @Test
    void isValid_ValidFiscalCodeTypeF_ReturnsTrue() {
        assertTrue(
                validator.isValid(
                        createDebtor(StEntityUniqueIdentifierType.F, "RSSMRA85T10H501Z"), context));
    }

    @Test
    void isValid_InvalidFiscalCodeTypeF_ReturnsFalse() {
        setupMockViolation();
        assertFalse(
                validator.isValid(createDebtor(StEntityUniqueIdentifierType.F, "INVALID123456789"), context));
    }

    @Test
    void isValid_ValidVATTypeG_ReturnsTrue() {
        assertTrue(
                validator.isValid(createDebtor(StEntityUniqueIdentifierType.G, "12345678901"), context));
    }

    @Test
    void isValid_InvalidVATTypeG_ReturnsFalse() {
        setupMockViolation();
        assertFalse(
                validator.isValid(createDebtor(StEntityUniqueIdentifierType.G, "1234567890"), context));
        assertFalse(
                validator.isValid(createDebtor(StEntityUniqueIdentifierType.G, "1234567890A"), context));
    }
}