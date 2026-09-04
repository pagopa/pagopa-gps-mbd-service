package it.gov.pagopa.mbd.gps.service.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates the {@code uniqueIdentifier} of a debtor: it must be present, and its value must match
 * the format expected for the declared {@code entityUniqueIdentifierType} (16-character Italian
 * fiscal code for {@code F}, 11-digit VAT number for {@code G}).
 *
 * <p>Applied at class level because the nested {@code CtEntityUniqueIdentifier} type is generated
 * by wsimport from {@code paForNode.wsdl} and cannot carry Bean Validation annotations (they would
 * be lost on every rebuild).
 */
@Documented
@Constraint(validatedBy = EntityUniqueIdentifierValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEntityUniqueIdentifier {

  String message() default "Invalid debtor unique identifier";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
