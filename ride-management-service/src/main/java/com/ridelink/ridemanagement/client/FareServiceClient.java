package com.ridelink.ridemanagement.client;

import com.ridelink.ridemanagement.dto.FareCalculationRequest;
import com.ridelink.ridemanagement.dto.FareCalculationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Synchronous REST client for communicating with the Fare & Payment Service.
 * Invoked after ride completion to calculate final fare and trigger payment workflow.
 * Never accesses the Fare database directly.
 */
@Component
public class FareServiceClient {

    private static final Logger log = LoggerFactory.getLogger(FareServiceClient.class);

    private final RestTemplate restTemplate;
    private final String fareServiceBaseUrl;

    public FareServiceClient(
            RestTemplate restTemplate,
            @Value("${ridelink.services.fare-service-url:http://localhost:8084}") String fareServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.fareServiceBaseUrl = fareServiceBaseUrl;
    }

    /**
     * Calls POST /api/fares/calculate on Fare & Payment Service to obtain the final fare
     * and initiate simulated payment recording for a completed ride.
     */
    public FareCalculationResponse calculateFinalFare(FareCalculationRequest request) {
        String url = fareServiceBaseUrl + "/api/fares/calculate";
        try {
            ResponseEntity<FareCalculationResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    FareCalculationResponse.class
            );
            return response.getBody();
        } catch (RestClientException ex) {
            log.warn("Fare & Payment Service communication failed at {}: {}", url, ex.getMessage());
            throw ex;
        }
    }
}
