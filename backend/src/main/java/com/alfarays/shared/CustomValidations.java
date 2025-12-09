package com.alfarays.shared;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.passay.*;
import org.springframework.beans.BeanUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.passay.EnglishCharacterData.*;

public class CustomValidations {

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = PasswordMatchValidator.class)
    public @interface PasswordMatch {
        String message() default "Password and confirm password do not match.";

        String password();

        String confirmPassword();

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};
    }

    public static class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {
        private String password;
        private String confirmPassword;

        @Override
        public void initialize(PasswordMatch constraintAnnotation) {
            this.password = constraintAnnotation.password();
            this.confirmPassword = constraintAnnotation.confirmPassword();
        }

        @Override
        public boolean isValid(Object object, ConstraintValidatorContext context) {
            try {
                Object passwordValue = BeanUtils
                        .getPropertyDescriptor(object.getClass(), password)
                        .getReadMethod()
                        .invoke(object);

                Object confirmPasswordValue = BeanUtils
                        .getPropertyDescriptor(object.getClass(), confirmPassword)
                        .getReadMethod()
                        .invoke(object);

                return passwordValue != null && passwordValue.equals(confirmPasswordValue);
            } catch(Exception e) {
                return false;
            }
        }

    }


    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = StrongPasswordValidator.class)
    public @interface StrongPassword {
        String message() default "Password is too weak";

        Class<?>[] groups() default {};

        Class<? extends Payload>[] payload() default {};

        int minLength() default 8;

        int maxLength() default 64;
    }

    public static class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
        private int minLength;
        private int maxLength;

        @Override
        public void initialize(StrongPassword constraintAnnotation) {
            this.minLength = constraintAnnotation.minLength();
            this.maxLength = constraintAnnotation.maxLength();
        }

        @Override
        public boolean isValid(String password, ConstraintValidatorContext context) {
            if(password == null) return false;
            return new PasswordValidator(Arrays.asList(
                    new LengthRule(minLength, maxLength),
                    new CharacterRule(UpperCase, 1),
                    new CharacterRule(LowerCase, 1),
                    new CharacterRule(Digit, 1),
                    new CharacterRule(Special, 1),
                    new WhitespaceRule()
            )).validate(new PasswordData(password))
                    .isValid();
        }
    }

    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = UsernameValidator.class)
    public @interface ValidUsername {
        String message() default "Invalid username";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
        int minLength() default 4;
        int maxLength() default 20;
    }


    public static class UsernameValidator implements ConstraintValidator<ValidUsername, String> {
        private static final List<String> FORBIDDEN_USERNAMES = Arrays.asList(
                "admin", "administrator", "root",
                "system", "username", "password",
                "user", "test", "guest"
        );
        private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._@#&-]+$");
        private int minLength;
        private int maxLength;

        @Override
        public void initialize(ValidUsername constraintAnnotation) {
            this.minLength = constraintAnnotation.minLength();
            this.maxLength = constraintAnnotation.maxLength();
        }

        @Override
        public boolean isValid(String username, ConstraintValidatorContext context) {
            if(username == null) return false;
            if(username.length() < minLength || username.length() > maxLength) {
                return false;
            }
            if(FORBIDDEN_USERNAMES.stream().anyMatch(forbidden -> username.toLowerCase().contains(forbidden))) {
                return false;
            }
            return USERNAME_PATTERN.matcher(username).matches();
        }
    }

}
