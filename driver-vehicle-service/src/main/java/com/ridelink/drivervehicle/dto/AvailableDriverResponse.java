package com.ridelink.drivervehicle.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "An eligible driver. distanceKm is only filled when latitude/longitude were requested.")
public record AvailableDriverResponse(
        DriverResponse driver,
        @Schema(example = "2.19") Double distanceKm) {
}
