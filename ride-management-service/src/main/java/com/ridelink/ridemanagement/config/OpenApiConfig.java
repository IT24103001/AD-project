package com.ridelink.ridemanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 / Swagger UI configuration with Bearer JWT security scheme.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RideLink - Ride Management Service API",
                version = "1.0.0",
                description = "IT3130 Application Development Group Assignment - Core Ride Workflow & Lifecycle Microservice. Owns independent MongoDB database 'ridelink_ride_db'.",
                contact = @Contact(name = "Member 3 - Ride Management Service Owner")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide the JWT token issued by the RideLink Account Service (claims: sub, role=PASSENGER|DRIVER|ADMIN)"
)
public class OpenApiConfig {
}
