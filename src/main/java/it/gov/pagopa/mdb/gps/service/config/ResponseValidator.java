package it.gov.pagopa.mdb.gps.service.config;

import it.gov.pagopa.mdb.gps.service.exception.AppException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Set;

@Aspect
@Component
public class ResponseValidator {

    private final Validator validator;

    @Autowired
    public ResponseValidator(Validator validator) {
        this.validator = validator;
    }

    /**
     * This method validates the response annotated with the {@link jakarta.validation.Constraint}
     *
     * @param joinPoint not used
     * @param result    the response to validate
     */
    @AfterReturning(pointcut = "execution(* it.gov.pagopa.mdb.gps.service.controller.*.*(..))", returning = "result")
    public void validateResponse(JoinPoint joinPoint, Object result) {
        if (result instanceof ResponseEntity) {
            validateResponse((ResponseEntity<?>) result);
        }
    }

    private <T> void validateResponse(ResponseEntity<T> response) {
        if (response.getBody() != null) {
            Set<ConstraintViolation<T>> validationResults = validator.validate(response.getBody());

            if (!validationResults.isEmpty()) {
                var sb = new StringBuilder();
                for (ConstraintViolation<T> error : validationResults) {
                    sb.append(error.getPropertyPath()).append(" ").append(error.getMessage()).append(". ");
                }
                var msg = StringUtils.chop(sb.toString());
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response", msg);
            }
        }
    }
}
