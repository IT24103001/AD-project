package com.ridelink.drivervehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** accountId is intentionally missing: a driver profile cannot be moved to another account. */
@Schema(description = "Request body used to update a driver profile (full update)")
public record DriverUpdateRequest(
        @Schema(example = "Nimal Perera")
        @NotBlank(message = "name cannot be blank") String name,

        @Schema(example = "0779876543")
        @NotBlank(message = "phone cannot be blank") String phone,

        @Schema(example = "B1234567")
        @NotBlank(message = "licenseNumber cannot be blank") String licenseNumber,

        @Schema(example = "Colombo")
        @NotBlank(message = "serviceArea cannot be blank") String serviceArea) {
}
