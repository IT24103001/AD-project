package com.ridelink.account.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        // Same message for "unknown email" and "wrong password" so attackers cannot tell which one failed.
        super("Invalid email or password");
    }
}
