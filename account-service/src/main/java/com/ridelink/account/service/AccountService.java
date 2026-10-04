package com.ridelink.account.service;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.RoleResponse;
import com.ridelink.account.dto.StatusUpdateRequest;
import com.ridelink.account.dto.UpdateProfileRequest;
import com.ridelink.account.security.AuthenticatedUser;

/** Account use-cases. Controllers depend on this interface, not on the implementation (DIP). */
public interface AccountService {
    AccountResponse registerPassenger(RegisterRequest request);
    AccountResponse registerDriver(RegisterRequest request);
    AccountResponse getAccount(String id, AuthenticatedUser caller);
    AccountResponse updateProfile(String id, UpdateProfileRequest request, AuthenticatedUser caller);
    AccountResponse updateStatus(String id, StatusUpdateRequest request, AuthenticatedUser caller);
    RoleResponse getRole(String id, AuthenticatedUser caller);
}
