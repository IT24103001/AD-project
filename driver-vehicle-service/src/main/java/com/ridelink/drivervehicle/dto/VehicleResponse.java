package com.ridelink.drivervehicle.dto;

import com.ridelink.drivervehicle.model.VehicleType;

import java.time.Instant;

public record VehicleResponse(
        String id,
        String driverId,
        String vehicleNumber,
        VehicleType vehicleType,
        String brand,
        String model,
        String color,
        Instant createdAt,
        Instant updatedAt) {
}
