package org.limir.expensetracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The value is an existing ISO 4217 currency code, e.g. KZT. {@code null} is valid: combine with @NotNull. */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsoCurrencyValidator.class)
public @interface IsoCurrency {

    String message() default "must be an ISO 4217 currency code, e.g. KZT";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
