package backoffice.common.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<EnumConstraint, String> {
  private EnumConstraint annotation;

  @Override
  public void initialize(EnumConstraint annotation) {
    this.annotation = annotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null)
      return false;

    Object[] enumValues = this.annotation.enumClass().getEnumConstants();
    return Arrays.stream(enumValues)
        .anyMatch(e -> value.equalsIgnoreCase(e.toString()));
  }
}
