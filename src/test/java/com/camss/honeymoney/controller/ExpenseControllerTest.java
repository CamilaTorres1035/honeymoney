package com.camss.honeymoney.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.camss.honeymoney.dto.ExpenseListResponse;
import com.camss.honeymoney.dto.ExpenseResponse;
import com.camss.honeymoney.exception.InvalidFilterException;
import com.camss.honeymoney.exception.ResourceNotFoundException;
import com.camss.honeymoney.model.Category;
import com.camss.honeymoney.model.User;
import com.camss.honeymoney.repository.UserRepository;
import com.camss.honeymoney.security.JwtService;
import com.camss.honeymoney.service.ExpenseService;

/**
 * Tests de integración de ExpenseController.
 * Se usa el contexto completo de Spring (SecurityConfig, JwtAuthenticationFilter,
 * GlobalExceptionHandler reales) mockeando solo la capa de servicio y el repositorio
 * de usuarios (usado internamente por UserDetailsServiceImpl para validar el JWT).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private UserRepository userRepository;

    private final String email = "test@example.com";
    private String token;
    private ExpenseResponse sampleResponse;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword("hashedPassword");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails principal = new org.springframework.security.core.userdetails.User(
                email, "hashedPassword", Collections.emptyList());
        token = jwtService.generateToken(principal);

        sampleResponse = new ExpenseResponse(
                1L, new BigDecimal("45.50"), Category.Groceries, "Cena de negocios",
                LocalDate.of(2026, 7, 3), Instant.now(), Instant.now());
    }

    // ---------------------------------------------------------------
    // Autenticación
    // ---------------------------------------------------------------

    @Test
    void getExpenses_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getExpenses_WithMalformedToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/expenses").header("Authorization", "Bearer token.invalido.aca"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------
    // Crear gasto
    // ---------------------------------------------------------------

    @Test
    void createExpense_WithValidBodyAndToken_ShouldReturn201() throws Exception {
        when(expenseService.create(any(), eq(email))).thenReturn(sampleResponse);

        String body = """
                {
                  "amount": 45.50,
                  "category": "Groceries",
                  "description": "Cena de negocios",
                  "expenseDate": "2026-07-03"
                }
                """;

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(45.50))
                .andExpect(jsonPath("$.category").value("Groceries"));
    }

    @Test
    void createExpense_WithNegativeAmount_ShouldReturn400WithValidationDetails() throws Exception {
        String body = """
                {
                  "amount": -10,
                  "category": "Groceries",
                  "expenseDate": "2026-07-03"
                }
                """;

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details[0].field").value("amount"));
    }

    @Test
    void createExpense_WithMissingCategory_ShouldReturn400() throws Exception {
        String body = """
                {
                  "amount": 10,
                  "expenseDate": "2026-07-03"
                }
                """;

        mockMvc.perform(post("/api/expenses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // Obtener gasto específico
    // ---------------------------------------------------------------

    @Test
    void getExpenseById_WhenExists_ShouldReturn200() throws Exception {
        when(expenseService.findById(1L, email)).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/expenses/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getExpenseById_WhenNotFoundOrNotOwned_ShouldReturn404() throws Exception {
        when(expenseService.findById(99L, email))
                .thenThrow(new ResourceNotFoundException("Gasto no encontrado o no autorizado"));

        mockMvc.perform(get("/api/expenses/99").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ---------------------------------------------------------------
    // Listado y filtros
    // ---------------------------------------------------------------

    @Test
    void getExpenses_WithoutFilters_ShouldCallFindAll() throws Exception {
        ExpenseListResponse listResponse = new ExpenseListResponse(
                List.of(sampleResponse), new ExpenseListResponse.Meta(1, Map.of()));
        when(expenseService.findAll(email)).thenReturn(listResponse);

        mockMvc.perform(get("/api/expenses").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.totalCount").value(1));
    }

    @Test
    void getExpenses_WithRangeFilter_ShouldCallFilterExpenses() throws Exception {
        ExpenseListResponse listResponse = new ExpenseListResponse(
                List.of(sampleResponse), new ExpenseListResponse.Meta(1, Map.of("range", "last_week")));
        when(expenseService.filterExpenses(eq(email), eq("last_week"), eq(null), eq(null)))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/expenses?range=last_week").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.appliedFilters.range").value("last_week"));
    }

    @Test
    void getExpenses_WhenServiceThrowsInvalidFilterException_ShouldReturn400() throws Exception {
        // Simula la regla de exclusión mutua evaluada en el service
        when(expenseService.filterExpenses(anyString(), eq("last_week"), any(), any()))
                .thenThrow(new InvalidFilterException(
                        "Los parámetros 'range' y 'startDate'/'endDate' son mutuamente excluyentes. Use solo uno de los dos."));

        mockMvc.perform(get("/api/expenses?range=last_week&startDate=2026-06-01")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("mutuamente excluyentes")));
    }

    @Test
    void getExpenses_WithMalformedDateFormat_ShouldReturn400() throws Exception {
        // startDate con formato inválido (dd-MM-yyyy en vez de yyyy-MM-dd): falla en el
        // binding del parámetro, antes de llegar al service. Cubre el fix de
        // MethodArgumentTypeMismatchException en GlobalExceptionHandler.
        mockMvc.perform(get("/api/expenses?startDate=04-07-2026&endDate=2026-07-10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("startDate")));
    }

    // ---------------------------------------------------------------
    // Actualizar gasto
    // ---------------------------------------------------------------

    @Test
    void updateExpense_WithValidPartialBody_ShouldReturn200() throws Exception {
        ExpenseResponse updated = new ExpenseResponse(
                1L, new BigDecimal("50.00"), Category.Groceries, "Ajuste de propina",
                LocalDate.of(2026, 7, 3), Instant.now(), Instant.now());
        when(expenseService.update(eq(1L), any(), eq(email))).thenReturn(updated);

        String body = """
                {
                  "amount": 50.00,
                  "description": "Ajuste de propina"
                }
                """;

        mockMvc.perform(patch("/api/expenses/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void updateExpense_WhenNotFound_ShouldReturn404() throws Exception {
        when(expenseService.update(eq(99L), any(), eq(email)))
                .thenThrow(new ResourceNotFoundException("Gasto no encontrado o no autorizado"));

        mockMvc.perform(patch("/api/expenses/99")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------
    // Eliminar gasto
    // ---------------------------------------------------------------

    @Test
    void deleteExpense_WhenExists_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/expenses/1").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteExpense_WithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/expenses/1"))
                .andExpect(status().isUnauthorized());
    }
}