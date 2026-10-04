package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.VehicleRequest;
import com.ridelink.drivervehicle.dto.VehicleResponse;
import com.ridelink.drivervehicle.exception.DriverNotFoundException;
import com.ridelink.drivervehicle.exception.DuplicateVehicleException;
import com.ridelink.drivervehicle.exception.VehicleNotFoundException;
import com.ridelink.drivervehicle.mapper.VehicleMapper;
import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.model.Vehicle;
import com.ridelink.drivervehicle.model.VehicleType;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import com.ridelink.drivervehicle.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private DriverRepository driverRepository;

    private VehicleServiceImpl vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository, driverRepository, new VehicleMapper());
    }

    private VehicleRequest request(String number) {
        return new VehicleRequest("d1", number, VehicleType.CAR, "Toyota", "Prius", "White");
    }

    private Vehicle vehicle(String id, String driverId, String number) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setDriverId(driverId);
        vehicle.setVehicleNumber(number);
        vehicle.setVehicleType(VehicleType.CAR);
        return vehicle;
    }

    @Test
    void createVehicle_success_normalizesVehicleNumber() {
        when(driverRepository.existsById("d1")).thenReturn(true);
        when(vehicleRepository.existsByVehicleNumber("CAB-1234")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle saved = invocation.getArgument(0);
            saved.setId("v1");
            return saved;
        });

        VehicleResponse response = vehicleService.createVehicle(request("  cab-1234 "));

        assertEquals("v1", response.id());
        assertEquals("CAB-1234", response.vehicleNumber());
        assertEquals("d1", response.driverId());
    }

    @Test
    void createVehicle_driverDoesNotExist_throws() {
        when(driverRepository.existsById("d1")).thenReturn(false);

        assertThrows(DriverNotFoundException.class, () -> vehicleService.createVehicle(request("CAB-1234")));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void createVehicle_duplicateNumber_throwsConflict() {
        when(driverRepository.existsById("d1")).thenReturn(true);
        when(vehicleRepository.existsByVehicleNumber("CAB-1234")).thenReturn(true);

        assertThrows(DuplicateVehicleException.class, () -> vehicleService.createVehicle(request("CAB-1234")));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void getVehicle_notFound_throws() {
        when(vehicleRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class, () -> vehicleService.getVehicleById("missing"));
    }

    @Test
    void getVehiclesByDriver_returnsOnlyThatDriversVehicles() {
        when(driverRepository.existsById("d1")).thenReturn(true);
        when(vehicleRepository.findByDriverId("d1")).thenReturn(List.of(vehicle("v1", "d1", "CAB-1")));

        List<VehicleResponse> result = vehicleService.getVehiclesByDriverId("d1");

        assertEquals(1, result.size());
        assertEquals("v1", result.get(0).id());
    }

    @Test
    void getVehiclesByDriver_unknownDriver_throws() {
        when(driverRepository.existsById("missing")).thenReturn(false);

        assertThrows(DriverNotFoundException.class, () -> vehicleService.getVehiclesByDriverId("missing"));
    }

    @Test
    void updateVehicle_success() {
        when(vehicleRepository.findById("v1")).thenReturn(Optional.of(vehicle("v1", "d1", "CAB-1")));
        when(driverRepository.existsById("d1")).thenReturn(true);
        when(vehicleRepository.existsByVehicleNumberAndIdNot("CAB-9999", "v1")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleResponse response = vehicleService.updateVehicle("v1", request("CAB-9999"));

        assertEquals("CAB-9999", response.vehicleNumber());
    }

    @Test
    void updateVehicle_numberUsedByAnotherVehicle_throwsConflict() {
        when(vehicleRepository.findById("v1")).thenReturn(Optional.of(vehicle("v1", "d1", "CAB-1")));
        when(driverRepository.existsById("d1")).thenReturn(true);
        when(vehicleRepository.existsByVehicleNumberAndIdNot("CAB-TAKEN", "v1")).thenReturn(true);

        assertThrows(DuplicateVehicleException.class, () -> vehicleService.updateVehicle("v1", request("CAB-TAKEN")));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void deleteVehicle_notFound_throws() {
        when(vehicleRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(VehicleNotFoundException.class, () -> vehicleService.deleteVehicle("missing"));
    }

    @Test
    void deleteVehicle_lastVehicleOfAvailableDriver_makesDriverUnavailable() {
        Vehicle existing = vehicle("v1", "d1", "CAB-1");
        Driver availableDriver = new Driver();
        availableDriver.setId("d1");
        availableDriver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        when(vehicleRepository.findById("v1")).thenReturn(Optional.of(existing));
        when(vehicleRepository.countByDriverId("d1")).thenReturn(0L);
        when(driverRepository.findById("d1")).thenReturn(Optional.of(availableDriver));

        vehicleService.deleteVehicle("v1");

        verify(vehicleRepository).delete(existing);
        ArgumentCaptor<Driver> saved = ArgumentCaptor.forClass(Driver.class);
        verify(driverRepository).save(saved.capture());
        assertEquals(AvailabilityStatus.UNAVAILABLE, saved.getValue().getAvailabilityStatus());
    }

    @Test
    void deleteVehicle_whenDriverStillHasOtherVehicles_leavesDriverUntouched() {
        Vehicle existing = vehicle("v1", "d1", "CAB-1");
        when(vehicleRepository.findById("v1")).thenReturn(Optional.of(existing));
        when(vehicleRepository.countByDriverId("d1")).thenReturn(1L);

        vehicleService.deleteVehicle("v1");

        verify(vehicleRepository).delete(existing);
        verifyNoInteractions(driverRepository);
    }
}
