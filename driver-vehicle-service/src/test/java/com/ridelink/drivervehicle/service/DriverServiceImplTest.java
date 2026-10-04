package com.ridelink.drivervehicle.service;

import com.ridelink.drivervehicle.dto.*;
import com.ridelink.drivervehicle.exception.*;
import com.ridelink.drivervehicle.mapper.DriverMapper;
import com.ridelink.drivervehicle.model.AvailabilityStatus;
import com.ridelink.drivervehicle.model.Driver;
import com.ridelink.drivervehicle.model.Location;
import com.ridelink.drivervehicle.repository.DriverRepository;
import com.ridelink.drivervehicle.repository.VehicleRepository;
import com.ridelink.drivervehicle.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests: repositories are mocked, so no MongoDB is needed. */
@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    private static final double RADIUS_KM = 5.0;

    @Mock
    private DriverRepository driverRepository;
    @Mock
    private VehicleRepository vehicleRepository;

    private DriverServiceImpl driverService;

    @BeforeEach
    void setUp() {
        driverService = new DriverServiceImpl(driverRepository, vehicleRepository, new DriverMapper(), RADIUS_KM);
    }

    private Driver driver(String id, String area, AvailabilityStatus status, Location location) {
        Driver driver = new Driver();
        driver.setId(id);
        driver.setAccountId("acc-" + id);
        driver.setName("Driver " + id);
        driver.setPhone("0771234567");
        driver.setLicenseNumber("LIC-" + id);
        driver.setServiceArea(area);
        driver.setAvailabilityStatus(status);
        driver.setCurrentLocation(location);
        return driver;
    }

    // ---------- create ----------

    @Test
    void createDriver_success_startsUnavailable() {
        DriverCreateRequest request = new DriverCreateRequest("acc-1", "Nimal", "0771234567", "B123", "Colombo");
        when(driverRepository.existsByAccountId("acc-1")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("B123")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> {
            Driver saved = invocation.getArgument(0);
            saved.setId("d1");
            return saved;
        });

        DriverResponse response = driverService.createDriver(request);

        assertEquals("d1", response.id());
        assertEquals("acc-1", response.accountId());
        assertEquals(AvailabilityStatus.UNAVAILABLE, response.availabilityStatus());
        assertNotNull(response.createdAt());
    }

    @Test
    void createDriver_duplicateAccountId_throwsAndDoesNotSave() {
        DriverCreateRequest request = new DriverCreateRequest("acc-1", "Nimal", "0771234567", "B123", "Colombo");
        when(driverRepository.existsByAccountId("acc-1")).thenReturn(true);

        assertThrows(DuplicateDriverException.class, () -> driverService.createDriver(request));
        verify(driverRepository, never()).save(any());
    }

    @Test
    void createDriver_duplicateLicense_throwsAndDoesNotSave() {
        DriverCreateRequest request = new DriverCreateRequest("acc-1", "Nimal", "0771234567", "B123", "Colombo");
        when(driverRepository.existsByAccountId("acc-1")).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("B123")).thenReturn(true);

        assertThrows(DuplicateDriverException.class, () -> driverService.createDriver(request));
        verify(driverRepository, never()).save(any());
    }

    // ---------- read ----------

    @Test
    void getDriver_success() {
        when(driverRepository.findById("d1"))
                .thenReturn(Optional.of(driver("d1", "Colombo", AvailabilityStatus.UNAVAILABLE, null)));

        DriverResponse response = driverService.getDriverById("d1");

        assertEquals("d1", response.id());
        assertEquals("Colombo", response.serviceArea());
    }

    @Test
    void getDriver_notFound_throws() {
        when(driverRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(DriverNotFoundException.class, () -> driverService.getDriverById("missing"));
    }

    // ---------- update ----------

    @Test
    void updateDriver_success() {
        Driver existing = driver("d1", "Colombo", AvailabilityStatus.UNAVAILABLE, null);
        DriverUpdateRequest request = new DriverUpdateRequest("New Name", "0700000000", "LIC-NEW", "Galle");
        when(driverRepository.findById("d1")).thenReturn(Optional.of(existing));
        when(driverRepository.existsByLicenseNumberAndIdNot("LIC-NEW", "d1")).thenReturn(false);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = driverService.updateDriver("d1", request);

        assertEquals("New Name", response.name());
        assertEquals("LIC-NEW", response.licenseNumber());
        assertEquals("Galle", response.serviceArea());
    }

    @Test
    void updateDriver_licenseUsedByAnotherDriver_throwsConflict() {
        when(driverRepository.findById("d1"))
                .thenReturn(Optional.of(driver("d1", "Colombo", AvailabilityStatus.UNAVAILABLE, null)));
        when(driverRepository.existsByLicenseNumberAndIdNot("LIC-TAKEN", "d1")).thenReturn(true);

        DriverUpdateRequest request = new DriverUpdateRequest("Name", "0700000000", "LIC-TAKEN", "Colombo");
        assertThrows(DuplicateDriverException.class, () -> driverService.updateDriver("d1", request));
        verify(driverRepository, never()).save(any());
    }

    // ---------- delete ----------

    @Test
    void deleteDriver_success_alsoDeletesTheirVehicles() {
        when(driverRepository.existsById("d1")).thenReturn(true);

        driverService.deleteDriver("d1");

        verify(vehicleRepository).deleteByDriverId("d1");
        verify(driverRepository).deleteById("d1");
    }

    @Test
    void deleteDriver_notFound_throws() {
        when(driverRepository.existsById("missing")).thenReturn(false);

        assertThrows(DriverNotFoundException.class, () -> driverService.deleteDriver("missing"));
        verify(driverRepository, never()).deleteById(any());
    }

    // ---------- availability (business rules) ----------

    @Test
    void updateAvailability_toAvailable_succeedsWhenLocationAndVehicleExist() {
        Driver existing = driver("d1", "Colombo", AvailabilityStatus.UNAVAILABLE, new Location(6.9271, 79.8612));
        when(driverRepository.findById("d1")).thenReturn(Optional.of(existing));
        when(vehicleRepository.existsByDriverId("d1")).thenReturn(true);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = driverService.updateAvailability("d1", AvailabilityStatus.AVAILABLE);

        assertEquals(AvailabilityStatus.AVAILABLE, response.availabilityStatus());
    }

    @Test
    void updateAvailability_toAvailable_withoutLocation_isRejected() {
        when(driverRepository.findById("d1"))
                .thenReturn(Optional.of(driver("d1", "Colombo", AvailabilityStatus.UNAVAILABLE, null)));

        assertThrows(InvalidAvailabilityException.class,
                () -> driverService.updateAvailability("d1", AvailabilityStatus.AVAILABLE));
        verify(driverRepository, never()).save(any());
    }

    @Test
    void updateAvailability_toAvailable_withoutVehicle_isRejected() {
        when(driverRepository.findById("d1")).thenReturn(Optional.of(
                driver("d1", "Colombo", AvailabilityStatus.UNAVAILABLE, new Location(6.9271, 79.8612))));
        when(vehicleRepository.existsByDriverId("d1")).thenReturn(false);

        assertThrows(InvalidAvailabilityException.class,
                () -> driverService.updateAvailability("d1", AvailabilityStatus.AVAILABLE));
        verify(driverRepository, never()).save(any());
    }

    @Test
    void updateAvailability_toUnavailable_isAlwaysAllowed() {
        when(driverRepository.findById("d1"))
                .thenReturn(Optional.of(driver("d1", "Colombo", AvailabilityStatus.AVAILABLE, null)));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = driverService.updateAvailability("d1", AvailabilityStatus.UNAVAILABLE);

        assertEquals(AvailabilityStatus.UNAVAILABLE, response.availabilityStatus());
    }

    // ---------- location / service area ----------

    @Test
    void updateLocation_success() {
        when(driverRepository.findById("d1"))
                .thenReturn(Optional.of(driver("d1", "Colombo", AvailabilityStatus.UNAVAILABLE, null)));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = driverService.updateLocation("d1", new LocationUpdateRequest(6.9271, 79.8612));

        assertEquals(6.9271, response.currentLocation().latitude());
        assertEquals(79.8612, response.currentLocation().longitude());
    }

    @Test
    void updateLocation_driverNotFound_throws() {
        when(driverRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(DriverNotFoundException.class,
                () -> driverService.updateLocation("missing", new LocationUpdateRequest(6.9, 79.8)));
    }

    @Test
    void updateServiceArea_success_trimsValue() {
        when(driverRepository.findById("d1"))
                .thenReturn(Optional.of(driver("d1", "Colombo", AvailabilityStatus.UNAVAILABLE, null)));
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DriverResponse response = driverService.updateServiceArea("d1", new ServiceAreaUpdateRequest("  Kandy "));

        assertEquals("Kandy", response.serviceArea());
    }

    // ---------- eligible available drivers ----------

    @Test
    void findAvailableDrivers_byServiceArea_returnsDriversWithoutDistance() {
        Driver d1 = driver("d1", "Colombo", AvailabilityStatus.AVAILABLE, null);
        Driver d2 = driver("d2", "Colombo", AvailabilityStatus.AVAILABLE, null);
        when(driverRepository.findByAvailabilityStatusAndServiceAreaIgnoreCase(AvailabilityStatus.AVAILABLE, "Colombo"))
                .thenReturn(List.of(d1, d2));

        List<AvailableDriverResponse> result = driverService.findAvailableDrivers("Colombo", null, null);

        assertEquals(2, result.size());
        assertNull(result.get(0).distanceKm());
    }

    @Test
    void findAvailableDrivers_noFilters_returnsAllAvailable() {
        when(driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(driver("d1", "Colombo", AvailabilityStatus.AVAILABLE, null)));

        assertEquals(1, driverService.findAvailableDrivers(null, null, null).size());
    }

    @Test
    void findAvailableDrivers_withCoordinates_keepsOnlyDriversInsideRadius_nearestFirst() {
        // Request point = Colombo Fort
        Driver exactlyThere = driver("B", "Colombo", AvailabilityStatus.AVAILABLE, new Location(6.9271, 79.8612));
        Driver about2Km = driver("A", "Colombo", AvailabilityStatus.AVAILABLE, new Location(6.9344, 79.8428));
        Driver inKandy = driver("C", "Kandy", AvailabilityStatus.AVAILABLE, new Location(7.2906, 80.6337));
        Driver noLocation = driver("D", "Colombo", AvailabilityStatus.AVAILABLE, null);
        when(driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE))
                .thenReturn(List.of(about2Km, inKandy, noLocation, exactlyThere));

        List<AvailableDriverResponse> result = driverService.findAvailableDrivers(null, 6.9271, 79.8612);

        assertEquals(2, result.size());
        assertEquals("B", result.get(0).driver().id());
        assertEquals(0.0, result.get(0).distanceKm());
        assertEquals("A", result.get(1).driver().id());
        assertTrue(result.get(1).distanceKm() > 0 && result.get(1).distanceKm() <= RADIUS_KM);
    }

    @Test
    void findAvailableDrivers_serviceAreaAndCoordinates_appliesBothFilters() {
        Driver near = driver("A", "Colombo", AvailabilityStatus.AVAILABLE, new Location(6.9344, 79.8428));
        Driver far = driver("C", "Colombo", AvailabilityStatus.AVAILABLE, new Location(7.2906, 80.6337));
        when(driverRepository.findByAvailabilityStatusAndServiceAreaIgnoreCase(AvailabilityStatus.AVAILABLE, "Colombo"))
                .thenReturn(List.of(near, far));

        List<AvailableDriverResponse> result = driverService.findAvailableDrivers("Colombo", 6.9271, 79.8612);

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).driver().id());
    }

    @Test
    void findAvailableDrivers_onlyOneCoordinate_isInvalidInput() {
        assertThrows(InvalidSearchCriteriaException.class,
                () -> driverService.findAvailableDrivers("Colombo", 6.9271, null));
        verifyNoInteractions(driverRepository);
    }
}
