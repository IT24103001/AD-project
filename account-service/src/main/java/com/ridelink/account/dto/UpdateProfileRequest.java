package com.ridelink.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Only name and phone can be edited. Email, role and status are NOT editable here. */
public record UpdateProfileRequest(
        @Schema(example = "Nimal K. Perera")
        @NotBlank(message = "Full name must not be blank")
        @Size(max = 100, message = "Full name must be at most 100 characters")
        String fullName,

        @Schema(example = "+94771234999")
        @NotBlank(message = "Phone must not be blank")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Phone must contain 9-15 digits with an optional leading +")
        String phone) {
}
