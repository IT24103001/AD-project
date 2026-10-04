package com.ridelink.ridemanagement.exception;

/**
 * Thrown when a requested ride ID does not exist in ridelink_ride_db.
 */
public class RideNotFoundException extends RuntimeException {

    public RideNotFoundException(String message) {
        super(message);
    }
}
