package org.sugar.media.validation.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


/**
 * Date:2024/11/13 15:56:18
 * Author：Tobin
 * Description:
 */

@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValidatorInterface {
    Class<? extends Enum<?>> enumClass();

    String message() default "Value is not a valid enum constant";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
