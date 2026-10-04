package com.ridelink.ridemanagement.dto;

/**
 * Response payload received from Fare & Payment Service after final fare calculation
 * and simulated payment recording.
 */
public class FareCalculationResponse {

    private String fareId;
    private String rideId;
    private Double finalFare;
    private String currency;
    private String paymentStatus;

    public FareCalculationResponse() {
    }

    public FareCalculationResponse(String fareId, String rideId, Double finalFare, String currency, String paymentStatus) {
        this.fareId = fareId;
        this.rideId = rideId;
        this.finalFare = finalFare;
        this.currency = currency;
        this.paymentStatus = paymentStatus;
    }

    public String getFareId() {
        return fareId;
    }

    public void setFareId(String fareId) {
        this.fareId = fareId;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public Double getFinalFare() {
        return finalFare;
    }

    public void setFinalFare(Double finalFare) {
        this.finalFare = finalFare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
