package com.ridelink.account.controller;

import com.ridelink.account.config.OpenApiConfig;
import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.ErrorResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.RoleResponse;
import com.ridelink.account.dto.StatusUpdateRequest;
import com.ridelink.account.dto.UpdateProfileRequest;
import com.ridelink.account.security.AuthenticatedUser;
import com.ridelink.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Thin controller: parse request -> call service -> return response. No business logic here. */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Registration, profile and account status")
public class AccountController {

    private static final String ACCOUNT_EXAMPLE = """
            {
              "id": "6650f1c2a1b2c3d4e5f60718",
              "fullName": "Nimal Perera",
              "email": "nimal@example.com",
              "phone": "+94771234567",
              "role": "PASSENGER",
              "accountStatus": "ACTIVE",
              "createdAt": "2026-10-04T08:30:00Z",
              "updatedAt": "2026-10-04T08:30:00Z"
            }""";
    private static final String ROLE_EXAMPLE = """
            { "accountId": "6650f1c2a1b2c3d4e5f60718", "role": "PASSENGER" }""";
    private static final String VALIDATION_ERROR_EXAMPLE = """
            {
              "timestamp": "2026-10-04T08:31:00Z",
              "status": 400,
              "error": "Bad Request",
              "message": "Validation failed",
              "path": "/api/accounts/register/passenger",
              "validationErrors": {
                "email": "Email must be a valid email address",
                "password": "Password must be 8-64 characters and contain at least one letter and one digit"
              }
            }""";

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/register/passenger")
    @Operation(summary = "Register a passenger account", description = "Public endpoint. Role is set to PASSENGER by the server.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Passenger created",
            content = @Content(schema = @Schema(implementation = AccountResponse.class),
                examples = @ExampleObject(value = ACCOUNT_EXAMPLE))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = VALIDATION_ERROR_EXAMPLE))),
        @ApiResponse(responseCode = "409", description = "Email already registered",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> registerPassenger(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.registerPassenger(request));
    }

    @PostMapping("/register/driver")
    @Operation(summary = "Register a driver account", description = "Public endpoint. Role is set to DRIVER by the server. "
            + "Vehicle details belong to the Driver & Vehicle Service, which links to this account by accountId.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Driver created",
            content = @Content(schema = @Schema(implementation = AccountResponse.class),
                examples = @ExampleObject(value = ACCOUNT_EXAMPLE))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = VALIDATION_ERROR_EXAMPLE))),
        @ApiResponse(responseCode = "409", description = "Email already registered",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> registerDriver(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.registerDriver(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "View a profile", description = "Requires JWT. Owner or ADMIN only.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile returned",
            content = @Content(schema = @Schema(implementation = AccountResponse.class),
                examples = @ExampleObject(value = ACCOUNT_EXAMPLE))),
        @ApiResponse(responseCode = "401", description = "Missing/invalid token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Not the owner and not ADMIN",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String id,
                                                      @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(accountService.getAccount(id, caller));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update profile (fullName, phone)", description = "Requires JWT. Owner or ADMIN only. "
            + "Email, role and status cannot be changed here.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated",
            content = @Content(schema = @Schema(implementation = AccountResponse.class),
                examples = @ExampleObject(value = ACCOUNT_EXAMPLE))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = VALIDATION_ERROR_EXAMPLE))),
        @ApiResponse(responseCode = "401", description = "Missing/invalid token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Not the owner and not ADMIN",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> updateProfile(@PathVariable String id,
                                                         @Valid @RequestBody UpdateProfileRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(accountService.updateProfile(id, request, caller));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change account status", description = "Requires JWT with role ADMIN. "
            + "Allowed values: ACTIVE, INACTIVE, SUSPENDED.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated",
            content = @Content(schema = @Schema(implementation = AccountResponse.class),
                examples = @ExampleObject(value = ACCOUNT_EXAMPLE))),
        @ApiResponse(responseCode = "400", description = "Missing or invalid status value",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing/invalid token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Caller is not ADMIN",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> updateStatus(@PathVariable String id,
                                                        @Valid @RequestBody StatusUpdateRequest request,
                                                        @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(accountService.updateStatus(id, request, caller));
    }

    @GetMapping("/{id}/role")
    @Operation(summary = "Get the role of an account", description = "Requires JWT. Owner or ADMIN only.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Role returned",
            content = @Content(schema = @Schema(implementation = RoleResponse.class),
                examples = @ExampleObject(value = ROLE_EXAMPLE))),
        @ApiResponse(responseCode = "401", description = "Missing/invalid token",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Not the owner and not ADMIN",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RoleResponse> getRole(@PathVariable String id,
                                                @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(accountService.getRole(id, caller));
    }
}
