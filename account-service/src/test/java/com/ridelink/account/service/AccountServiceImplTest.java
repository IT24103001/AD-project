package com.ridelink.account.service;

import com.ridelink.account.dto.AccountResponse;
import com.ridelink.account.dto.RegisterRequest;
import com.ridelink.account.dto.RoleResponse;
import com.ridelink.account.dto.StatusUpdateRequest;
import com.ridelink.account.dto.UpdateProfileRequest;
import com.ridelink.account.exception.AccountNotFoundException;
import com.ridelink.account.exception.DuplicateEmailException;
import com.ridelink.account.exception.UnauthorizedException;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class AccountServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private AccountServiceImpl accountService;

    private final AuthenticatedUser owner = new AuthenticatedUser("acc-1", "nimal@example.com", Role.PASSENGER);
    private final AuthenticatedUser stranger = new AuthenticatedUser("acc-2", "other@example.com", Role.DRIVER);
    private final AuthenticatedUser admin = new AuthenticatedUser("admin-1", "admin@example.com", Role.ADMIN);

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(accountRepository, passwordEncoder);
    }

    private RegisterRequest registerRequest() {
        return new RegisterRequest("Nimal Perera", "Nimal@Example.com", "+94771234567", "Passw0rd123");
    }

    private Account existingAccount() {
        Account a = new Account();
        a.setId("acc-1");
        a.setFullName("Nimal Perera");
        a.setEmail("nimal@example.com");
        a.setPhone("+94771234567");
        a.setPasswordHash("hashed");
        a.setRole(Role.PASSENGER);
        a.setAccountStatus(AccountStatus.ACTIVE);
        a.setCreatedAt(Instant.now());
        a.setUpdatedAt(Instant.now());
        return a;
    }

    private void stubSaveAssigningId() {
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId("acc-1");
            return a;
        });
    }

    // 1
    @Test
    @DisplayName("Passenger registration: role PASSENGER, ACTIVE, password hashed, email lower-cased")
    void registerPassenger_success() {
        when(accountRepository.existsByEmail("nimal@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Passw0rd123")).thenReturn("hashed");
        stubSaveAssigningId();

        AccountResponse response = accountService.registerPassenger(registerRequest());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("hashed").isNotEqualTo("Passw0rd123");
        assertThat(saved.getEmail()).isEqualTo("nimal@example.com");
        assertThat(response.id()).isEqualTo("acc-1");
        assertThat(response.role()).isEqualTo(Role.PASSENGER);
        assertThat(response.accountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    // 2
    @Test
    @DisplayName("Driver registration: role DRIVER")
    void registerDriver_success() {
        when(accountRepository.existsByEmail("nimal@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Passw0rd123")).thenReturn("hashed");
        stubSaveAssigningId();

        AccountResponse response = accountService.registerDriver(registerRequest());

        assertThat(response.role()).isEqualTo(Role.DRIVER);
        assertThat(response.accountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    // 3
    @Test
    @DisplayName("Duplicate email is rejected and nothing is saved")
    void register_duplicateEmail_throws() {
        when(accountRepository.existsByEmail("nimal@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> accountService.registerPassenger(registerRequest()));

        verify(accountRepository, never()).save(any());
    }

    // 7
    @Test
    @DisplayName("Owner can view own profile")
    void getProfile_owner_success() {
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount()));

        AccountResponse response = accountService.getAccount("acc-1", owner);

        assertThat(response.email()).isEqualTo("nimal@example.com");
    }

    @Test
    @DisplayName("Admin can view any profile")
    void getProfile_admin_success() {
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount()));

        assertThat(accountService.getAccount("acc-1", admin).id()).isEqualTo("acc-1");
    }

    // 8
    @Test
    @DisplayName("Owner can update fullName and phone")
    void updateProfile_success() {
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount()));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.updateProfile("acc-1",
                new UpdateProfileRequest("Nimal K. Perera", "+94770000000"), owner);

        assertThat(response.fullName()).isEqualTo("Nimal K. Perera");
        assertThat(response.phone()).isEqualTo("+94770000000");
        assertThat(response.email()).isEqualTo("nimal@example.com");   // unchanged
    }

    // 9
    @Test
    @DisplayName("Account not found")
    void getProfile_notFound_throws() {
        when(accountRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount("missing", admin));
    }

    // 10
    @Test
    @DisplayName("User cannot view another user's profile")
    void getProfile_otherUser_unauthorized() {
        assertThrows(UnauthorizedException.class, () -> accountService.getAccount("acc-1", stranger));
        verify(accountRepository, never()).findById(any());
    }

    @Test
    @DisplayName("User cannot update another user's profile")
    void updateProfile_otherUser_unauthorized() {
        assertThrows(UnauthorizedException.class, () -> accountService.updateProfile("acc-1",
                new UpdateProfileRequest("Hacker", "+94770000000"), stranger));
        verify(accountRepository, never()).save(any());
    }

    // 11
    @Test
    @DisplayName("Admin can change account status")
    void updateStatus_admin_success() {
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount()));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.updateStatus("acc-1",
                new StatusUpdateRequest(AccountStatus.SUSPENDED), admin);

        assertThat(response.accountStatus()).isEqualTo(AccountStatus.SUSPENDED);
    }

    @Test
    @DisplayName("Non-admin cannot change account status")
    void updateStatus_nonAdmin_unauthorized() {
        assertThrows(UnauthorizedException.class, () -> accountService.updateStatus("acc-1",
                new StatusUpdateRequest(AccountStatus.SUSPENDED), owner));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Owner can read own role")
    void getRole_owner_success() {
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(existingAccount()));

        RoleResponse response = accountService.getRole("acc-1", owner);

        assertThat(response.role()).isEqualTo(Role.PASSENGER);
    }
}
