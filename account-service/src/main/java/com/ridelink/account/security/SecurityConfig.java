package com.ridelink.account.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   JsonSecurityErrorHandler errorHandler) throws Exception {
        http
            .csrf(csrf -> csrf.disable())                       // stateless REST API, no cookies/sessions
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint(errorHandler)         // 401 JSON
                .accessDeniedHandler(errorHandler))             // 403 JSON
            .authorizeHttpRequests(auth -> auth
                // public endpoints
                .requestMatchers("/api/accounts/register/**", "/api/accounts/login").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                // admin only
                .requestMatchers(HttpMethod.PATCH, "/api/accounts/*/status").hasRole("ADMIN")
                // everything else needs a valid token (ownership checked again in the service layer)
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();                     // salted, slow hash
    }
}
