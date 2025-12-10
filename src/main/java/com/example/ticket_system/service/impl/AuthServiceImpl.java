package com.example.ticket_system.service.impl;

import com.example.ticket_system.auth.dto.JwtAuthResponse;
import com.example.ticket_system.auth.dto.LoginDto;
import com.example.ticket_system.auth.dto.NoIdUserDto;
import com.example.ticket_system.auth.token.AuthToken;
import com.example.ticket_system.auth.token.TokenService;
import com.example.ticket_system.auth.token.TokenType;
import com.example.ticket_system.auth.user.entity.Role;
import com.example.ticket_system.auth.user.entity.User;
import com.example.ticket_system.auth.user.repository.UserRepository;
import com.example.ticket_system.auth.security.jwt.JwtTokenProvider;
import com.example.ticket_system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ModelMapper modelMapper;
    private final TokenService tokenService;

    @Value("${app.refresh-short-expiration}")
    private long refreshTokenShortExpiration;

    @Value("${app.refresh-long-expiration}")
    private long refreshTokenLongtExpiration;

    public AuthServiceImpl(UserRepository userRepository,
                           AuthenticationManager authenticationManager,
                           JwtTokenProvider jwtTokenProvider,
                           ModelMapper modelMapper,
                           TokenService tokenService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.modelMapper = modelMapper;
        this.tokenService = tokenService;
    }

    @Override
    public JwtAuthResponse login(LoginDto loginDto, HttpServletRequest request) {

        // Authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        tokenService.revokeAllUserTokens(user);

        // Create refresh token
        long refreshExpiry = loginDto.getRememberMe() ? refreshTokenLongtExpiration : refreshTokenShortExpiration;
        AuthToken refreshToken = tokenService.createToken(
                user,
                TokenType.REFRESH,
                refreshExpiry,
                request.getHeader("User-Agent"),
                request.getRemoteAddr()
        );

        // Prepare user DTO
        NoIdUserDto noIduserDto = modelMapper.map(user, NoIdUserDto.class);
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");
        noIduserDto.setRole(roleName);

        // Return JWT response
        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setAccessToken(accessToken);
        jwtAuthResponse.setRefreshToken(refreshToken.getToken());
        jwtAuthResponse.setUser(noIduserDto);

        return jwtAuthResponse;
    }

    @Override
    public JwtAuthResponse refresh(String refreshToken, String currentAccessToken) {
        // 1. If current access token is still valid, do nothing
        if (currentAccessToken != null && jwtTokenProvider.validateToken(currentAccessToken)) {
            return null; // token still valid, no refresh needed
        }

        // 2. Check refresh token in DB
        Optional<AuthToken> tokenOpt = tokenService.findByToken(refreshToken);
        if (tokenOpt.isEmpty()) return null;

        AuthToken token = tokenOpt.get();

        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            return null; // revoked or expired
        }

        // 3. Create new access token
        User user = token.getUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                user.getPassword()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateToken(authentication);

        JwtAuthResponse response = new JwtAuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        NoIdUserDto userDto = modelMapper.map(user, NoIdUserDto.class);
        userDto.setRole(user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER"));
        response.setUser(userDto);

        return response;
    }

    @Override
    public void logout(String refreshTokenStr) {
        tokenService.findByToken(refreshTokenStr)
                .ifPresent(tokenService::deleteToken);
    }

}