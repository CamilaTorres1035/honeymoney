package com.camss.honeymoney.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.camss.honeymoney.dto.LoginRequest;
import com.camss.honeymoney.dto.LoginResponse;
import com.camss.honeymoney.dto.RegisterRequest;
import com.camss.honeymoney.dto.RegisterResponse;
import com.camss.honeymoney.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> signUp(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(request);

        // Crear la cookie para el refresh token
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(false) // 'true' solo si se usa HTTPS en producción. En local (http://localhost) dejar en 'false' o fallará.
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @org.springframework.web.bind.annotation.CookieValue(name = "refreshToken", required = false) String requestRefreshToken) {
        
        if (requestRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        LoginResponse response = authService.refreshToken(requestRefreshToken);

        // Enviar el nuevo refresh token como una Cookie HttpOnly
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(true) // Cambiar a false si estás probando en local sin HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@org.springframework.web.bind.annotation.CookieValue(name = "refreshToken", required = false) String requestRefreshToken) {
            authService.logout(requestRefreshToken);

            org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // true en producción con HTTPS
                .path("/")
                .maxAge(0) // Expira inmediatamente
                .sameSite("Strict")
                .build();
            
            return ResponseEntity.ok().header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }
    
}
