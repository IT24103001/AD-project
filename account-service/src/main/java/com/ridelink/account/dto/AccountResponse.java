package com.ridelink.account.dto;

import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;

import java.time.Instant;

/** Safe view of an account. Deliberately has NO passwordHash field. */
public record AccountResponse(
        String id,
        String fullName,
        String email,
        String phone,
        Role role,
        AccountStatus accountStatus,
        Instant createdAt,
        Instant updatedAt) {
}
