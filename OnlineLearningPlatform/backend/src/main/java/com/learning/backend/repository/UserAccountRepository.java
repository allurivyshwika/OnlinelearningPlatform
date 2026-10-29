package com.learning.backend.repository;

import com.learning.backend.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    java.util.List<UserAccount> findAllByOrderByUserIdDesc();

    long countByRole(String role);
}
