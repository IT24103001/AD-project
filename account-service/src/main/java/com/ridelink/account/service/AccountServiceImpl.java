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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Locale;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection: dependencies are explicit, final and easy to mock in tests
    public AccountServiceImpl(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AccountResponse registerPassenger(RegisterRequest request) {
        return register(request, Role.PASSENGER);
    }

    @Override
    public AccountResponse registerDriver(RegisterRequest request) {
        return register(request, Role.DRIVER);
    }

    @Override
    public AccountResponse getAccount(String id, AuthenticatedUser caller) {
        requireSelfOrAdmin(id, caller);
        return AccountMapper.toResponse(findById(id));
    }

    @Override
    public AccountResponse updateProfile(String id, UpdateProfileRequest request, AuthenticatedUser caller) {
        requireSelfOrAdmin(id, caller);
        Account account = findById(id);
        account.setFullName(request.fullName().trim());
        account.setPhone(request.phone());
        account.setUpdatedAt(Instant.now());
        return AccountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    public AccountResponse updateStatus(String id, StatusUpdateRequest request, AuthenticatedUser caller) {
        // Second line of defence: SecurityConfig already restricts this URL to ADMIN.
        if (caller.role() != Role.ADMIN) {
            throw new UnauthorizedException("Only an ADMIN can change account status");
        }
        Account account = findById(id);
        AccountStatus newStatus = request.status();
        account.setAccountStatus(newStatus);
        account.setUpdatedAt(Instant.now());
        log.info("Admin {} changed status of account {} to {}", caller.id(), id, newStatus);
        return AccountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    public RoleResponse getRole(String id, AuthenticatedUser caller) {
        requireSelfOrAdmin(id, caller);
        return new RoleResponse(id, findById(id).getRole());
    }

    // ---------- private helpers ----------

    private AccountResponse register(RegisterRequest request, Role role) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);   // "A@x.com" == "a@x.com"
        if (accountRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        Instant now = Instant.now();
        Account account = new Account();
        account.setFullName(request.fullName().trim());
        account.setEmail(email);
        account.setPhone(request.phone());
        account.setPasswordHash(passwordEncoder.encode(request.password()));   // BCrypt, never plain text
        account.setRole(role);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(now);
        account.setUpdatedAt(now);

        try {
            Account saved = accountRepository.save(account);
            log.info("Registered {} account {}", role, saved.getId());
            return AccountMapper.toResponse(saved);
        } catch (DuplicateKeyException ex) {   // two simultaneous registrations: unique index wins
            throw new DuplicateEmailException(email);
        }
    }

    /** A user may act on their own account; an ADMIN may act on any account. */
    private void requireSelfOrAdmin(String targetId, AuthenticatedUser caller) {
        boolean isAdmin = caller.role() == Role.ADMIN;
        boolean isOwner = caller.id().equals(targetId);
        if (!isAdmin && !isOwner) {
            throw new UnauthorizedException("You are not allowed to access another user's account");
        }
    }

    private Account findById(String id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }
}
