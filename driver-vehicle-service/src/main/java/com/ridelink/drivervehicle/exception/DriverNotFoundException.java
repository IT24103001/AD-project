package com.ridelink.drivervehicle.exception;

public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException(String driverId) {
        super("Driver not found with id: " + driverId);
    }
}
