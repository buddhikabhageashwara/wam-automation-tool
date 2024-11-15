package wam.automationtool.application.config;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ValidDateTimeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateTime {
    String message() default "Invalid date-time format";
    String format() default "yyyy-MM-dd HH:mm:ss.SSSSSS";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
