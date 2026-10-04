package com.ridelink.farepayment.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.model.Fare;
import com.ridelink.farepayment.repository.FareRepository;

@Service
public class FareService {

    private static final BigDecimal BASE_FARE = new BigDecimal("200.00");
    private static final BigDecimal RATE_PER_KM = new BigDecimal("80.00");
    private static final BigDecimal RATE_PER_MINUTE = new BigDecimal("5.00");

    private final FareRepository fareRepository;

    public FareService(FareRepository fareRepository) {
        this.fareRepository = fareRepository;
    }

    public Fare calculateEstimate(FareEstimateRequest request) {

        BigDecimal distanceCharge = BigDecimal
                .valueOf(request.distanceKm())
                .multiply(RATE_PER_KM)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal timeCharge = BigDecimal
                .valueOf(request.durationMinutes())
                .multiply(RATE_PER_MINUTE)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal estimatedFare = BASE_FARE
                .add(distanceCharge)
                .add(timeCharge)
                .setScale(2, RoundingMode.HALF_UP);

        Fare fare = new Fare();
        fare.setRideId(request.rideId());
        fare.setPickupLocation(request.pickupLocation());
        fare.setDestination(request.destination());
        fare.setDistanceKm(request.distanceKm());
        fare.setDurationMinutes(request.durationMinutes());
        fare.setBaseFare(BASE_FARE);
        fare.setDistanceCharge(distanceCharge);
        fare.setTimeCharge(timeCharge);
        fare.setEstimatedFare(estimatedFare);
        fare.setStatus("ESTIMATED");
        fare.setCreatedAt(LocalDateTime.now());
        fare.setUpdatedAt(LocalDateTime.now());

        return fareRepository.save(fare);
    }

    public Fare getFareByRideId(String rideId) {
        return fareRepository
                .findTopByRideIdOrderByCreatedAtDesc(rideId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Fare record not found for ride ID: " + rideId));
    }
}