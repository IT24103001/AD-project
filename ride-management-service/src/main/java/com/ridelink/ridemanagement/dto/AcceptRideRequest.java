package com.ridelink.ridemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for a driver accepting an assigned ride (POST /api/rides/{id}/accept).
 */
@Schema(description = "Payload for a driver to accept an assigned ride")
public class AcceptRideRequest {

    @NotBlank(message = "driverId is required to accept a ride")
    @Schema(description = "Stable identifier of the assigned driver accepting the ride", example = "DRV-2001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String driverId;

    public AcceptRideRequest() {
    }

    public AcceptRideRequest(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
}
