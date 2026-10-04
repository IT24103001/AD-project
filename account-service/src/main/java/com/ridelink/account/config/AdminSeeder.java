package com.ridelink.account.config;

import com.ridelink.account.model.Account;
import com.ridelink.account.model.AccountStatus;
import com.ridelink.account.model.Role;
import com.ridelink.account.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;

/**
 * There is deliberately no public "register admin" endpoint (anyone could make themselves admin).
 * Instead the first ADMIN is created at startup from ADMIN_EMAIL / ADMIN_PASSWORD env variables.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String fullName;
    private final String phone;

    public AdminSeeder(AccountRepository accountRepository, PasswordEncoder passwordEncoder,
                       @Value("${admin.email:}") String email,
                       @Value("${admin.password:}") String password,
                       @Value("${admin.full-name}") String fullName,
                       @Value("${admin.phone}") String phone) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank()) {
            log.info("ADMIN_EMAIL / ADMIN_PASSWORD not set - no admin account seeded");
            return;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (accountRepository.existsByEmail(normalized)) {
            return;
        }
        Account admin = new Account();
        admin.setFullName(fullName);
        admin.setEmail(normalized);
        admin.setPhone(phone);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setAccountStatus(AccountStatus.ACTIVE);
        admin.setCreatedAt(Instant.now());
        admin.setUpdatedAt(Instant.now());
        accountRepository.save(admin);
        log.info("Seeded ADMIN account for {}", normalized);
    }
}
