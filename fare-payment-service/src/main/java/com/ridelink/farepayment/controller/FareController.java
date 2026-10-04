package com.ridelink.farepayment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ridelink.farepayment.dto.FareEstimateRequest;
import com.ridelink.farepayment.model.Fare;
import com.ridelink.farepayment.service.FareService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fares")
public class FareController {

    private final FareService fareService;

    public FareController(FareService fareService) {
        this.fareService = fareService;
    }

    @PostMapping("/estimate")
    @ResponseStatus(HttpStatus.CREATED)
    public Fare calculateEstimate(
            @Valid @RequestBody FareEstimateRequest request) {

        return fareService.calculateEstimate(request);
    }

    @GetMapping("/{rideId}")
    public Fare getFareByRideId(
            @PathVariable String rideId) {

        return fareService.getFareByRideId(rideId);
    }
}