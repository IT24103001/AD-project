package com.ridelink.drivervehicle.service.impl;

import com.ridelink.drivervehicle.dto.*;
import com.ridelink.drivervehicle.exception.*;
import com.ridelink.drivervehicle.mapper.DriverMapper;
import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.model.Location;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import com.ridelink.drivervehicle.service.DriverService;
import com.ridelink.drivervehicle.util.DistanceCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * All driver business rules live here (controllers only pass data through).
 */
@Service
public class DriverServiceImpl implements DriverService {

    private static final Logger log = LoggerFactory.getLogger(DriverServiceImpl.class);

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverMapper driverMapper;
    private final double searchRadiusKm;

    // Constructor injection: dependencies are explicit and easy to replace with mocks in unit tests.
    public DriverServiceImpl(DriverRepository driverRepository,
                             VehicleRepository vehicleRepository,
                             DriverMapper driverMapper,
                             @Value("${ridelink.eligibility.search-radius-km:5.0}") double searchRadiusKm) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.driverMapper = driverMapper;
        this.searchRadiusKm = searchRadiusKm;
    }

    @Override
    public DriverResponse createDriver(DriverCreateRequest request) {
        // Rule: one driver profile per Account Service user, and licence numbers are unique.
        if (driverRepository.existsByAccountId(request.accountId().trim())) {
            throw new DuplicateDriverException("A driver profile already exists for accountId: " + request.accountId());
        }
        if (driverRepository.existsByLicenseNumber(request.licenseNumber().trim())) {
            throw new DuplicateDriverException("Licence number already registered: " + request.licenseNumber());
        }
        Driver saved = driverRepository.save(driverMapper.toEntity(request));
        log.info("Driver created with id {}", saved.getId());
        return driverMapper.toResponse(saved);
    }

    @Override
    public DriverResponse getDriverById(String id) {
        return driverMapper.toResponse(findDriverOrThrow(id));
    }

    @Override
    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream().map(driverMapper::toResponse).toList();
    }

    @Override
    public DriverResponse updateDriver(String id, DriverUpdateRequest request) {
        Driver driver = findDriverOrThrow(id);
        // Rule: the new licence number must not belong to a DIFFERENT driver.
        if (driverRepository.existsByLicenseNumberAndIdNot(request.licenseNumber().trim(), id)) {
            throw new DuplicateDriverException("Licence number already registered: " + request.licenseNumber());
        }
        driverMapper.applyUpdate(driver, request);
        return driverMapper.toResponse(driverRepository.save(driver));
    }

    @Override
    public void deleteDriver(String id) {
        if (!driverRepository.existsById(id)) {
            throw new DriverNotFoundException(id);
        }
        // Rule: vehicles belong to a driver, so they are removed with the driver (no orphan vehicles).
        vehicleRepository.deleteByDriverId(id);
        driverRepository.deleteById(id);
        log.info("Driver {} and their vehicles deleted", id);
    }

    @Override
    public DriverResponse updateAvailability(String id, AvailabilityStatus status) {
        Driver driver = findDriverOrThrow(id);

        // Rule: a driver may only become AVAILABLE if Ride Management could really use them,
        // i.e. we know where they are and what vehicle they drive. Going UNAVAILABLE is always allowed.
        if (status == AvailabilityStatus.AVAILABLE) {
            if (driver.getCurrentLocation() == null) {
                throw new InvalidAvailabilityException("Driver cannot be AVAILABLE without a current location");
            }
            if (!vehicleRepository.existsByDriverId(id)) {
                throw new InvalidAvailabilityException("Driver cannot be AVAILABLE without a registered vehicle");
            }
        }
        driver.setAvailabilityStatus(status);
        driver.setUpdatedAt(Instant.now());
        return driverMapper.toResponse(driverRepository.save(driver));
    }

    @Override
    public DriverResponse updateLocation(String id, LocationUpdateRequest request) {
        Driver driver = findDriverOrThrow(id);
        driver.setCurrentLocation(new Location(request.latitude(), request.longitude()));
        driver.setUpdatedAt(Instant.now());
        return driverMapper.toResponse(driverRepository.save(driver));
    }

    @Override
    public DriverResponse updateServiceArea(String id, ServiceAreaUpdateRequest request) {
        Driver driver = findDriverOrThrow(id);
        driver.setServiceArea(request.serviceArea().trim());
        driver.setUpdatedAt(Instant.now());
        return driverMapper.toResponse(driverRepository.save(driver));
    }

    /**
     * ELIGIBILITY RULE - a driver is eligible when:
     *  1. availabilityStatus = AVAILABLE
     *  2. if serviceArea is given: it equals the driver's serviceArea (ignoring upper/lower case)
     *  3. if latitude+longitude are given: the driver has a location and is within the configured radius (km)
     * Results with coordinates are sorted nearest first.
     */
    @Override
    public List<AvailableDriverResponse> findAvailableDrivers(String serviceArea, Double latitude, Double longitude) {
        // Coordinates only make sense as a pair.
        if ((latitude == null) != (longitude == null)) {
            throw new InvalidSearchCriteriaException("latitude and longitude must be provided together");
        }
        boolean filterByArea = serviceArea != null && !serviceArea.isBlank();
        boolean filterByLocation = latitude != null;

        // Rules 1 and 2 are answered by the database query.
        List<Driver> candidates = filterByArea
                ? driverRepository.findByAvailabilityStatusAndServiceAreaIgnoreCase(AvailabilityStatus.AVAILABLE, serviceArea.trim())
                : driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);

        if (!filterByLocation) {
            return candidates.stream()
                    .map(driver -> new AvailableDriverResponse(driverMapper.toResponse(driver), null))
                    .toList();
        }

        // Rule 3 is applied in Java with the Haversine distance.
        List<AvailableDriverResponse> eligible = new ArrayList<>();
        for (Driver driver : candidates) {
            Location location = driver.getCurrentLocation();
            if (location == null) {
                continue; // we cannot measure the distance of a driver with no known position
            }
            double distance = DistanceCalculator.calculateKm(latitude, longitude,
                    location.getLatitude(), location.getLongitude());
            if (distance <= searchRadiusKm) {
                eligible.add(new AvailableDriverResponse(driverMapper.toResponse(driver), roundToTwoDecimals(distance)));
            }
        }
        eligible.sort(Comparator.comparing(AvailableDriverResponse::distanceKm));
        log.debug("Found {} eligible drivers within {} km", eligible.size(), searchRadiusKm);
        return eligible;
    }

    private Driver findDriverOrThrow(String id) {
        return driverRepository.findById(id).orElseThrow(() -> new DriverNotFoundException(id));
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
