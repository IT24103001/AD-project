package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.*;
import com.ridelink.drivervehicle.model.AvailabilityStatus;

import java.util.List;

/** Business operations for drivers. Controllers depend on this interface, not on the implementation (DIP). */
public interface DriverService {

    DriverResponse createDriver(DriverCreateRequest request);

    DriverResponse getDriverById(String id);

    List<DriverResponse> getAllDrivers();

    DriverResponse updateDriver(String id, DriverUpdateRequest request);

    void deleteDriver(String id);

    DriverResponse updateAvailability(String id, AvailabilityStatus status);

    DriverResponse updateLocation(String id, LocationUpdateRequest request);

    DriverResponse updateServiceArea(String id, ServiceAreaUpdateRequest request);

    List<AvailableDriverResponse> findAvailableDrivers(String serviceArea, Double latitude, Double longitude);
}
