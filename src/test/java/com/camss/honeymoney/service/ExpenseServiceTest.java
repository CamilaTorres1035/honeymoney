package com.camss.honeymoney.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.camss.honeymoney.dto.ExpenseListResponse;
import com.camss.honeymoney.dto.ExpenseRequest;
import com.camss.honeymoney.dto.ExpenseResponse;
import com.camss.honeymoney.dto.ExpenseUpdateRequest;
import com.camss.honeymoney.exception.ResourceNotFoundException;
import com.camss.honeymoney.model.Category;
import com.camss.honeymoney.model.Expense;
import com.camss.honeymoney.model.User;
import com.camss.honeymoney.repository.ExpenseRepository;
import com.camss.honeymoney.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User sampleUser;
    private Expense sampleExpense;
    private final String email = "test@example.com";
    private final Long expenseId = 1L;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setEmail(email);

        sampleExpense = new Expense();
        sampleExpense.setId(expenseId);
        sampleExpense.setAmount(new BigDecimal("100.0"));
        // Asumiendo que tu enum Category tiene constantes en MAYÚSCULAS convencionales
        sampleExpense.setCategory(Category.Groceries); 
        sampleExpense.setDescription("Lunch");
        sampleExpense.setUser(sampleUser);
    }

    @Test
    void create_WhenUserExists_ShouldReturnExpenseResponse() {
        ExpenseRequest request = mock(ExpenseRequest.class);
        when(request.toEntity()).thenReturn(new Expense());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(sampleUser));
        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);

        ExpenseResponse response = expenseService.create(request, email);

        assertNotNull(response);
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test
    void create_WhenUserDoesNotExist_ShouldThrowUsernameNotFoundException() {
        ExpenseRequest request = mock(ExpenseRequest.class);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> expenseService.create(request, email));
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void update_WhenExpenseExists_ShouldUpdateFieldsAndReturnResponse() {
        // FIX: Se usan tipos correctos (Category.RENT y LocalDate.now()) acorde al record
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(new BigDecimal("150.0"), Category.Utilities, "Taxi", LocalDate.now());
        
        when(expenseRepository.findByIdAndUserEmail(expenseId, email)).thenReturn(Optional.of(sampleExpense));
        // FIX: Se mockea el save para evitar NullPointerException por el cambio en el Service
        when(expenseRepository.save(any(Expense.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExpenseResponse response = expenseService.update(expenseId, request, email);

        assertNotNull(response);
        assertEquals(new BigDecimal("150.0"), sampleExpense.getAmount());
        assertEquals(Category.Utilities, sampleExpense.getCategory());
    }

    @Test
    void update_WhenExpenseDoesNotExist_ShouldThrowResourceNotFoundException() {
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(null, null, null, null);
        when(expenseRepository.findByIdAndUserEmail(expenseId, email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.update(expenseId, request, email));
    }

    @Test
    void findById_WhenExpenseExists_ShouldReturnExpenseResponse() {
        when(expenseRepository.findByIdAndUserEmail(expenseId, email)).thenReturn(Optional.of(sampleExpense));

        ExpenseResponse response = expenseService.findById(expenseId, email);

        assertNotNull(response);
        verify(expenseRepository, times(1)).findByIdAndUserEmail(expenseId, email);
    }

    @Test
    void findById_WhenExpenseDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(expenseRepository.findByIdAndUserEmail(expenseId, email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.findById(expenseId, email));
    }

    @Test
    void findAll_ShouldReturnExpenseListResponse() {
        // FIX: Agregado el uso correcto de List.of de java.util
        when(expenseRepository.findByUserEmail(email)).thenReturn(List.of(sampleExpense));

        ExpenseListResponse response = expenseService.findAll(email);

        assertNotNull(response);
        // Nota: Asegúrate de que tus Records/DTOs ExpenseListResponse tengan estos métodos expuestos
        assertNotNull(response.data()); 
    }

    @Test
    void delete_WhenExpenseExists_ShouldDeleteSuccessfully() {
        when(expenseRepository.findByIdAndUserEmail(expenseId, email)).thenReturn(Optional.of(sampleExpense));

        // FIX: Agregada la aserción correcta importada de Junit 5
        assertDoesNotThrow(() -> expenseService.delete(expenseId, email));
        verify(expenseRepository, times(1)).delete(sampleExpense);
    }

    @Test
    void delete_WhenExpenseDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(expenseRepository.findByIdAndUserEmail(expenseId, email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.delete(expenseId, email));
        verify(expenseRepository, never()).delete(any(Expense.class));
    }
}
