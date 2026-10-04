package com.ridelink.account.service;

import com.ridelink.account.dto.LoginRequest;
import com.ridelink.account.dto.LoginResponse;
import com.ridelink.account.exception.AccountInactiveException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(AccountRepository accountRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * 1. find account by email  2. check password (BCrypt)  3. check status is ACTIVE  4. issue JWT.
     * The status is checked AFTER the password so that strangers cannot learn an account's status.
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException(account.getAccountStatus());
        }

        String token = jwtService.generateToken(account);
        return new LoginResponse(token, "Bearer", jwtService.getExpirationMs() / 1000, AccountMapper.toResponse(account));
    }
}
