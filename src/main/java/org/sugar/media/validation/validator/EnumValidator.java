package org.sugar.media.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

/**
 * Date:2024/11/13 15:58:38
 * Author：Tobin
 * Description:
 */
public class EnumValidator implements ConstraintValidator<EnumValidatorInterface, String> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(EnumValidatorInterface annotation) {
        this.enumClass = annotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false; // 如果允许空值，可直接返回 true；否则，返回 false
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(e -> e.name().equals(value));
    }
}
