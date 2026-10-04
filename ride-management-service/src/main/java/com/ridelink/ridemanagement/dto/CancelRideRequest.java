package com.ridelink.ridemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for cancelling a ride (POST /api/rides/{id}/cancel).
 */
@Schema(description = "Payload for cancelling an active ride before it is in progress or completed")
public class CancelRideRequest {

    @NotBlank(message = "Cancellation reason cannot be blank")
    @Size(min = 3, max = 250, message = "Cancellation reason must be between 3 and 250 characters")
    @Schema(description = "Reason for cancelling the ride", example = "Passenger travel plans changed", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;

    public CancelRideRequest() {
    }

    public CancelRideRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
