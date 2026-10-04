package com.ridelink.account.repository;

import com.ridelink.account.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AccountRepository extends MongoRepository<Account, String> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);
}
