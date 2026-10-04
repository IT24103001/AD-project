package com.ridelink.account.service;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.exception.AccountInactiveException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(accountRepository, passwordEncoder, jwtService);
    }

    private Account account(AccountStatus status) {
        Account a = new Account();
        a.setId("acc-1");
        a.setFullName("Nimal Perera");
        a.setEmail("nimal@example.com");
        a.setPhone("+94771234567");
        a.setPasswordHash("hashed");
        a.setRole(Role.PASSENGER);
        a.setAccountStatus(status);
        a.setCreatedAt(Instant.now());
        a.setUpdatedAt(Instant.now());
        return a;
    }

    // 4
    @Test
    @DisplayName("Login success returns token and basic user info")
    void login_success() {
        Account account = account(AccountStatus.ACTIVE);
        when(accountRepository.findByEmail("nimal@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Passw0rd123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(account)).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3_600_000L);

        LoginResponse response = authService.login(new LoginRequest("Nimal@Example.com", "Passw0rd123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600);
        assertThat(response.user().id()).isEqualTo("acc-1");
        assertThat(response.user().role()).isEqualTo(Role.PASSENGER);
    }

    // 5
    @Test
    @DisplayName("Wrong password -> InvalidCredentialsException, no token issued")
    void login_invalidPassword_throws() {
        when(accountRepository.findByEmail("nimal@example.com")).thenReturn(Optional.of(account(AccountStatus.ACTIVE)));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("nimal@example.com", "wrong")));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Unknown email -> InvalidCredentialsException (same error as wrong password)")
    void login_unknownEmail_throws() {
        when(accountRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("nobody@example.com", "Passw0rd123")));
    }

    // 6
    @Test
    @DisplayName("Suspended account cannot log in")
    void login_suspendedAccount_throws() {
        when(accountRepository.findByEmail("nimal@example.com")).thenReturn(Optional.of(account(AccountStatus.SUSPENDED)));
        when(passwordEncoder.matches("Passw0rd123", "hashed")).thenReturn(true);

        assertThrows(AccountInactiveException.class,
                () -> authService.login(new LoginRequest("nimal@example.com", "Passw0rd123")));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Inactive account cannot log in")
    void login_inactiveAccount_throws() {
        when(accountRepository.findByEmail("nimal@example.com")).thenReturn(Optional.of(account(AccountStatus.INACTIVE)));
        when(passwordEncoder.matches("Passw0rd123", "hashed")).thenReturn(true);

        assertThrows(AccountInactiveException.class,
                () -> authService.login(new LoginRequest("nimal@example.com", "Passw0rd123")));
    }
}
