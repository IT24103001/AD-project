package com.ridelink.farepayment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String rideId;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private String transactionReference;
    private String receiptNumber;
    private LocalDateTime createdAt;

    public Payment() {
    }

    public Payment(
            String rideId,
            BigDecimal amount,
            String paymentMethod,
            String status,
            String transactionReference,
            String receiptNumber,
            LocalDateTime createdAt) {

        this.rideId = rideId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.transactionReference = transactionReference;
        this.receiptNumber = receiptNumber;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getRideId() {
        return rideId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}