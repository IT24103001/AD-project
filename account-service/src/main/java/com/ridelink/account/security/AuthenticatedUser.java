package com.ridelink.account.security;

import com.ridelink.account.model.Role;

/** The logged-in user, rebuilt from the JWT on every request (no database lookup). */
public record AuthenticatedUser(String id, String email, Role role) {
}
