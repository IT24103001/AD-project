package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.dto.AcceptRideRequest;
import com.ridelink.ridemanagement.dto.AssignDriverRequest;
import com.ridelink.ridemanagement.dto.CancelRideRequest;
import com.ridelink.ridemanagement.dto.CreateRideRequest;
import com.ridelink.ridemanagement.dto.RideResponse;

import java.util.List;

/**
 * Business service contract for Ride Management operations.
 * Follows Dependency Inversion Principle (SOLID).
 */
public interface RideService {

    RideResponse createRide(CreateRideRequest request);

    RideResponse getRideById(String rideId);

    List<RideResponse> getAllRides();

    List<RideResponse> getRidesByPassengerId(String passengerId);

    List<RideResponse> getRidesByDriverId(String driverId);

    RideResponse assignDriver(String rideId, AssignDriverRequest request);

    RideResponse acceptRide(String rideId, AcceptRideRequest request);

    RideResponse startRide(String rideId);

    RideResponse completeRide(String rideId);

    RideResponse cancelRide(String rideId, CancelRideRequest request);
}
