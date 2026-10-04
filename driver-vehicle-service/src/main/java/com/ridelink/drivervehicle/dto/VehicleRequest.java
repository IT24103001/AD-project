package com.ridelink.drivervehicle.dto;

import com.ridelink.drivervehicle.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body used to create or update a vehicle")
public record VehicleRequest(
        @Schema(description = "ID of the owning driver (drivers collection of this service)", example = "665f1c2e9a1b2c3d4e5f6a7b")
        @NotBlank(message = "driverId cannot be blank") String driverId,

        @Schema(example = "CAB-1234")
        @NotBlank(message = "vehicleNumber cannot be blank") String vehicleNumber,

        @Schema(description = "CAR, VAN or SUV", example = "CAR")
        @NotNull(message = "vehicleType is required (CAR, VAN or SUV)") VehicleType vehicleType,

        @Schema(example = "Toyota") String brand,
        @Schema(example = "Prius") String model,
        @Schema(example = "White") String color) {
}
