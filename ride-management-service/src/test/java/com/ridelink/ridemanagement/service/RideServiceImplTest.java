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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RideServiceImpl using JUnit 5 and Mockito.
 * Covers all 12 required positive and negative scenarios.
 */
@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverServiceClient driverServiceClient;

    @Mock
    private FareServiceClient fareServiceClient;

    @InjectMocks
    private RideServiceImpl rideService;

    private Ride sampleRide;

    @BeforeEach
    void setUp() {
        sampleRide = new Ride();
        sampleRide.setId("ride-1001");
        sampleRide.setPassengerId("PASS-1001");
        sampleRide.setPickupLocation("SLIIT Malabe Campus");
        sampleRide.setDestinationLocation("Colombo Fort Station");
        sampleRide.setRideStatus(RideStatus.REQUESTED);
        sampleRide.setRequestedAt(Instant.now());
    }

    @Test
    @DisplayName("1. Create ride: valid request creates ride with REQUESTED status")
    void createRide_ValidRequest_ReturnsRequestedRide() {
        CreateRideRequest request = new CreateRideRequest(
                "PASS-1001",
                "SLIIT Malabe Campus",
                "Colombo Fort Station",
                false
        );

        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride saved = invocation.getArgument(0);
            saved.setId("ride-1001");
            return saved;
        });

        RideResponse response = rideService.createRide(request);

        assertNotNull(response);
        assertEquals("ride-1001", response.getId());
        assertEquals("PASS-1001", response.getPassengerId());
        assertEquals(RideStatus.REQUESTED, response.getRideStatus());
        assertNotNull(response.getRequestedAt());
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    @DisplayName("2. Invalid ride request: identical pickup and destination throws InvalidRideRequestException")
    void createRide_IdenticalPickupAndDestination_ThrowsInvalidRideRequestException() {
        CreateRideRequest request = new CreateRideRequest(
                "PASS-1001",
                "SLIIT Malabe Campus",
                "SLIIT Malabe Campus",
                false
        );

        assertThrows(InvalidRideRequestException.class, () -> rideService.createRide(request));
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    @DisplayName("3. Get ride: existing ride ID returns RideResponse")
    void getRideById_ExistingId_ReturnsRideResponse() {
        when(rideRepository.findById("ride-1001")).thenReturn(Optional.of(sampleRide));

        RideResponse response = rideService.getRideById("ride-1001");

        assertNotNull(response);
        assertEquals("ride-1001", response.getId());
        assertEquals("PASS-1001", response.getPassengerId());
    }

    @Test
    @DisplayName("4. Ride not found: non-existent ride ID throws RideNotFoundException")
    void getRideById_NonExistentId_ThrowsRideNotFoundException() {
        when(rideRepository.findById("ride-9999")).thenReturn(Optional.empty());

        assertThrows(RideNotFoundException.class, () -> rideService.getRideById("ride-9999"));
    }

    @Test
    @DisplayName("5. Driver assignment: queries Driver Service, selects eligible driver deterministically, and updates status to ASSIGNED")
    void assignDriver_AvailableDriverExists_UpdatesStatusToAssigned() {
        AvailableDriverDto eligibleDriver = new AvailableDriverDto(
                "DRV-2001",
                "Kasun Perera",
                "CAB-4582",
                "SEDAN",
                true,
                "Malabe",
                "Malabe Junction"
        );

        when(rideRepository.findById("ride-1001")).thenReturn(Optional.of(sampleRide));
        when(driverServiceClient.selectFirstEligibleDriver()).thenReturn(eligibleDriver);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.assignDriver("ride-1001", new AssignDriverRequest(null));

        assertEquals(RideStatus.ASSIGNED, response.getRideStatus());
        assertEquals("DRV-2001", response.getDriverId());
        assertEquals("CAB-4582", response.getVehicleNumber());
        assertNotNull(response.getAssignedAt());
    }

    @Test
    @DisplayName("6. No available driver: throws DriverAssignmentException and does not transition ride status")
    void assignDriver_NoAvailableDriver_ThrowsDriverAssignmentException() {
        when(rideRepository.findById("ride-1001")).thenReturn(Optional.of(sampleRide));
        when(driverServiceClient.selectFirstEligibleDriver())
                .thenThrow(new DriverAssignmentException("No eligible available drivers found from Driver & Vehicle Service"));

        assertThrows(DriverAssignmentException.class,
                () -> rideService.assignDriver("ride-1001", new AssignDriverRequest(null)));

        assertEquals(RideStatus.REQUESTED, sampleRide.getRideStatus());
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    @DisplayName("7. Accept ride: assigned driver accepts ride and updates status to ACCEPTED")
    void acceptRide_ValidAssignedDriver_UpdatesStatusToAccepted() {
        sampleRide.setRideStatus(RideStatus.ASSIGNED);
        sampleRide.setDriverId("DRV-2001");

        when(rideRepository.findById("ride-1001")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.acceptRide("ride-1001", new AcceptRideRequest("DRV-2001"));

        assertEquals(RideStatus.ACCEPTED, response.getRideStatus());
        assertNotNull(response.getAcceptedAt());
    }

    @Test
    @DisplayName("8. Start ride: accepted ride transitions to IN_PROGRESS")
    void startRide_AcceptedRide_UpdatesStatusToInProgress() {
        sampleRide.setRideStatus(RideStatus.ACCEPTED);
        sampleRide.setDriverId("DRV-2001");

        when(rideRepository.findById("ride-1001")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.startRide("ride-1001");

        assertEquals(RideStatus.IN_PROGRESS, response.getRideStatus());
        assertNotNull(response.getStartedAt());
    }

    @Test
    @DisplayName("9. Complete ride: in-progress ride transitions to COMPLETED and calls Fare & Payment Service")
    void completeRide_InProgressRide_CallsFareServiceAndUpdatesStatusToCompleted() {
        sampleRide.setRideStatus(RideStatus.IN_PROGRESS);
        sampleRide.setDriverId("DRV-2001");
        sampleRide.setStartedAt(Instant.now().minusSeconds(900));

        FareCalculationResponse fareCalculationResponse = new FareCalculationResponse(
                "FARE-9001",
                "ride-1001",
                1450.00,
                "LKR",
                "PAID"
        );

        when(rideRepository.findById("ride-1001")).thenReturn(Optional.of(sampleRide));
        when(fareServiceClient.calculateFinalFare(any(FareCalculationRequest.class)))
                .thenReturn(fareCalculationResponse);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.completeRide("ride-1001");

        assertEquals(RideStatus.COMPLETED, response.getRideStatus());
        assertEquals(1450.00, response.getFinalFare());
        assertEquals("FARE-9001", response.getFareId());
        assertEquals("PAID", response.getPaymentStatus());
        assertNotNull(response.getCompletedAt());
        verify(fareServiceClient).calculateFinalFare(any(FareCalculationRequest.class));
    }

    @Test
    @DisplayName("10. Cancel ride: ride in REQUESTED or ASSIGNED state transitions to CANCELLED")
    void cancelRide_RequestedRide_UpdatesStatusToCancelled() {
        sampleRide.setRideStatus(RideStatus.REQUESTED);

        when(rideRepository.findById("ride-1001")).thenReturn(Optional.of(sampleRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RideResponse response = rideService.cancelRide("ride-1001", new CancelRideRequest("Passenger changed plans"));

        assertEquals(RideStatus.CANCELLED, response.getRideStatus());
        assertEquals("Passenger changed plans", response.getCancellationReason());
        assertNotNull(response.getCancelledAt());
    }

    @Test
    @DisplayName("11. Invalid status transition: COMPLETED -> IN_PROGRESS throws InvalidRideStatusException")
    void startRide_FromCompletedStatus_ThrowsInvalidRideStatusException() {
        sampleRide.setRideStatus(RideStatus.COMPLETED);

        when(rideRepository.findById("ride-1001")).thenReturn(Optional.of(sampleRide));

        InvalidRideStatusException ex = assertThrows(
                InvalidRideStatusException.class,
                () -> rideService.startRide("ride-1001")
        );

        assertTrue(ex.getMessage().contains("COMPLETED"));
        assertTrue(ex.getMessage().contains("IN_PROGRESS"));
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    @DisplayName("12. Unauthorized/Unassigned driver operation: different driver attempting to accept ride fails")
    void acceptRide_UnassignedDriver_ThrowsInvalidRideRequestException() {
        sampleRide.setRideStatus(RideStatus.ASSIGNED);
        sampleRide.setDriverId("DRV-2001");

        when(rideRepository.findById("ride-1001")).thenReturn(Optional.of(sampleRide));

        assertThrows(
                InvalidRideRequestException.class,
                () -> rideService.acceptRide("ride-1001", new AcceptRideRequest("DRV-9999"))
        );
        verify(rideRepository, never()).save(any(Ride.class));
    }
}
