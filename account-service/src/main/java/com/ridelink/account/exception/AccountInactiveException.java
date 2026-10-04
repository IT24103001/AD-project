package com.ridelink.account.exception;

import com.ridelink.account.model.AccountStatus;

public class AccountInactiveException extends RuntimeException {
    public AccountInactiveException(AccountStatus status) {
        super("Account is " + status + " and cannot log in. Please contact support.");
    }
}
