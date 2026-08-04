package it.gov.pagopa.mbd.gps.service.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MbdDebtorValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMbdDebtor {

  String message() default
      "Debtor name is required for Physical Persons (16-character fiscal code)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
