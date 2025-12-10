package com.example.ticket_system.auth.token;

import com.example.ticket_system.auth.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    @Value("${app.jwt-expiration}")
    private long accessTokenExpiry;
    private final AuthTokenRepository tokenRepository;

    @Transactional
    public AuthToken createToken(User user, TokenType type, long expirySeconds, String userAgent, String ipAddress) {
        AuthToken token = AuthToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .type(type)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(expirySeconds))
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();
        return tokenRepository.save(token);
    }

    @Transactional
    public String refreshAccessToken(String refreshTokenStr, String userAgent, String ipAddress) {

        AuthToken refreshToken = tokenRepository.findByToken(refreshTokenStr)
                .filter(token -> !token.isRevoked() && token.getExpiresAt().isAfter(Instant.now()))
                .filter(token -> token.getType() == TokenType.REFRESH)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        User user = refreshToken.getUser();

        revokeAllUserTokens(user);

        AuthToken newAccessToken = createToken(user, TokenType.ACCESS, accessTokenExpiry, userAgent, ipAddress);

        return newAccessToken.getToken();
    }

    @Transactional
    public void revokeAllUserTokens(User user) {
        tokenRepository.revokeAllUserTokens(user.getId());
    }

    @Transactional
    public void revokeToken(AuthToken token) {
        token.setRevoked(true);
        tokenRepository.save(token);
    }

    @Override
    public Optional<AuthToken> findByToken(String tokenStr) {
        return tokenRepository.findByToken(tokenStr);
    }

    @Override
    public boolean isValidToken(String tokenStr) {
        return tokenRepository.findByToken(tokenStr)
                .filter(token -> !token.isRevoked() && token.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    @Override
    public void deleteToken(AuthToken authToken) {
        tokenRepository.delete(authToken);
    }

}