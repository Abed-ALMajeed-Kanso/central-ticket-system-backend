package com.example.ticket_system.auth.token;

import com.example.ticket_system.auth.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface TokenService {

    public AuthToken createToken(User user, TokenType type, long expirySeconds, String userAgent, String ipAddress);

    public void revokeAllUserTokens(User user);

    public String refreshAccessToken(String refreshTokenStr, String userAgent, String ipAddress);

    public boolean isValidToken(String tokenStr);

    public void revokeToken(AuthToken token);

    Optional<AuthToken> findByToken(String tokenStr);

    public void deleteToken(AuthToken authToken);

}
