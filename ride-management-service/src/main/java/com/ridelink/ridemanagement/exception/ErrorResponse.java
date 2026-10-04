package com.ridelink.ridemanagement.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized JSON error payload returned by GlobalExceptionHandler (@RestControllerAdvice).
 */
@Schema(description = "Consistent JSON error response structure across all Ride Management APIs")
public class ErrorResponse {

    @Schema(description = "UTC timestamp when the error occurred", example = "2026-10-04T14:30:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP status reason phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Machine-readable error classification code", example = "INVALID_RIDE_STATUS_TRANSITION")
    private String errorCode;

    @Schema(description = "Human-readable explanation of the failure", example = "Invalid ride status transition from COMPLETED to IN_PROGRESS.")
    private String message;

    @Schema(description = "Request URI path", example = "/api/rides/665f1a2b3c4d5e6f7a8b9c0d/start")
    private String path;

    @Schema(description = "Field-level validation errors when applicable")
    private Map<String, String> validationErrors;

    public ErrorResponse() {
    }

    public ErrorResponse(Instant timestamp, int status, String error, String errorCode, String message, String path, Map<String, String> validationErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
