package com.ridelink.ridemanagement.dto;

/**
 * DTO representing an eligible available driver returned by Driver & Vehicle Service
 * via GET /api/drivers/available.
 */
public class AvailableDriverDto {

    private String driverId;
    private String driverName;
    private String vehicleNumber;
    private String vehicleType;
    private boolean available;
    private String serviceArea;
    private String currentLocation;

    public AvailableDriverDto() {
    }

    public AvailableDriverDto(String driverId, String driverName, String vehicleNumber, String vehicleType, boolean available, String serviceArea, String currentLocation) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.available = available;
        this.serviceArea = serviceArea;
        this.currentLocation = currentLocation;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }
}
