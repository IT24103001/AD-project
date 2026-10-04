package com.ridelink.account.service;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}
