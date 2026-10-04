package com.ridelink.drivervehicle.mapper;

import com.ridelink.drivervehicle.dto.DriverCreateRequest;
import com.ridelink.drivervehicle.dto.DriverResponse;
import com.ridelink.drivervehicle.dto.DriverUpdateRequest;
import com.ridelink.drivervehicle.dto.LocationResponse;
import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.model.Location;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** Converts between Driver entities and DTOs so the database model is never exposed directly. */
@Component
public class DriverMapper {

    /** A brand-new driver always starts UNAVAILABLE until they have a vehicle and a location. */
    public Driver toEntity(DriverCreateRequest request) {
        Driver driver = new Driver();
        driver.setAccountId(request.accountId().trim());
        driver.setName(request.name().trim());
        driver.setPhone(request.phone().trim());
        driver.setLicenseNumber(request.licenseNumber().trim());
        driver.setServiceArea(request.serviceArea().trim());
        driver.setAvailabilityStatus(AvailabilityStatus.UNAVAILABLE);
        Instant now = Instant.now();
        driver.setCreatedAt(now);
        driver.setUpdatedAt(now);
        return driver;
    }

    public void applyUpdate(Driver driver, DriverUpdateRequest request) {
        driver.setName(request.name().trim());
        driver.setPhone(request.phone().trim());
        driver.setLicenseNumber(request.licenseNumber().trim());
        driver.setServiceArea(request.serviceArea().trim());
        driver.setUpdatedAt(Instant.now());
    }

    public DriverResponse toResponse(Driver driver) {
        return new DriverResponse(
                driver.getId(),
                driver.getAccountId(),
                driver.getName(),
                driver.getPhone(),
                driver.getLicenseNumber(),
                driver.getAvailabilityStatus(),
                driver.getServiceArea(),
                toLocationResponse(driver.getCurrentLocation()),
                driver.getCreatedAt(),
                driver.getUpdatedAt());
    }

    private LocationResponse toLocationResponse(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationResponse(location.getLatitude(), location.getLongitude());
    }
}
