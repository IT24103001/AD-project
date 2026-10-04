package com.ridelink.ridemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new ride request (POST /api/rides).
 */
@Schema(description = "Payload to create a new ride request")
public class CreateRideRequest {

    @NotBlank(message = "passengerId is required and cannot be blank")
    @Schema(description = "Stable identifier of the passenger requesting the ride", example = "PASS-1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String passengerId;

    @NotBlank(message = "pickupLocation is required and cannot be blank")
    @Size(min = 3, max = 200, message = "pickupLocation must be between 3 and 200 characters")
    @Schema(description = "Pickup place name or simulated coordinates", example = "SLIIT Malabe Campus Main Gate", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pickupLocation;

    @NotBlank(message = "destinationLocation is required and cannot be blank")
    @Size(min = 3, max = 200, message = "destinationLocation must be between 3 and 200 characters")
    @Schema(description = "Destination place name or simulated coordinates", example = "Colombo Fort Railway Station", requiredMode = Schema.RequiredMode.REQUIRED)
    private String destinationLocation;

    @Schema(description = "If true, immediately requests available drivers from Driver & Vehicle Service and assigns one deterministically", example = "false", defaultValue = "false")
    private boolean autoAssignDriver = false;

    public CreateRideRequest() {
    }

    public CreateRideRequest(String passengerId, String pickupLocation, String destinationLocation, boolean autoAssignDriver) {
        this.passengerId = passengerId;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.autoAssignDriver = autoAssignDriver;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public boolean isAutoAssignDriver() {
        return autoAssignDriver;
    }

    public void setAutoAssignDriver(boolean autoAssignDriver) {
        this.autoAssignDriver = autoAssignDriver;
    }
}
