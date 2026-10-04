package com.ridelink.account.service;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.model.Account;

/** Converts the Account entity to a safe response DTO (passwordHash is never copied). */
public final class AccountMapper {

    private AccountMapper() { }

    public static AccountResponse toResponse(Account a) {
        return new AccountResponse(a.getId(), a.getFullName(), a.getEmail(), a.getPhone(),
                a.getRole(), a.getAccountStatus(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
