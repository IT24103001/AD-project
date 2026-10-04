package com.ridelink.drivervehicle.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the Bean Validation rules on the request DTOs (the "invalid input" scenarios). */
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static <T> Set<String> invalidFields(T dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void validDriverRequest_hasNoViolations() {
        DriverCreateRequest request = new DriverCreateRequest("acc-1", "Nimal", "0771234567", "B123", "Colombo");
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void blankDriverFields_areRejected() {
        DriverCreateRequest request = new DriverCreateRequest("", " ", "", "", "");
        assertEquals(Set.of("accountId", "name", "phone", "licenseNumber", "serviceArea"), invalidFields(request));
    }

    @Test
    void latitudeAndLongitudeOutOfRange_areRejected() {
        assertEquals(Set.of("latitude", "longitude"), invalidFields(new LocationUpdateRequest(95.0, 200.0)));
    }

    @Test
    void validCoordinates_areAccepted() {
        assertTrue(validator.validate(new LocationUpdateRequest(6.9271, 79.8612)).isEmpty());
    }

    @Test
    void missingVehicleTypeAndBlankNumber_areRejected() {
        VehicleRequest request = new VehicleRequest("d1", " ", null, "Toyota", "Prius", "White");
        assertEquals(Set.of("vehicleNumber", "vehicleType"), invalidFields(request));
    }

    @Test
    void missingAvailabilityStatus_isRejected() {
        assertEquals(Set.of("availabilityStatus"), invalidFields(new AvailabilityUpdateRequest(null)));
    }
}
