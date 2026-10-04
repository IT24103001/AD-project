package com.ridelink.drivervehicle.repository;

import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/** Spring Data generates the queries from the method names - no SQL/Mongo query code needed. */
public interface DriverRepository extends MongoRepository<Driver, String> {

    boolean existsByAccountId(String accountId);

    boolean existsByLicenseNumber(String licenseNumber);

    /** Used on update: another driver (different id) already owns this licence number. */
    boolean existsByLicenseNumberAndIdNot(String licenseNumber, String id);

    List<Driver> findByAvailabilityStatus(AvailabilityStatus availabilityStatus);

    List<Driver> findByAvailabilityStatusAndServiceAreaIgnoreCase(AvailabilityStatus availabilityStatus, String serviceArea);
}
