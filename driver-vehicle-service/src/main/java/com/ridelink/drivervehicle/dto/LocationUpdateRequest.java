package com.ridelink.drivervehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Simulated current location of the driver")
public record LocationUpdateRequest(
        @Schema(description = "Latitude between -90 and 90", example = "6.9271")
        @NotNull(message = "latitude is required")
        @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "latitude must be <= 90") Double latitude,

        @Schema(description = "Longitude between -180 and 180", example = "79.8612")
        @NotNull(message = "longitude is required")
        @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "longitude must be <= 180") Double longitude) {
}
