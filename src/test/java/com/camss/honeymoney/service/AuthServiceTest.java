package com.camss.honeymoney.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.camss.honeymoney.dto.LoginRequest;
import com.camss.honeymoney.dto.LoginResponse;
import com.camss.honeymoney.dto.RegisterRequest;
import com.camss.honeymoney.dto.RegisterResponse;
import com.camss.honeymoney.exception.DuplicatedEmailException;
import com.camss.honeymoney.model.User;
import com.camss.honeymoney.repository.UserRepository;
import com.camss.honeymoney.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User userMock;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("Juan", "juan@example.com", "password123");
        
        userMock = new User();
        userMock.setId(1L);
        userMock.setName("Juan");
        userMock.setEmail("juan@example.com");
        userMock.setPassword("encodedPassword");
        userMock.setCreatedAt(Instant.now());
    }

    @Test
    void signUp_WhenEmailDoesNotExist_ShouldRegisterUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userMock);

        // Act
        RegisterResponse response = authService.signUp(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals(userMock.getId(), response.id());
        assertEquals(userMock.getName(), response.name());
        assertEquals(userMock.getEmail(), response.email());
        
        verify(userRepository, times(1)).existsByEmail(registerRequest.email());
        verify(passwordEncoder, times(1)).encode(registerRequest.password());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void signUp_WhenEmailAlreadyExists_ShouldThrowDuplicatedEmailException() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        // Act & Assert
        DuplicatedEmailException exception = assertThrows(DuplicatedEmailException.class, () -> {
            authService.signUp(registerRequest);
        });

        assertEquals("El email ya está registrado: " + registerRequest.email(), exception.getMessage());
        
        // Verificar que no se llamó a codificar clave ni a guardar
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_WhenCredentialsAreValid_ShouldReturnLoginResponse() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("juan@example.com", "password123");
        String mockToken = "eyJhbGciOiJIUzI1NiJ9.mockToken";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(userMock));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(mockToken);

        // Act
        LoginResponse response = authService.authenticate(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(mockToken, response.token());
        assertEquals(userMock.getId(), response.user().id());
        assertEquals(userMock.getName(), response.user().name());
        assertEquals(userMock.getEmail(), response.user().email());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(loginRequest.email());
        verify(jwtService, times(1)).generateToken(any(UserDetails.class));
    }

    @Test
    void authenticate_WhenUserDoesNotExist_ShouldThrowException(){
        // Arrenge
        LoginRequest loginRequest = new LoginRequest("juan@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> authService.authenticate(loginRequest));

        // Verify
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail(loginRequest.email());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void authenticate_WhenCredentialsAreNotValid_ShouldThrowBadCredentialsException(){
        // Arrange
        LoginRequest loginRequest = new LoginRequest("juan@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad Credentials"));

        assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> authService.authenticate(loginRequest));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any());
    }
}
