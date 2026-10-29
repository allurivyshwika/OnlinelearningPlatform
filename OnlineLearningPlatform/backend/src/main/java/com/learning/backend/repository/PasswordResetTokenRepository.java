package com.learning.backend.repository;

import com.learning.backend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
            String tokenHash, LocalDateTime now);

    void deleteAllByUserId(Integer userId);

    @Modifying
    @Query("""
            update PasswordResetToken token
            set token.usedAt = :now
            where token.tokenHash = :tokenHash
              and token.usedAt is null
              and token.expiresAt > :now
            """)
    int consumeIfActive(
            @Param("tokenHash") String tokenHash,
            @Param("now") LocalDateTime now);
}
