package com.ridelink.account.exception;

/** Authenticated user tries to do something they are not allowed to (maps to HTTP 403). */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
