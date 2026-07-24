package com.camss.honeymoney.service;

import java.time.Instant;
import java.util.Collections;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.camss.honeymoney.dto.LoginRequest;
import com.camss.honeymoney.dto.LoginResponse;
import com.camss.honeymoney.dto.RegisterRequest;
import com.camss.honeymoney.dto.RegisterResponse;
import com.camss.honeymoney.exception.DuplicatedEmailException;
import com.camss.honeymoney.model.RefreshToken;
import com.camss.honeymoney.model.User;
import com.camss.honeymoney.repository.UserRepository;
import com.camss.honeymoney.security.JwtService;

@Service
public class AuthService {
    private final JwtService jwtService;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public RegisterResponse signUp(RegisterRequest request){
        if (userRepository.existsByEmail(request.email()) ) {throw new DuplicatedEmailException("El email ya está registrado: " + request.email());}
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedAt(Instant.now());

        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getCreatedAt());
    }

    public LoginResponse authenticate(LoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email()).orElseThrow();

        var principal = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), Collections.emptyList());

        String accessToken = jwtService.generateToken(principal);
        RefreshToken refreshToken = jwtService.createRefreshToken(user.getEmail());

        return new LoginResponse(accessToken, refreshToken.getToken(), new LoginResponse.UserSummary(user.getId(), user.getName(), user.getEmail()));
    }

    public LoginResponse refreshToken(String requestRefreshToken) {
        RefreshToken refreshToken = jwtService.findByToken(requestRefreshToken)
                .map(jwtService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Refresh token no encontrado o expirado."));

        User user = refreshToken.getUser();

        // Rotación: Eliminar el refresh token viejo y generar uno nuevo
        jwtService.deleteRefreshToken(refreshToken);
        RefreshToken newRefreshToken = jwtService.createRefreshToken(user.getEmail());

        // Generar un nuevo Access Token
        var principal = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), Collections.emptyList());
        String newAccessToken = jwtService.generateToken(principal);

        return new LoginResponse(
            newAccessToken, 
            newRefreshToken.getToken(), 
            new LoginResponse.UserSummary(user.getId(), user.getName(), user.getEmail())
        );
    }

    public void logout(String requestRefreshToken){
        if (requestRefreshToken!= null) {
            jwtService.findByToken(requestRefreshToken).ifPresent(jwtService::deleteRefreshToken);
        }
    }
}
