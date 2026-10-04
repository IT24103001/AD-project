package com.ridelink.ridemanagement.repository;

import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the 'rides' collection in 'ridelink_ride_db'.
 */
@Repository
public interface RideRepository extends MongoRepository<Ride, String> {

    List<Ride> findByPassengerIdOrderByRequestedAtDesc(String passengerId);

    List<Ride> findByDriverIdOrderByRequestedAtDesc(String driverId);

    List<Ride> findByRideStatus(RideStatus rideStatus);
}
