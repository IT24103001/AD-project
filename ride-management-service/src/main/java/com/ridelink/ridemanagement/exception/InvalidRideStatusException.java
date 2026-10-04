package com.ridelink.ridemanagement.exception;

import com.ridelink.ridemanagement.model.RideStatus;

/**
 * Thrown when an operation attempts an invalid ride status transition
 * (for example, COMPLETED -> IN_PROGRESS or IN_PROGRESS -> CANCELLED).
 */
public class InvalidRideStatusException extends RuntimeException {

    public InvalidRideStatusException(String message) {
        super(message);
    }

    public InvalidRideStatusException(RideStatus currentStatus, RideStatus targetStatus) {
        super(String.format("Invalid ride status transition from %s to %s. Allowed next states from %s: %s",
                currentStatus, targetStatus, currentStatus, currentStatus.getAllowedNextStatuses()));
    }
}
