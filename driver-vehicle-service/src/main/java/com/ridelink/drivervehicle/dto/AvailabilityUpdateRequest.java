package com.ridelink.drivervehicle.dto;

import com.ridelink.drivervehicle.model.AvailabilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body to change driver availability")
public record AvailabilityUpdateRequest(
        @Schema(description = "AVAILABLE or UNAVAILABLE", example = "AVAILABLE")
        @NotNull(message = "availabilityStatus is required (AVAILABLE or UNAVAILABLE)")
        AvailabilityStatus availabilityStatus) {
}
