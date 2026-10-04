package com.ridelink.drivervehicle.exception;

/** Thrown when the available-driver search parameters are inconsistent (e.g. latitude without longitude). */
public class InvalidSearchCriteriaException extends RuntimeException {
    public InvalidSearchCriteriaException(String message) {
        super(message);
    }
}
