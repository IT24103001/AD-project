package com.ridelink.drivervehicle.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/** The single error format returned by every failing endpoint. */
@Schema(description = "Standard error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        @Schema(example = "404") int status,
        @Schema(example = "DRIVER_NOT_FOUND") String error,
        @Schema(example = "Driver not found with id: 123") String message,
        @Schema(example = "/api/drivers/123") String path,
        @Schema(description = "Field-level problems; only present for validation errors",
                example = "{\"name\": \"name cannot be blank\"}") Map<String, String> details) {
}
