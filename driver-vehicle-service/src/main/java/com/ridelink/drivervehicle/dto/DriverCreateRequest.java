package com.ridelink.drivervehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body used to create a driver operational profile")
public record DriverCreateRequest(
        @Schema(description = "User ID from the Account Service (external identifier)", example = "acc-1001")
        @NotBlank(message = "accountId cannot be blank") String accountId,

        @Schema(example = "Nimal Perera")
        @NotBlank(message = "name cannot be blank") String name,

        @Schema(example = "0771234567")
        @NotBlank(message = "phone cannot be blank") String phone,

        @Schema(example = "B1234567")
        @NotBlank(message = "licenseNumber cannot be blank") String licenseNumber,

        @Schema(example = "Colombo")
        @NotBlank(message = "serviceArea cannot be blank") String serviceArea) {
}
