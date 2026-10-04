package com.ridelink.ridemanagement.exception;

/**
 * Thrown when no eligible available driver can be obtained or assigned
 * from the Driver & Vehicle Service.
 */
public class DriverAssignmentException extends RuntimeException {

    public DriverAssignmentException(String message) {
        super(message);
    }

    public DriverAssignmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
