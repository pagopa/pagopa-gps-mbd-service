package it.gov.pagopa.mbd.gps.service.annotation;

import it.gov.pagopa.mbd.gps.service.model.MbdPaymentOptionRequestProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class MbdDebtorValidator
        implements ConstraintValidator<ValidMbdDebtor, MbdPaymentOptionRequestProperties> {

    @Override
    public boolean isValid(
            MbdPaymentOptionRequestProperties properties, ConstraintValidatorContext context) {
        if (properties.getDebtorFiscalCode().length() == 16) {
            boolean isNameValid = StringUtils.isNotBlank(properties.getDebtorName());

            if (!isNameValid) {
                context.disableDefaultConstraintViolation();
                context
                        .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode("debtorName")
                        .addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
