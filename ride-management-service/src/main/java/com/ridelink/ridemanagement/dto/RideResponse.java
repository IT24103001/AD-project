package com.ridelink.ridemanagement.dto;

import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Standard API response representation of a Ride resource.
 */
@Schema(description = "Ride resource representation returned by Ride Management Service")
public class RideResponse {

    @Schema(description = "Unique ride identifier", example = "665f1a2b3c4d5e6f7a8b9c0d")
    private String id;

    @Schema(description = "Stable passenger identifier", example = "PASS-1001")
    private String passengerId;

    @Schema(description = "Stable assigned driver identifier", example = "DRV-2001")
    private String driverId;

    @Schema(description = "Pickup location name or coordinates", example = "SLIIT Malabe Campus Main Gate")
    private String pickupLocation;

    @Schema(description = "Destination location name or coordinates", example = "Colombo Fort Railway Station")
    private String destinationLocation;

    @Schema(description = "Current lifecycle status of the ride", example = "ASSIGNED")
    private RideStatus rideStatus;

    @Schema(description = "Assigned vehicle registration number from Driver & Vehicle Service", example = "CAB-4582")
    private String vehicleNumber;

    @Schema(description = "Calculated final fare from Fare & Payment Service upon completion", example = "1450.00")
    private Double finalFare;

    @Schema(description = "Fare record identifier from Fare & Payment Service", example = "FARE-9001")
    private String fareId;

    @Schema(description = "Simulated payment status from Fare & Payment Service", example = "PAID")
    private String paymentStatus;

    @Schema(description = "Reason provided if the ride was cancelled", example = "Passenger travel plans changed")
    private String cancellationReason;

    private Instant requestedAt;
    private Instant assignedAt;
    private Instant acceptedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private Instant createdAt;
    private Instant updatedAt;

    public RideResponse() {
    }

    public static RideResponse fromEntity(Ride ride) {
        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setPassengerId(ride.getPassengerId());
        response.setDriverId(ride.getDriverId());
        response.setPickupLocation(ride.getPickupLocation());
        response.setDestinationLocation(ride.getDestinationLocation());
        response.setRideStatus(ride.getRideStatus());
        response.setVehicleNumber(ride.getVehicleNumber());
        response.setFinalFare(ride.getFinalFare());
        response.setFareId(ride.getFareId());
        response.setPaymentStatus(ride.getPaymentStatus());
        response.setCancellationReason(ride.getCancellationReason());
        response.setRequestedAt(ride.getRequestedAt());
        response.setAssignedAt(ride.getAssignedAt());
        response.setAcceptedAt(ride.getAcceptedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        response.setCancelledAt(ride.getCancelledAt());
        response.setCreatedAt(ride.getCreatedAt());
        response.setUpdatedAt(ride.getUpdatedAt());
        return response;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public RideStatus getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(RideStatus rideStatus) {
        this.rideStatus = rideStatus;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Double getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(Double finalFare) {
        this.finalFare = finalFare;
    }

    public String getFareId() {
        return fareId;
    }

    public void setFareId(String fareId) {
        this.fareId = fareId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
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

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
