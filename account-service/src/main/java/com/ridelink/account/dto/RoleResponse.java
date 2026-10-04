package com.ridelink.account.dto;

import com.ridelink.account.model.Role;

public record RoleResponse(String accountId, Role role) {
}
