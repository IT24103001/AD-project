package com.ridelink.drivervehicle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Used when SECURITY_ENABLED=true. This service does NOT log users in (that is the Account Service's job);
 * it only VERIFIES the JWT sent in "Authorization: Bearer <token>" using the shared HS256 secret from JWT_SECRET.
 *
 * Assumed token contract (agree it with the Account Service owner): a "roles" claim containing e.g. ["DRIVER"] or ["ADMIN"].
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // turns on the @PreAuthorize annotations used in the controllers
@ConditionalOnProperty(name = "ridelink.security.enabled", havingValue = "true")
public class JwtSecurityConfig {

    @Bean
    public JwtDecoder jwtDecoder(@Value("${ridelink.security.jwt-secret:}") String secret) {
        // Fail fast at start-up instead of silently running with no/weak secret.
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be set (at least 32 characters) when SECURITY_ENABLED=true");
        }
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Bean
    public SecurityFilterChain securedFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception {
        // Turn the "roles" claim into Spring roles: ["DRIVER"] -> ROLE_DRIVER
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(converter)));
        return http.build();
    }
}
