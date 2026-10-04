package com.ridelink.drivervehicle.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Used when SECURITY_ENABLED=false (default): everything is open so the service can be
 * demonstrated in Swagger/Postman on its own. Role checks (@PreAuthorize) are NOT active in this mode.
 */
@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "ridelink.security.enabled", havingValue = "false", matchIfMissing = true)
public class OpenSecurityConfig {

    @Bean
    public SecurityFilterChain openFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
