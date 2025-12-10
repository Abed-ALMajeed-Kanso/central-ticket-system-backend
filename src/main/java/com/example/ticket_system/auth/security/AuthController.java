package com.example.ticket_system.auth.security;

import com.example.ticket_system.auth.dto.JwtAuthResponse;
import com.example.ticket_system.auth.dto.LoginDto;
import com.example.ticket_system.auth.dto.NoIdUserDto;
import com.example.ticket_system.auth.security.jwt.JwtTokenProvider;
import com.example.ticket_system.auth.token.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import com.example.ticket_system.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt-expiration}")
    private long jwtExpiration;

    @Value("${app.refresh-short-expiration}")
    private long refreshTokenShortExpiration;

    @Value("${app.refresh-long-expiration}")
    private long refreshTokenLongtExpiration;

    public AuthController(AuthService authService, TokenService tokenService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @PostMapping("/login")
    public ResponseEntity<NoIdUserDto> login(@RequestBody LoginDto loginDto, HttpServletRequest request) {

        JwtAuthResponse jwtAuthResponse = authService.login(loginDto, request);

        long refreshTokenExpiry = loginDto.getRememberMe() ? refreshTokenLongtExpiration : refreshTokenShortExpiration;

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwtAuthResponse.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtExpiration / 1000)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", jwtAuthResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenExpiry)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(jwtAuthResponse.getUser());
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(
            @CookieValue(value = "jwt", required = false) String accessToken
    ) {
        if (accessToken == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
            boolean valid = jwtTokenProvider.validateToken(accessToken);
            if (valid)
                return ResponseEntity.ok("Token is valid");
            else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(value = "jwt", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null) {

            ResponseCookie clearJwt = ResponseCookie.from("jwt", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, clearJwt.toString())
                    .header(HttpHeaders.SET_COOKIE, clearRefresh.toString())
                    .body("Missing refresh token");
        }

        JwtAuthResponse jwtAuthResponse = authService.refresh(refreshToken, accessToken);

        if (jwtAuthResponse == null) {
            // Remove cookies on invalid/expired refresh token
            ResponseCookie clearJwt = ResponseCookie.from("jwt", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, clearJwt.toString())
                    .header(HttpHeaders.SET_COOKIE, clearRefresh.toString())
                    .body("Invalid or expired refresh token");
        }

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwtAuthResponse.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtExpiration / 1000) // ensure seconds
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(jwtAuthResponse.getUser());


    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {

        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("refreshToken".equals(c.getName())) {
                    refreshToken = c.getValue();
                    break;
                }
            }
        }

        // Invalidate refresh token server-side
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }

        // Delete refresh cookie
        ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        // Delete JWT cookie (your login cookie is named "jwt")
        ResponseCookie deleteJwt = ResponseCookie.from("jwt", "")
                .httpOnly(true)   // should match login cookie
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
                .header(HttpHeaders.SET_COOKIE, deleteJwt.toString())
                .body("Logged out successfully.");
    }

}