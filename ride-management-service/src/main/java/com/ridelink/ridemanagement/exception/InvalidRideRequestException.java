package com.ridelink.ridemanagement.exception;

/**
 * Thrown when a ride request or operation contains invalid business parameters
 * (such as identical pickup and destination locations, or mismatched driverId).
 */
public class InvalidRideRequestException extends RuntimeException {

    public InvalidRideRequestException(String message) {
        super(message);
    }
}
