package com.ridelink.drivervehicle.exception;

/** Thrown when a vehicle number is already registered. */
public class DuplicateVehicleException extends RuntimeException {
    public DuplicateVehicleException(String vehicleNumber) {
        super("Vehicle number already registered: " + vehicleNumber);
    }
}
