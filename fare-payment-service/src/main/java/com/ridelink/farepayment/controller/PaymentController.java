package com.ridelink.farepayment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.farepayment.dto.PaymentRequest;
import com.ridelink.farepayment.dto.ReceiptResponse;
import com.ridelink.farepayment.model.Payment;
import com.ridelink.farepayment.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment recordPayment(
            @Valid @RequestBody PaymentRequest request) {

        return paymentService.recordPayment(request);
    }

    @GetMapping("/{paymentId}")
    public Payment getPayment(
            @PathVariable String paymentId) {

        return paymentService.getPayment(paymentId);
    }

    @GetMapping("/{paymentId}/receipt")
    public ReceiptResponse getReceipt(
            @PathVariable String paymentId) {

        return paymentService.getReceipt(paymentId);
    }
}