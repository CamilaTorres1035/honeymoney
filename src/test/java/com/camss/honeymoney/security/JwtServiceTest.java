package com.camss.honeymoney.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.security.SignatureException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;

class JwtServiceTest {
    private JwtService jwtService;
    private UserDetails mockUser;

    private final String base64Secret = Base64.getEncoder().encodeToString("3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007b".getBytes());

    private final long expirationTime = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Inyectamos manualmente los valores de @Value
        ReflectionTestUtils.setField(jwtService, "secretKey", base64Secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expirationTime);

        // Simulamos el UserDetails de Spring Security
        mockUser = Mockito.mock(UserDetails.class);
        when(mockUser.getUsername()).thenReturn("usuario@prueba.com");
    }

    @Test
    void generateToken_ShouldReturnValidToken(){
        String token = jwtService.generateToken(mockUser);

        assertNotNull(token);
        assertEquals("usuario@prueba.com", jwtService.extractUserName(token));
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsCorrect(){
        String token = jwtService.generateToken(mockUser);

        boolean isValid = jwtService.isTokenValid(token, mockUser);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameDoesNotMatch(){
        String token = jwtService.generateToken(mockUser);

        UserDetails wrongUser = Mockito.mock(UserDetails.class);

        when(wrongUser.getUsername()).thenReturn("otroUsuario@prueba.com");

        boolean isValid = jwtService.isTokenValid(token, wrongUser);

        assertFalse(isValid);
    }

    @Test 
    void getExpirationTime_ShouldReturnConfiguredValue(){
        assertEquals(expirationTime, jwtService.getExpirationTime());
    }

    @Test
    void isTokenExpired_ShoulThrowException_WhenTokenIsExpired(){
        // Forzamos expiración estableciendo la expiración a 0 o negativo
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String expiredToken = jwtService.generateToken(mockUser);

        assertThrows(ExpiredJwtException.class, () -> {jwtService.isTokenValid(expiredToken, mockUser);});
    }

    @Test
    void generateToken_WithExtraClaims_ShouldIncludeClaimsPayload(){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ROLE_ADMIN");
        extraClaims.put("userId", 123);

        String token = jwtService.generateToken(extraClaims, mockUser);
        String extractedRole = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        Integer extractedId = jwtService.extractClaim(token, claims -> claims.get("userId", Integer.class));

        assertEquals("ROLE_ADMIN", extractedRole);
        assertEquals(123, extractedId);
    }

    @Test 
    void extractAllClaims_ShouldThrowSignatureException_WhenTokenIsTampered(){
        String validToken = jwtService.generateToken(mockUser);
        String[] parts = validToken.split("\\.");

        String tamperedPayload = parts[1] + "X";
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];
        
        assertThrows(SignatureException.class, () -> {
            jwtService.extractUserName(tamperedToken);
        });
    }
}
