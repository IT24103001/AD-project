package com.ridelink.drivervehicle.model;

/**
 * Simulated GPS position (embedded inside the Driver document, so it needs no collection of its own).
 * Latitude/longitude were chosen so a simple distance calculation is possible.
 */
public class Location {

    private Double latitude;
    private Double longitude;

    public Location() {
    }

    public Location(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
