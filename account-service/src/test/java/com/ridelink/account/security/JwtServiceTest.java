package com.ridelink.account.security;

import com.ridelink.account.model.Account;
import com.ridelink.account.model.Role;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234";

    private Account account() {
        Account a = new Account();
        a.setId("acc-1");
        a.setEmail("nimal@example.com");
        a.setRole(Role.DRIVER);
        return a;
    }

    @Test
    @DisplayName("Generated token can be parsed back to the same user")
    void generateAndParse_roundTrip() {
        JwtService jwtService = new JwtService(SECRET, 60_000);

        AuthenticatedUser user = jwtService.parseToken(jwtService.generateToken(account()));

        assertThat(user.id()).isEqualTo("acc-1");
        assertThat(user.email()).isEqualTo("nimal@example.com");
        assertThat(user.role()).isEqualTo(Role.DRIVER);
    }

    @Test
    @DisplayName("Token signed with a different secret is rejected")
    void parse_wrongSecret_throws() {
        String token = new JwtService(SECRET, 60_000).generateToken(account());
        JwtService other = new JwtService("another-secret-another-secret-123456", 60_000);

        assertThrows(JwtException.class, () -> other.parseToken(token));
    }

    @Test
    @DisplayName("Expired token is rejected")
    void parse_expiredToken_throws() {
        JwtService jwtService = new JwtService(SECRET, -1000);

        String token = jwtService.generateToken(account());

        assertThrows(JwtException.class, () -> jwtService.parseToken(token));
    }

    @Test
    @DisplayName("Secret shorter than 32 characters is refused at startup")
    void constructor_shortSecret_throws() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short", 60_000));
    }
}
