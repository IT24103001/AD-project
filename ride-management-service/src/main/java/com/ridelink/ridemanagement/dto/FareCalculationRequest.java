package com.ridelink.ridemanagement.dto;

import java.time.Instant;

/**
 * Request payload sent synchronously to Fare & Payment Service (POST /api/fares/calculate)
 * upon ride completion.
 */
public class FareCalculationRequest {

    private String rideId;
    private String passengerId;
    private String driverId;
    private String pickupLocation;
    private String destinationLocation;
    private Instant startedAt;
    private Instant completedAt;

    public FareCalculationRequest() {
    }

    public FareCalculationRequest(String rideId, String passengerId, String driverId, String pickupLocation, String destinationLocation, Instant startedAt, Instant completedAt) {
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
