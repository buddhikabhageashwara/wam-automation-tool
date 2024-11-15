package wam.automationtool.application.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class ValidDateTimeValidator implements ConstraintValidator<ValidDateTime, String> {

  private String format;

  @Override
  public void initialize(ValidDateTime constraintAnnotation) {
    this.format = constraintAnnotation.format();
  }

  @Override
  public boolean isValid(String date, ConstraintValidatorContext context) {
    if (Objects.isNull(date)) {
      return true;
    }
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
    try {
      LocalDateTime.parse(date, formatter);
      return true;
    } catch (final DateTimeParseException e) {
      return false;
    }
  }
}
