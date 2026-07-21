package com.camss.honeymoney.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.camss.honeymoney.dto.LoginResponse;
import com.camss.honeymoney.dto.RegisterResponse;
import com.camss.honeymoney.exception.DuplicatedEmailException;
import com.camss.honeymoney.service.AuthService;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // ---------------------------------------------------------------
    // Registro
    // ---------------------------------------------------------------

    @Test
    void register_WithValidBody_ShouldReturn201() throws Exception {
        RegisterResponse response = new RegisterResponse(1L, "Carlos Mendoza", "carlos@example.com", Instant.now());
        when(authService.signUp(any())).thenReturn(response);

        String body = """
                {
                  "name": "Carlos Mendoza",
                  "email": "carlos@example.com",
                  "password": "PasswordSegura123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("carlos@example.com"));
    }

    @Test
    void register_WithInvalidEmail_ShouldReturn400() throws Exception {
        String body = """
                {
                  "name": "Carlos Mendoza",
                  "email": "no-es-un-email",
                  "password": "PasswordSegura123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("email"));
    }

    @Test
    void register_WithShortPassword_ShouldReturn400() throws Exception {
        String body = """
                {
                  "name": "Carlos Mendoza",
                  "email": "carlos@example.com",
                  "password": "1234567"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldReturn409() throws Exception {
        when(authService.signUp(any()))
                .thenThrow(new DuplicatedEmailException("El email ya está registrado: carlos@example.com"));

        String body = """
                {
                  "name": "Carlos Mendoza",
                  "email": "carlos@example.com",
                  "password": "PasswordSegura123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------

    @Test
    void login_WithValidCredentials_ShouldReturn200AndToken() throws Exception {
        LoginResponse response = new LoginResponse("fake.jwt.token", "fake.jwt.refresh",
                new LoginResponse.UserSummary(1L, "Carlos Mendoza", "carlos@example.com"));
        when(authService.authenticate(any())).thenReturn(response);

        String body = """
                {
                  "email": "carlos@example.com",
                  "password": "PasswordSegura123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake.jwt.token"))
                .andExpect(jsonPath("$.user.email").value("carlos@example.com"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturn401WithGenericMessage() throws Exception {
        when(authService.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        String body = """
                {
                  "email": "carlos@example.com",
                  "password": "incorrecta"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    void login_WithMissingFields_ShouldReturn400() throws Exception {
        String body = """
                {
                  "email": "",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}