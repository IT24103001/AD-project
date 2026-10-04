package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.dto.AvailableDriverDto;
import com.ridelink.ridemanagement.exception.DriverAssignmentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Synchronous REST client for communicating with the Driver & Vehicle Service.
 * Never accesses the Driver database directly; strictly uses REST API contracts.
 */
@Component
public class DriverServiceClient {

    private final RestTemplate restTemplate;
    private final String driverServiceBaseUrl;

    public DriverServiceClient(
            RestTemplate restTemplate,
            @Value("${ridelink.services.driver-service-url:http://localhost:8082}") String driverServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.driverServiceBaseUrl = driverServiceBaseUrl;
    }

    /**
     * Fetches available drivers from Driver & Vehicle Service via GET /api/drivers/available.
     */
    public List<AvailableDriverDto> fetchAvailableDrivers() {
        String url = driverServiceBaseUrl + "/api/drivers/available";
        try {
            ResponseEntity<List<AvailableDriverDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<AvailableDriverDto>>() {}
            );
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (RestClientException ex) {
            throw new DriverAssignmentException(
                    "Unable to retrieve available drivers from Driver & Vehicle Service at " + url, ex);
        }
    }

    /**
     * Deterministic Driver Selection Strategy:
     * 1. Request available drivers via GET /api/drivers/available
     * 2. Filter drivers where available == true and driverId is non-blank
     * 3. Sort deterministically by driverId ascending
     * 4. Select the first eligible driver (or throw DriverAssignmentException if none exist)
     */
    public AvailableDriverDto selectFirstEligibleDriver() {
        List<AvailableDriverDto> drivers = fetchAvailableDrivers();
        return drivers.stream()
                .filter(d -> d != null && d.isAvailable() && d.getDriverId() != null && !d.getDriverId().isBlank())
                .sorted(Comparator.comparing(AvailableDriverDto::getDriverId))
                .findFirst()
                .orElseThrow(() -> new DriverAssignmentException(
                        "No eligible available drivers found from Driver & Vehicle Service"));
    }
}
