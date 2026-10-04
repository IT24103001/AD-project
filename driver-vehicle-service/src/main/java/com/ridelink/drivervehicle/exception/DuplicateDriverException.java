package com.ridelink.drivervehicle.exception;

/** Thrown when accountId or licenseNumber is already used by another driver. */
public class DuplicateDriverException extends RuntimeException {
    public DuplicateDriverException(String message) {
        super(message);
    }
}
