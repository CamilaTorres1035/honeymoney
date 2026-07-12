package com.camss.honeymoney.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.camss.honeymoney.dto.ExpenseListResponse;
import com.camss.honeymoney.dto.ExpenseRequest;
import com.camss.honeymoney.dto.ExpenseResponse;
import com.camss.honeymoney.dto.ExpenseUpdateRequest;
import com.camss.honeymoney.exception.InvalidFilterException;
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
    private Pageable pageable;
    private final String email = "test@example.com";
    private final Long expenseId = 1L;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setEmail(email);

        sampleExpense = new Expense();
        sampleExpense.setId(expenseId);
        sampleExpense.setAmount(new BigDecimal("100.0"));
        sampleExpense.setCategory(Category.Groceries);
        sampleExpense.setDescription("Lunch");
        sampleExpense.setUser(sampleUser);

        pageable = PageRequest.of(0, 10);
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
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(new BigDecimal("150.0"), Category.Utilities, "Taxi", LocalDate.now());

        when(expenseRepository.findByIdAndUserEmail(expenseId, email)).thenReturn(Optional.of(sampleExpense));
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
        when(expenseRepository.findByUserEmail(email, pageable))
                .thenReturn(new PageImpl<>(List.of(sampleExpense), pageable, 1));

        ExpenseListResponse response = expenseService.findAll(email, pageable);

        assertNotNull(response);
        assertEquals(1, response.meta().totalCount());
        assertEquals(1, response.data().size());
        verify(expenseRepository, times(1)).findByUserEmail(email, pageable);
    }

    @Test
    void delete_WhenExpenseExists_ShouldDeleteSuccessfully() {
        when(expenseRepository.findByIdAndUserEmail(expenseId, email)).thenReturn(Optional.of(sampleExpense));

        assertDoesNotThrow(() -> expenseService.delete(expenseId, email));
        verify(expenseRepository, times(1)).delete(sampleExpense);
    }

    @Test
    void delete_WhenExpenseDoesNotExist_ShouldThrowResourceNotFoundException() {
        when(expenseRepository.findByIdAndUserEmail(expenseId, email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> expenseService.delete(expenseId, email));
        verify(expenseRepository, never()).delete(any(Expense.class));
    }

    // ---------------------------------------------------------------------
    // filterExpenses() — cobertura de la regla de exclusión mutua (bug fix)
    // ---------------------------------------------------------------------

    @Test
    void filterExpenses_WhenRangeAndStartDateProvidedTogether_ShouldThrowInvalidFilterException() {
        assertThrows(InvalidFilterException.class,
                () -> expenseService.filterExpenses(email, "last_week", LocalDate.of(2026, 6, 1), null,null, pageable));

        verify(expenseRepository, never()).findByUserEmailAndExpenseDateBetween(any(), any(), any(), any());
    }

    @Test
    void filterExpenses_WhenRangeAndEndDateProvidedTogether_ShouldThrowInvalidFilterException() {
        assertThrows(InvalidFilterException.class,
                () -> expenseService.filterExpenses(email, "last_month", null, LocalDate.of(2026, 7, 4),null, pageable));

        verify(expenseRepository, never()).findByUserEmailAndExpenseDateBetween(any(), any(), any(), any());
    }

    @Test
    void filterExpenses_WhenRangeAndBothCustomDatesProvided_ShouldThrowInvalidFilterException() {
        assertThrows(InvalidFilterException.class,
                () -> expenseService.filterExpenses(email, "last_3_months",
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 4),null, pageable));

        verify(expenseRepository, never()).findByUserEmailAndExpenseDateBetween(any(), any(), any(), any());
    }

    @Test
    void filterExpenses_WhenOnlyRangeProvided_ShouldQueryRepository() {
        when(expenseRepository.findByUserEmailAndExpenseDateBetween(eq(email), any(), any(), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(sampleExpense), pageable, 1));

        ExpenseListResponse response = expenseService.filterExpenses(email, "last_week", null, null,null, pageable);

        assertNotNull(response);
        assertEquals(1, response.meta().totalCount());
        assertEquals("last_week", response.meta().appliedFilters().get("range"));
        verify(expenseRepository, times(1)).findByUserEmailAndExpenseDateBetween(eq(email), any(), any(), eq(pageable));
    }

    @Test
    void filterExpenses_WhenOnlyCustomDatesProvided_ShouldQueryRepository() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 7, 4);
        when(expenseRepository.findByUserEmailAndExpenseDateBetween(email, start, end, pageable))
            .thenReturn(new PageImpl<>(List.of(sampleExpense), pageable, 1));

        ExpenseListResponse response = expenseService.filterExpenses(email, null, start, end,null, pageable);

        assertNotNull(response);
        assertEquals("custom", response.meta().appliedFilters().get("range"));
        verify(expenseRepository, times(1)).findByUserEmailAndExpenseDateBetween(email, start, end, pageable);
    }

    @Test
    void filterExpenses_WhenNoFiltersProvided_ShouldThrowInvalidFilterException() {
        assertThrows(InvalidFilterException.class,
                () -> expenseService.filterExpenses(email, null, null, null,null, pageable));
    }

    @Test
    void filterExpenses_WhenRangeIsUnknown_ShouldThrowInvalidFilterException() {
        assertThrows(InvalidFilterException.class,
                () -> expenseService.filterExpenses(email, "last_year", null, null,null, pageable));
    }

    @Test
    void filterExpenses_WhenStartDateAfterEndDate_ShouldThrowInvalidFilterException() {
        LocalDate start = LocalDate.of(2026, 7, 10);
        LocalDate end = LocalDate.of(2026, 7, 1);

        assertThrows(InvalidFilterException.class,
            () -> expenseService.filterExpenses(email, null, start, end,null, pageable));

        verify(expenseRepository, never()).findByUserEmailAndExpenseDateBetween(any(), any(), any(), any());
    }
}