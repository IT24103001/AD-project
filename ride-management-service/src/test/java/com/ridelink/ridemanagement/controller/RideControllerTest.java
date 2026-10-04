package com.ridelink.ridemanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.ridemanagement.config.SecurityConfig;
import com.ridelink.ridemanagement.dto.AcceptRideRequest;
import com.ridelink.ridemanagement.dto.CreateRideRequest;
import com.ridelink.ridemanagement.dto.RideResponse;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.security.JwtAuthenticationFilter;
import com.ridelink.ridemanagement.security.JwtTokenProvider;
import com.ridelink.ridemanagement.service.RideService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller & Role-Based Security unit tests verifying HTTP status codes,
 * Bean Validation (@Valid), and @PreAuthorize role enforcement.
 */
@WebMvcTest(RideController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideService rideService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(username = "PASS-1001", roles = {"PASSENGER"})
    @DisplayName("PASSENGER can create a valid ride request (201 Created)")
    void createRide_AsPassenger_Returns201Created() throws Exception {
        CreateRideRequest request = new CreateRideRequest(
                "PASS-1001",
                "SLIIT Malabe Campus",
                "Colombo Fort Station",
                false
        );

        RideResponse mockResponse = new RideResponse();
        mockResponse.setId("ride-1001");
        mockResponse.setPassengerId("PASS-1001");
        mockResponse.setPickupLocation("SLIIT Malabe Campus");
        mockResponse.setDestinationLocation("Colombo Fort Station");
        mockResponse.setRideStatus(RideStatus.REQUESTED);

        when(rideService.createRide(any(CreateRideRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ride-1001"))
                .andExpect(jsonPath("$.rideStatus").value("REQUESTED"));
    }

    @Test
    @WithMockUser(username = "PASS-1001", roles = {"PASSENGER"})
    @DisplayName("Blank pickup or destination returns 400 Bad Request with validationErrors")
    void createRide_BlankLocations_Returns400BadRequest() throws Exception {
        CreateRideRequest invalidRequest = new CreateRideRequest("PASS-1001", "", "", false);

        mockMvc.perform(post("/api/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.validationErrors.pickupLocation").exists())
                .andExpect(jsonPath("$.validationErrors.destinationLocation").exists());
    }

    @Test
    @WithMockUser(username = "PASS-1001", roles = {"PASSENGER"})
    @DisplayName("Unauthorized operation: PASSENGER attempting DRIVER-only endpoint (/accept) receives 403 Forbidden")
    void acceptRide_AsPassengerRole_Returns403Forbidden() throws Exception {
        AcceptRideRequest acceptRequest = new AcceptRideRequest("DRV-2001");

        mockMvc.perform(post("/api/rides/ride-1001/accept")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN_OPERATION"));
    }

    @Test
    @WithMockUser(username = "DRV-2001", roles = {"DRIVER"})
    @DisplayName("Unauthorized operation: DRIVER attempting ADMIN-only endpoint (GET /api/rides) receives 403 Forbidden")
    void getAllRides_AsDriverRole_Returns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/rides"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN_OPERATION"));
    }
}
