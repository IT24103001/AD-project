package com.ridelink.farepayment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ridelink.farepayment.model.Payment;

public interface PaymentRepository
        extends MongoRepository<Payment, String> {
}