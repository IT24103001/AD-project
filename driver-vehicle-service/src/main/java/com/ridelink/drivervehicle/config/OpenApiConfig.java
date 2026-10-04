package com.ridelink.drivervehicle.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI title/description and the "Authorize" button for a Bearer JWT. */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI driverVehicleOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Driver & Vehicle Service")
                        .version("1.0.0")
                        .description("Driver profiles, vehicles, availability, service area, simulated location "
                                + "and eligible-driver search for the Ride Management Service."))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
