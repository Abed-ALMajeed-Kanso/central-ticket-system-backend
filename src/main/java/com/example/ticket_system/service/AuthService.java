package com.example.ticket_system.service;

import com.example.ticket_system.auth.dto.JwtAuthResponse;
import com.example.ticket_system.auth.dto.LoginDto;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    JwtAuthResponse login(LoginDto loginDto, HttpServletRequest request);

    void logout(String refreshTokenStr);

    JwtAuthResponse refresh(String refreshToken, String currentAccessToken);
}
