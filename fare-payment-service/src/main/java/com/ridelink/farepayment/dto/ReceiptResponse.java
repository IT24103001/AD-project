package com.ridelink.farepayment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReceiptResponse(
        String receiptNumber,
        String paymentId,
        String rideId,
        BigDecimal amount,
        String paymentMethod,
        String paymentStatus,
        LocalDateTime issuedAt) {
}