package com.ridelink.ridemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Optional request payload for POST /api/rides/{id}/assign.
 * If driverId is omitted or null, the service synchronously queries Driver & Vehicle Service
 * (GET /api/drivers/available) and selects an eligible driver deterministically.
 */
@Schema(description = "Optional payload for driver assignment. Leave driverId empty to automatically select from Driver & Vehicle Service.")
public class AssignDriverRequest {

    @Schema(description = "Optional explicit driverId. If omitted, Ride Management Service fetches available drivers via GET /api/drivers/available", example = "DRV-2001")
    private String driverId;

    public AssignDriverRequest() {
    }

    public AssignDriverRequest(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
}
