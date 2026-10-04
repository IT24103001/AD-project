package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.client.DriverServiceClient;
import com.ridelink.ridemanagement.client.FareServiceClient;
import com.ridelink.ridemanagement.dto.AcceptRideRequest;
import com.ridelink.ridemanagement.dto.AssignDriverRequest;
import com.ridelink.ridemanagement.dto.AvailableDriverDto;
import com.ridelink.ridemanagement.dto.CancelRideRequest;
import com.ridelink.ridemanagement.dto.CreateRideRequest;
import com.ridelink.ridemanagement.dto.FareCalculationRequest;
import com.ridelink.ridemanagement.dto.FareCalculationResponse;
import com.ridelink.ridemanagement.dto.RideResponse;
import com.ridelink.ridemanagement.exception.DriverAssignmentException;
import com.ridelink.ridemanagement.exception.InvalidRideRequestException;
import com.ridelink.ridemanagement.exception.InvalidRideStatusException;
import com.ridelink.ridemanagement.exception.RideNotFoundException;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core implementation of Ride Management business workflows, state machine transitions,
 * and interservice REST orchestration with Driver & Vehicle Service and Fare & Payment Service.
 */
@Service
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;
    private final FareServiceClient fareServiceClient;

    public RideServiceImpl(
            RideRepository rideRepository,
            DriverServiceClient driverServiceClient,
            FareServiceClient fareServiceClient) {
        this.rideRepository = rideRepository;
        this.driverServiceClient = driverServiceClient;
        this.fareServiceClient = fareServiceClient;
    }

    @Override
    public RideResponse createRide(CreateRideRequest request) {
        validateCreateRideRequest(request);

        Instant now = Instant.now();
        Ride ride = new Ride();
        ride.setPassengerId(request.getPassengerId().trim());
        ride.setPickupLocation(request.getPickupLocation().trim());
        ride.setDestinationLocation(request.getDestinationLocation().trim());
        ride.setRideStatus(RideStatus.REQUESTED);
        ride.setRequestedAt(now);
        ride.setCreatedAt(now);
        ride.setUpdatedAt(now);

        Ride savedRide = rideRepository.save(ride);

        // If autoAssignDriver is requested at creation time, synchronously query Driver Service
        // and transition REQUESTED -> ASSIGNED using the deterministic selection strategy.
        if (request.isAutoAssignDriver()) {
            return assignDriver(savedRide.getId(), new AssignDriverRequest(null));
        }

        return RideResponse.fromEntity(savedRide);
    }

    @Override
    public RideResponse getRideById(String rideId) {
        Ride ride = findRideOrThrow(rideId);
        return RideResponse.fromEntity(ride);
    }

    @Override
    public List<RideResponse> getAllRides() {
        return rideRepository.findAll()
                .stream()
                .map(RideResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RideResponse> getRidesByPassengerId(String passengerId) {
        if (passengerId == null || passengerId.isBlank()) {
            throw new InvalidRideRequestException("passengerId cannot be blank");
        }
        return rideRepository.findByPassengerIdOrderByRequestedAtDesc(passengerId.trim())
                .stream()
                .map(RideResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<RideResponse> getRidesByDriverId(String driverId) {
        if (driverId == null || driverId.isBlank()) {
            throw new InvalidRideRequestException("driverId cannot be blank");
        }
        return rideRepository.findByDriverIdOrderByRequestedAtDesc(driverId.trim())
                .stream()
                .map(RideResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public RideResponse assignDriver(String rideId, AssignDriverRequest request) {
        Ride ride = findRideOrThrow(rideId);
        validateStateTransition(ride, RideStatus.ASSIGNED);

        String requestedDriverId = (request != null && request.getDriverId() != null && !request.getDriverId().isBlank())
                ? request.getDriverId().trim()
                : null;

        if (requestedDriverId != null) {
            // Verify the specified driver is among eligible available drivers from Driver Service
            List<AvailableDriverDto> availableDrivers = driverServiceClient.fetchAvailableDrivers();
            AvailableDriverDto matchedDriver = availableDrivers.stream()
                    .filter(d -> d != null && d.isAvailable() && requestedDriverId.equals(d.getDriverId()))
                    .findFirst()
                    .orElseThrow(() -> new DriverAssignmentException(
                            "Requested driver '" + requestedDriverId + "' is not currently available in Driver & Vehicle Service"));

            ride.setDriverId(matchedDriver.getDriverId());
            ride.setVehicleNumber(matchedDriver.getVehicleNumber());
        } else {
            // Deterministic selection strategy via Driver & Vehicle Service (GET /api/drivers/available)
            AvailableDriverDto selectedDriver = driverServiceClient.selectFirstEligibleDriver();
            ride.setDriverId(selectedDriver.getDriverId());
            ride.setVehicleNumber(selectedDriver.getVehicleNumber());
        }

        Instant now = Instant.now();
        ride.setRideStatus(RideStatus.ASSIGNED);
        ride.setAssignedAt(now);
        ride.setUpdatedAt(now);

        Ride updatedRide = rideRepository.save(ride);
        return RideResponse.fromEntity(updatedRide);
    }

    @Override
    public RideResponse acceptRide(String rideId, AcceptRideRequest request) {
        if (request == null || request.getDriverId() == null || request.getDriverId().isBlank()) {
            throw new InvalidRideRequestException("driverId is required to accept a ride");
        }

        Ride ride = findRideOrThrow(rideId);
        validateStateTransition(ride, RideStatus.ACCEPTED);

        String acceptingDriverId = request.getDriverId().trim();
        if (ride.getDriverId() == null || !ride.getDriverId().equals(acceptingDriverId)) {
            throw new InvalidRideRequestException(
                    "Driver '" + acceptingDriverId + "' is not the assigned driver ('" + ride.getDriverId() + "') for this ride");
        }

        Instant now = Instant.now();
        ride.setRideStatus(RideStatus.ACCEPTED);
        ride.setAcceptedAt(now);
        ride.setUpdatedAt(now);

        Ride updatedRide = rideRepository.save(ride);
        return RideResponse.fromEntity(updatedRide);
    }

    @Override
    public RideResponse startRide(String rideId) {
        Ride ride = findRideOrThrow(rideId);
        validateStateTransition(ride, RideStatus.IN_PROGRESS);

        Instant now = Instant.now();
        ride.setRideStatus(RideStatus.IN_PROGRESS);
        ride.setStartedAt(now);
        ride.setUpdatedAt(now);

        Ride updatedRide = rideRepository.save(ride);
        return RideResponse.fromEntity(updatedRide);
    }

    @Override
    public RideResponse completeRide(String rideId) {
        Ride ride = findRideOrThrow(rideId);
        validateStateTransition(ride, RideStatus.COMPLETED);

        Instant now = Instant.now();
        ride.setRideStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(now);
        ride.setUpdatedAt(now);

        // Interact with Fare & Payment Service via REST to obtain final fare & support payment workflow
        FareCalculationRequest fareRequest = new FareCalculationRequest(
                ride.getId(),
                ride.getPassengerId(),
                ride.getDriverId(),
                ride.getPickupLocation(),
                ride.getDestinationLocation(),
                ride.getStartedAt(),
                now
        );

        FareCalculationResponse fareResponse = fareServiceClient.calculateFinalFare(fareRequest);
        if (fareResponse != null) {
            ride.setFinalFare(fareResponse.getFinalFare());
            ride.setFareId(fareResponse.getFareId());
            ride.setPaymentStatus(fareResponse.getPaymentStatus());
        }

        Ride updatedRide = rideRepository.save(ride);
        return RideResponse.fromEntity(updatedRide);
    }

    @Override
    public RideResponse cancelRide(String rideId, CancelRideRequest request) {
        if (request == null || request.getReason() == null || request.getReason().isBlank()) {
            throw new InvalidRideRequestException("Cancellation reason is required and cannot be blank");
        }

        Ride ride = findRideOrThrow(rideId);
        validateStateTransition(ride, RideStatus.CANCELLED);

        Instant now = Instant.now();
        ride.setRideStatus(RideStatus.CANCELLED);
        ride.setCancellationReason(request.getReason().trim());
        ride.setCancelledAt(now);
        ride.setUpdatedAt(now);

        Ride updatedRide = rideRepository.save(ride);
        return RideResponse.fromEntity(updatedRide);
    }

    private Ride findRideOrThrow(String rideId) {
        if (rideId == null || rideId.isBlank()) {
            throw new InvalidRideRequestException("rideId cannot be blank");
        }
        return rideRepository.findById(rideId.trim())
                .orElseThrow(() -> new RideNotFoundException("Ride not found with id: " + rideId));
    }

    private void validateStateTransition(Ride ride, RideStatus targetStatus) {
        RideStatus currentStatus = ride.getRideStatus();
        if (currentStatus == null || !currentStatus.canTransitionTo(targetStatus)) {
            throw new InvalidRideStatusException(currentStatus, targetStatus);
        }
    }

    private void validateCreateRideRequest(CreateRideRequest request) {
        if (request == null) {
            throw new InvalidRideRequestException("Ride creation request body cannot be null");
        }
        if (request.getPassengerId() == null || request.getPassengerId().isBlank()) {
            throw new InvalidRideRequestException("passengerId is required and cannot be blank");
        }
        if (request.getPickupLocation() == null || request.getPickupLocation().isBlank()) {
            throw new InvalidRideRequestException("pickupLocation is required and cannot be blank");
        }
        if (request.getDestinationLocation() == null || request.getDestinationLocation().isBlank()) {
            throw new InvalidRideRequestException("destinationLocation is required and cannot be blank");
        }
        if (request.getPickupLocation().trim().equalsIgnoreCase(request.getDestinationLocation().trim())) {
            throw new InvalidRideRequestException("pickupLocation and destinationLocation cannot be identical");
        }
    }
}
