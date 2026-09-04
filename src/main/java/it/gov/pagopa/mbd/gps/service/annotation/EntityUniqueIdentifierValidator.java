package it.gov.pagopa.mbd.gps.service.annotation;

import it.gov.pagopa.mbd.gps.service.model.marcadabollo.DebtorInfo;
import it.gov.pagopa.mbd.gps.service.model.partner.CtEntityUniqueIdentifier;
import it.gov.pagopa.mbd.gps.service.model.partner.StEntityUniqueIdentifierType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

public class EntityUniqueIdentifierValidator
    implements ConstraintValidator<ValidEntityUniqueIdentifier, DebtorInfo> {

  private static final String FISCAL_CODE_PATTERN =
      "^[A-Za-z]{6}[0-9]{2}[A-Za-z][0-9]{2}[A-Za-z][0-9]{3}[A-Za-z]$";
  private static final String VAT_NUMBER_PATTERN = "^[0-9]{11}$";

  @Override
  public boolean isValid(DebtorInfo debtor, ConstraintValidatorContext context) {
    if (debtor == null) {
      return true;
    }

    CtEntityUniqueIdentifier uid = debtor.getUniqueIdentifier();
    if (uid == null
        || uid.getEntityUniqueIdentifierType() == null
        || StringUtils.isBlank(uid.getEntityUniqueIdentifierValue())) {
      return buildViolation(context, "Debtor unique identifier type and value are required");
    }

    StEntityUniqueIdentifierType type = uid.getEntityUniqueIdentifierType();
    String value = uid.getEntityUniqueIdentifierValue();

    boolean valid =
        switch (type) {
          case F -> value.matches(FISCAL_CODE_PATTERN);
          case G -> value.matches(VAT_NUMBER_PATTERN);
        };

    if (!valid) {
      String message =
          type == StEntityUniqueIdentifierType.F
              ? "Debtor fiscal code must be a valid 16-character Italian fiscal code for type F"
              : "Debtor identifier must be an 11-digit VAT number for type G";
      return buildViolation(context, message);
    }

    return true;
  }

  private boolean buildViolation(ConstraintValidatorContext context, String message) {
    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(message)
        .addPropertyNode("uniqueIdentifier")
        .addConstraintViolation();
    return false;
  }
}
