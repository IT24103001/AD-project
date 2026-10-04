package com.ridelink.account.controller;

import com.ridelink.account.dto.ErrorResponse;
import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Authentication", description = "Login and token issuance")
public class AuthController {

    private static final String LOGIN_EXAMPLE = """
            {
              "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
              "tokenType": "Bearer",
              "expiresInSeconds": 3600,
              "user": {
                "id": "6650f1c2a1b2c3d4e5f60718",
                "fullName": "Nimal Perera",
                "email": "nimal@example.com",
                "phone": "+94771234567",
                "role": "PASSENGER",
                "accountStatus": "ACTIVE",
                "createdAt": "2026-10-04T08:30:00Z",
                "updatedAt": "2026-10-04T08:30:00Z"
              }
            }""";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT", description = "Public endpoint. No authentication required.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class),
                examples = @ExampleObject(value = LOGIN_EXAMPLE))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Wrong email or password",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Account is INACTIVE or SUSPENDED",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
