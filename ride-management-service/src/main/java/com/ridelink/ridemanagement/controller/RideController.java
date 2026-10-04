package com.ridelink.ridemanagement.controller;

import com.ridelink.ridemanagement.dto.AcceptRideRequest;
import com.ridelink.ridemanagement.dto.AssignDriverRequest;
import com.ridelink.ridemanagement.dto.CancelRideRequest;
import com.ridelink.ridemanagement.dto.CreateRideRequest;
import com.ridelink.ridemanagement.dto.RideResponse;
import com.ridelink.ridemanagement.exception.ErrorResponse;
import com.ridelink.ridemanagement.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller exposing all 10 Ride Management Service endpoints.
 * Documented with OpenAPI 3 / Swagger annotations and secured with Role-Based Access Control.
 */
@RestController
@RequestMapping("/api/rides")
@Tag(name = "Ride Management", description = "Endpoints for ride request creation, driver assignment, lifecycle transitions, and ride retrieval")
@SecurityRequirement(name = "bearerAuth")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    @Operation(
            summary = "Create a new ride request",
            description = "Creates a ride in REQUESTED state (or immediately assigns an available driver from Driver & Vehicle Service if autoAssignDriver=true). Requires PASSENGER or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ride created successfully",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or identical pickup and destination",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Caller does not have PASSENGER or ADMIN role"),
            @ApiResponse(responseCode = "409", description = "No available driver found when autoAssignDriver=true",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
        RideResponse created = rideService.createRide(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN')")
    @Operation(
            summary = "Retrieve a ride by ID",
            description = "Fetches full ride lifecycle timestamps, locations, assigned driver, and fare details by ride ID. Requires PASSENGER, DRIVER, or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RideResponse> getRideById(
            @Parameter(description = "Unique ride identifier", example = "665f1a2b3c4d5e6f7a8b9c0d")
            @PathVariable("id") String id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Retrieve all rides",
            description = "Administrative endpoint to list all rides in ridelink_ride_db. Requires ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of all rides retrieved"),
            @ApiResponse(responseCode = "403", description = "Requires ADMIN role")
    })
    public ResponseEntity<List<RideResponse>> getAllRides() {
        return ResponseEntity.ok(rideService.getAllRides());
    }

    @GetMapping("/passenger/{passengerId}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    @Operation(
            summary = "Retrieve rides for a specific passenger",
            description = "Returns all rides requested by the given passengerId ordered by requestedAt descending. Requires PASSENGER or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passenger ride history retrieved"),
            @ApiResponse(responseCode = "403", description = "Requires PASSENGER or ADMIN role")
    })
    public ResponseEntity<List<RideResponse>> getRidesByPassenger(
            @Parameter(description = "Stable passenger identifier", example = "PASS-1001")
            @PathVariable("passengerId") String passengerId) {
        return ResponseEntity.ok(rideService.getRidesByPassengerId(passengerId));
    }

    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(
            summary = "Retrieve rides for a specific driver",
            description = "Returns all rides assigned to the given driverId ordered by requestedAt descending. Requires DRIVER or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver ride history retrieved"),
            @ApiResponse(responseCode = "403", description = "Requires DRIVER or ADMIN role")
    })
    public ResponseEntity<List<RideResponse>> getRidesByDriver(
            @Parameter(description = "Stable driver identifier", example = "DRV-2001")
            @PathVariable("driverId") String driverId) {
        return ResponseEntity.ok(rideService.getRidesByDriverId(driverId));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('PASSENGER', 'ADMIN')")
    @Operation(
            summary = "Assign an eligible available driver to a requested ride",
            description = "Transitions ride from REQUESTED -> ASSIGNED. Queries Driver & Vehicle Service (GET /api/drivers/available) and selects an eligible driver deterministically. Requires PASSENGER or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver assigned and status updated to ASSIGNED",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid ride status transition (must be REQUESTED)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "No eligible available drivers in Driver & Vehicle Service",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RideResponse> assignDriver(
            @PathVariable("id") String id,
            @RequestBody(required = false) AssignDriverRequest request) {
        return ResponseEntity.ok(rideService.assignDriver(id, request));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(
            summary = "Accept an assigned ride",
            description = "Transitions ride from ASSIGNED -> ACCEPTED. Validates that the accepting driverId matches the assigned driverId. Requires DRIVER or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride accepted and status updated to ACCEPTED",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition or mismatched driverId",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requires DRIVER or ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<RideResponse> acceptRide(
            @PathVariable("id") String id,
            @Valid @RequestBody AcceptRideRequest request) {
        return ResponseEntity.ok(rideService.acceptRide(id, request));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(
            summary = "Start an accepted ride",
            description = "Transitions ride from ACCEPTED -> IN_PROGRESS and records startedAt timestamp. Requires DRIVER or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride started and status updated to IN_PROGRESS",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition (e.g., COMPLETED -> IN_PROGRESS is rejected)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requires DRIVER or ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<RideResponse> startRide(@PathVariable("id") String id) {
        return ResponseEntity.ok(rideService.startRide(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    @Operation(
            summary = "Complete an in-progress ride and calculate final fare",
            description = "Transitions ride from IN_PROGRESS -> COMPLETED, records completedAt, and synchronously calls Fare & Payment Service (POST /api/fares/calculate) to record final fare and payment status. Requires DRIVER or ADMIN role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride completed and final fare calculated",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition (must be IN_PROGRESS)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Requires DRIVER or ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<RideResponse> completeRide(@PathVariable("id") String id) {
        return ResponseEntity.ok(rideService.completeRide(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER', 'ADMIN')")
    @Operation(
            summary = "Cancel a ride",
            description = "Transitions ride to CANCELLED only if current status is REQUESTED, ASSIGNED, or ACCEPTED. Cancellation is rejected if IN_PROGRESS, COMPLETED, or already CANCELLED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride cancelled successfully",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition (cannot cancel IN_PROGRESS or COMPLETED ride) or blank reason",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<RideResponse> cancelRide(
            @PathVariable("id") String id,
            @Valid @RequestBody CancelRideRequest request) {
        return ResponseEntity.ok(rideService.cancelRide(id, request));
    }
}
