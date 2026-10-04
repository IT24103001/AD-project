package com.ridelink.drivervehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body to change the driver's service area")
public record ServiceAreaUpdateRequest(
        @Schema(example = "Colombo")
        @NotBlank(message = "serviceArea cannot be blank") String serviceArea) {
}
