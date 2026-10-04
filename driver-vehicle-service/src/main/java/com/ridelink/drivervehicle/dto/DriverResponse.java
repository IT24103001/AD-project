package com.ridelink.drivervehicle.dto;

import com.ridelink.drivervehicle.model.AvailabilityStatus;

import java.time.Instant;

public record DriverResponse(
        String id,
        String accountId,
        String name,
        String phone,
        String licenseNumber,
        AvailabilityStatus availabilityStatus,
        String serviceArea,
        LocationResponse currentLocation,
        Instant createdAt,
        Instant updatedAt) {
}
