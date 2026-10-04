package com.ridelink.drivervehicle.controller;

import com.ridelink.drivervehicle.dto.*;
import com.ridelink.drivervehicle.exception.ErrorResponse;
import com.ridelink.drivervehicle.service.DriverService;
import com.ridelink.drivervehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST layer only: it receives the HTTP request, calls the service and returns the HTTP response.
 * No business logic here. @PreAuthorize is only active when SECURITY_ENABLED=true.
 */
@RestController
@RequestMapping("/api/drivers")
@Validated
@Tag(name = "Drivers", description = "Driver profile, availability, location, service area and eligible-driver search")
public class DriverController {

    private final DriverService driverService;
    private final VehicleService vehicleService;

    public DriverController(DriverService driverService, VehicleService vehicleService) {
        this.driverService = driverService;
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Create driver", description = "POST /api/drivers - creates a driver profile. New drivers start UNAVAILABLE.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Driver created"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g. blank name)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "accountId or licenseNumber already registered",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody DriverCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.createDriver(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Get driver by id", description = "GET /api/drivers/{id}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver found"),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public DriverResponse getDriver(@Parameter(description = "Driver id", example = "665f1c2e9a1b2c3d4e5f6a7b")
                                    @PathVariable String id) {
        return driverService.getDriverById(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all drivers", description = "GET /api/drivers")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "List of drivers (may be empty)"))
    public List<DriverResponse> getAllDrivers() {
        return driverService.getAllDrivers();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Update driver profile", description = "PUT /api/drivers/{id} - full update of name, phone, licenseNumber and serviceArea.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver updated"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Licence number belongs to another driver",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public DriverResponse updateDriver(@PathVariable String id, @Valid @RequestBody DriverUpdateRequest request) {
        return driverService.updateDriver(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete driver", description = "DELETE /api/drivers/{id} - also deletes the driver's vehicles.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Driver deleted"),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteDriver(@PathVariable String id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Update availability",
            description = "PATCH /api/drivers/{id}/availability - body {\"availabilityStatus\":\"AVAILABLE\"}. "
                    + "To become AVAILABLE the driver needs a current location and at least one vehicle.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability updated"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid availabilityStatus",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Driver has no location or no vehicle (INVALID_AVAILABILITY)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public DriverResponse updateAvailability(@PathVariable String id, @Valid @RequestBody AvailabilityUpdateRequest request) {
        return driverService.updateAvailability(id, request.availabilityStatus());
    }

    @PatchMapping("/{id}/location")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Update simulated location", description = "PATCH /api/drivers/{id}/location - body {\"latitude\":6.9271,\"longitude\":79.8612}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location updated"),
            @ApiResponse(responseCode = "400", description = "Latitude/longitude missing or out of range",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public DriverResponse updateLocation(@PathVariable String id, @Valid @RequestBody LocationUpdateRequest request) {
        return driverService.updateLocation(id, request);
    }

    @PatchMapping("/{id}/service-area")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Update service area", description = "PATCH /api/drivers/{id}/service-area - body {\"serviceArea\":\"Colombo\"}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service area updated"),
            @ApiResponse(responseCode = "400", description = "serviceArea is blank",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public DriverResponse updateServiceArea(@PathVariable String id, @Valid @RequestBody ServiceAreaUpdateRequest request) {
        return driverService.updateServiceArea(id, request);
    }

    @GetMapping("/available")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Find eligible available drivers (used by Ride Management Service)",
            description = "GET /api/drivers/available?serviceArea=Colombo&latitude=6.9271&longitude=79.8612. "
                    + "Eligible = AVAILABLE + same serviceArea (if given) + within the configured radius of "
                    + "latitude/longitude (if given). Coordinates must be sent together. Nearest first.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eligible drivers (empty list if none)"),
            @ApiResponse(responseCode = "400", description = "Only one coordinate sent, or coordinate out of range",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<AvailableDriverResponse> getAvailableDrivers(
            @Parameter(description = "Service area name (case-insensitive)", example = "Colombo")
            @RequestParam(name = "serviceArea", required = false) String serviceArea,
            @Parameter(description = "Pick-up latitude (-90..90)", example = "6.9271")
            @RequestParam(name = "latitude", required = false)
            @DecimalMin(value = "-90.0", message = "latitude must be >= -90")
            @DecimalMax(value = "90.0", message = "latitude must be <= 90") Double latitude,
            @Parameter(description = "Pick-up longitude (-180..180)", example = "79.8612")
            @RequestParam(name = "longitude", required = false)
            @DecimalMin(value = "-180.0", message = "longitude must be >= -180")
            @DecimalMax(value = "180.0", message = "longitude must be <= 180") Double longitude) {
        return driverService.findAvailableDrivers(serviceArea, latitude, longitude);
    }

    @GetMapping("/{driverId}/vehicles")
    @PreAuthorize("hasAnyRole('DRIVER','ADMIN')")
    @Operation(summary = "Get vehicles of a driver", description = "GET /api/drivers/{driverId}/vehicles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicles of the driver (may be empty)"),
            @ApiResponse(responseCode = "404", description = "Driver not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<VehicleResponse> getVehiclesOfDriver(@PathVariable String driverId) {
        return vehicleService.getVehiclesByDriverId(driverId);
    }
}
