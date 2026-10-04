package com.ridelink.drivervehicle.service.impl;

import com.ridelink.drivervehicle.dto.VehicleRequest;
import com.ridelink.drivervehicle.dto.VehicleResponse;
import com.ridelink.drivervehicle.exception.DriverNotFoundException;
import com.ridelink.drivervehicle.exception.DuplicateVehicleException;
import com.ridelink.drivervehicle.exception.VehicleNotFoundException;
import com.ridelink.drivervehicle.mapper.VehicleMapper;
import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import com.ridelink.drivervehicle.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class VehicleServiceImpl implements VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleServiceImpl.class);

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final VehicleMapper vehicleMapper;

    public VehicleServiceImpl(VehicleRepository vehicleRepository,
                              DriverRepository driverRepository,
                              VehicleMapper vehicleMapper) {
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
        this.vehicleMapper = vehicleMapper;
    }

    @Override
    public VehicleResponse createVehicle(VehicleRequest request) {
        assertDriverExists(request.driverId().trim());
        String vehicleNumber = VehicleMapper.normalizeVehicleNumber(request.vehicleNumber());
        // Rule: a plate number can be registered only once.
        if (vehicleRepository.existsByVehicleNumber(vehicleNumber)) {
            throw new DuplicateVehicleException(vehicleNumber);
        }
        Vehicle saved = vehicleRepository.save(vehicleMapper.toEntity(request));
        log.info("Vehicle {} created for driver {}", saved.getId(), saved.getDriverId());
        return vehicleMapper.toResponse(saved);
    }

    @Override
    public VehicleResponse getVehicleById(String id) {
        return vehicleMapper.toResponse(findVehicleOrThrow(id));
    }

    @Override
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(vehicleMapper::toResponse).toList();
    }

    @Override
    public List<VehicleResponse> getVehiclesByDriverId(String driverId) {
        assertDriverExists(driverId);
        return vehicleRepository.findByDriverId(driverId).stream().map(vehicleMapper::toResponse).toList();
    }

    @Override
    public VehicleResponse updateVehicle(String id, VehicleRequest request) {
        Vehicle vehicle = findVehicleOrThrow(id);
        assertDriverExists(request.driverId().trim());
        String vehicleNumber = VehicleMapper.normalizeVehicleNumber(request.vehicleNumber());
        // Rule: the plate may stay the same, but it must not belong to a DIFFERENT vehicle.
        if (vehicleRepository.existsByVehicleNumberAndIdNot(vehicleNumber, id)) {
            throw new DuplicateVehicleException(vehicleNumber);
        }
        vehicleMapper.applyUpdate(vehicle, request);
        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public void deleteVehicle(String id) {
        Vehicle vehicle = findVehicleOrThrow(id);
        vehicleRepository.delete(vehicle);

        // Rule: an AVAILABLE driver must always own a vehicle (see DriverServiceImpl.updateAvailability),
        // so if their last vehicle is removed the driver is switched to UNAVAILABLE automatically.
        if (vehicleRepository.countByDriverId(vehicle.getDriverId()) == 0) {
            driverRepository.findById(vehicle.getDriverId()).ifPresent(driver -> {
                if (driver.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE) {
                    driver.setAvailabilityStatus(AvailabilityStatus.UNAVAILABLE);
                    driver.setUpdatedAt(Instant.now());
                    driverRepository.save(driver);
                    log.info("Driver {} set UNAVAILABLE because their last vehicle was deleted", driver.getId());
                }
            });
        }
    }

    private Vehicle findVehicleOrThrow(String id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new VehicleNotFoundException(id));
    }

    private void assertDriverExists(String driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new DriverNotFoundException(driverId);
        }
    }
}
