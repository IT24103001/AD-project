package com.ridelink.account.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        AccountResponse user) {
}
