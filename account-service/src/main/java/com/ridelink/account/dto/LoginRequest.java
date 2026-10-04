package com.ridelink.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "nimal@example.com")
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        String email,

        @Schema(example = "Passw0rd123")
        @NotBlank(message = "Password must not be blank")
        String password) {
}
