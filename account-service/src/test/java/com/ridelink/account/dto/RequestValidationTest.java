package com.ridelink.account.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.ridelink.account.model.AccountStatus;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests Bean Validation annotations directly - no Spring context needed. */
class RequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    @Test
    @DisplayName("Valid registration request has no violations")
    void validRequest_noViolations() {
        var request = new RegisterRequest("Nimal Perera", "nimal@example.com", "+94771234567", "Passw0rd123");
        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("Blank name is rejected")
    void blankName_rejected() {
        var request = new RegisterRequest("   ", "nimal@example.com", "+94771234567", "Passw0rd123");
        assertThat(validator.validate(request)).extracting(v -> v.getPropertyPath().toString()).contains("fullName");
    }

    @Test
    @DisplayName("Invalid email is rejected")
    void invalidEmail_rejected() {
        var request = new RegisterRequest("Nimal", "not-an-email", "+94771234567", "Passw0rd123");
        assertThat(validator.validate(request)).extracting(v -> v.getPropertyPath().toString()).contains("email");
    }

    @Test
    @DisplayName("Invalid phone is rejected")
    void invalidPhone_rejected() {
        var request = new RegisterRequest("Nimal", "nimal@example.com", "abc123", "Passw0rd123");
        assertThat(validator.validate(request)).extracting(v -> v.getPropertyPath().toString()).contains("phone");
    }

    @Test
    @DisplayName("Weak passwords are rejected (too short / no digit)")
    void weakPassword_rejected() {
        var tooShort = new RegisterRequest("Nimal", "nimal@example.com", "+94771234567", "Ab1");
        var noDigit = new RegisterRequest("Nimal", "nimal@example.com", "+94771234567", "OnlyLettersHere");
        assertThat(validator.validate(tooShort)).extracting(v -> v.getPropertyPath().toString()).contains("password");
        assertThat(validator.validate(noDigit)).extracting(v -> v.getPropertyPath().toString()).contains("password");
    }

    @Test
    @DisplayName("Status is required")
    void nullStatus_rejected() {
        assertThat(validator.validate(new StatusUpdateRequest(null))).hasSize(1);
        assertThat(validator.validate(new StatusUpdateRequest(AccountStatus.ACTIVE))).isEmpty();
    }

    @Test
    @DisplayName("Profile update validates name and phone")
    void updateProfile_invalid_rejected() {
        var request = new UpdateProfileRequest("", "12");
        assertThat(validator.validate(request)).extracting(v -> v.getPropertyPath().toString())
                .contains("fullName", "phone");
    }
}
