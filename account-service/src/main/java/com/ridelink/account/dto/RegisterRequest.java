package com.ridelink.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body for both passenger and driver registration. The role is decided by the endpoint, not the client. */
public record RegisterRequest(
        @Schema(example = "Nimal Perera")
        @NotBlank(message = "Full name must not be blank")
        @Size(max = 100, message = "Full name must be at most 100 characters")
        String fullName,

        @Schema(example = "nimal@example.com")
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        String email,

        @Schema(example = "+94771234567")
        @NotBlank(message = "Phone must not be blank")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Phone must contain 9-15 digits with an optional leading +")
        String phone,

        @Schema(example = "Passw0rd123")
        @NotBlank(message = "Password must not be blank")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                message = "Password must be 8-64 characters and contain at least one letter and one digit")
        String password) {
}
