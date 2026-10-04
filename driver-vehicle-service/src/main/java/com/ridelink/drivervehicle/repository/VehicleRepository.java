package com.ridelink.drivervehicle.repository;

import com.ridelink.drivervehicle.model.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VehicleRepository extends MongoRepository<Vehicle, String> {

    List<Vehicle> findByDriverId(String driverId);

    boolean existsByDriverId(String driverId);

    long countByDriverId(String driverId);

    boolean existsByVehicleNumber(String vehicleNumber);

    /** Used on update: another vehicle (different id) already has this number. */
    boolean existsByVehicleNumberAndIdNot(String vehicleNumber, String id);

    void deleteByDriverId(String driverId);
}
