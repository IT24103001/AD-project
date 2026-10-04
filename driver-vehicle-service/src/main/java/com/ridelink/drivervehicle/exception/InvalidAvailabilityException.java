package com.ridelink.drivervehicle.exception;

/** Thrown when a driver breaks the rules for becoming AVAILABLE (see DriverServiceImpl.updateAvailability). */
public class InvalidAvailabilityException extends RuntimeException {
    public InvalidAvailabilityException(String message) {
        super(message);
    }
}
