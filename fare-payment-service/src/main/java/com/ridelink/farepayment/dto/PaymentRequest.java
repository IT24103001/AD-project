package com.ridelink.farepayment.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(

        @NotBlank(message = "Ride ID is required") String rideId,

        @NotBlank(message = "Payment method is required") String paymentMethod,

        boolean simulateFailure

) {
}