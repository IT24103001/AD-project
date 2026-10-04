package com.ridelink.farepayment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record FareEstimateRequest(

        @NotBlank(message = "Ride ID is required") String rideId,

        @NotBlank(message = "Pickup location is required") String pickupLocation,

        @NotBlank(message = "Destination is required") String destination,

        @DecimalMin(value = "0.1", message = "Distance must be greater than zero") double distanceKm,

        @Min(value = 1, message = "Duration must be at least one minute") int durationMinutes) {
}