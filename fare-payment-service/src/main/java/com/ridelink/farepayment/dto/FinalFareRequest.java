package com.ridelink.farepayment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

public record FinalFareRequest(

        @DecimalMin(value = "0.1", message = "Actual distance must be greater than zero") double actualDistanceKm,

        @Min(value = 1, message = "Actual duration must be at least one minute") int actualDurationMinutes) {
}