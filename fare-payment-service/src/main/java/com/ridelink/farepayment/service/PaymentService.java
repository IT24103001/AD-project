package com.ridelink.farepayment.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ridelink.farepayment.dto.PaymentRequest;
import com.ridelink.farepayment.dto.ReceiptResponse;
import com.ridelink.farepayment.model.Fare;
import com.ridelink.farepayment.model.Payment;
import com.ridelink.farepayment.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final FareService fareService;

    public PaymentService(
            PaymentRepository paymentRepository,
            FareService fareService) {

        this.paymentRepository = paymentRepository;
        this.fareService = fareService;
    }

    public Payment recordPayment(PaymentRequest request) {

        Fare fare = fareService.getFareByRideId(request.rideId());

        if (fare.getFinalFare() == null
                || !"FINALIZED".equals(fare.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Final fare must be calculated before payment");
        }

        boolean failed = request.simulateFailure();

        String status = failed ? "FAILED" : "SUCCESS";

        String transactionReference = failed
                ? null
                : "TXN-" + generateNumber();

        String receiptNumber = failed
                ? null
                : "RCT-" + generateNumber();

        Payment payment = new Payment(
                request.rideId(),
                fare.getFinalFare(),
                request.paymentMethod().toUpperCase(),
                status,
                transactionReference,
                receiptNumber,
                LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public Payment getPayment(String paymentId) {

        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Payment not found"));
    }

    public ReceiptResponse getReceipt(String paymentId) {

        Payment payment = getPayment(paymentId);

        if (!"SUCCESS".equals(payment.getStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Receipt is available only for successful payments");
        }

        return new ReceiptResponse(
                payment.getReceiptNumber(),
                payment.getId(),
                payment.getRideId(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                payment.getCreatedAt());
    }

    private String generateNumber() {

        return UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}