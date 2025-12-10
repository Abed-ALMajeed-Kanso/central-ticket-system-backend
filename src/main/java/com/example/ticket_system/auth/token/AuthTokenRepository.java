package com.example.ticket_system.auth.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {

    Optional<AuthToken> findByToken(String token);

    List<AuthToken> findAllByUserIdAndRevokedFalse(Long userId);

    @Modifying
    @Query("UPDATE AuthToken t SET t.revoked = true WHERE t.user.id = :userId")
    void revokeAllUserTokens(Long userId);

    @Modifying
    @Query("UPDATE AuthToken t SET t.revoked = true WHERE t.user.id = :userId AND t.id != :currentTokenId")
    void revokeOtherTokens(Long userId, Long currentTokenId);

}