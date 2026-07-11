package com.camss.honeymoney.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Expense expense = request.toEntity();
        
        // Optimización: Limpieza de descripción en un solo paso
        if (expense.getDescription() != null) {
            String trimmed = expense.getDescription().trim();
            expense.setDescription(trimmed.isEmpty() ? null : trimmed);
        }

        expense.setCreatedAt(Instant.now());
        expense.setUpdatedAt(Instant.now());
        expense.setUser(user);

        return new ExpenseResponse(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request, String userEmail) {
        Expense expense = expenseRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado o no autorizado"));

        if (request.amount() != null) {
            expense.setAmount(request.amount());
        }
        if (request.category() != null) {
            expense.setCategory(request.category());
        }
        if (request.description() != null) {
            String trimmed = request.description().trim();
            expense.setDescription(trimmed.isEmpty() ? null : trimmed);
        }
        if (request.expenseDate() != null) {
            expense.setExpenseDate(request.expenseDate());
        }
        
        expense.setUpdatedAt(Instant.now());
        return new ExpenseResponse(expenseRepository.save(expense));
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id, String userEmail) {
        return expenseRepository.findByIdAndUserEmail(id, userEmail)
                .map(ExpenseResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado o no autorizado"));
    }

    @Transactional(readOnly = true)
    public ExpenseListResponse findAll(String userEmail, Pageable pageable) {
        Page<Expense> expenses = expenseRepository.findByUserEmail(userEmail, pageable);
        return buildExpenseListResponse(expenses, Map.of());
    }

    @Transactional
    public void delete(Long id, String userEmail) {
        Expense expense = expenseRepository.findByIdAndUserEmail(id, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado o no autorizado"));
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public ExpenseListResponse filterExpenses(String userEmail, String range, LocalDate startDate, LocalDate endDate, Category category, Pageable pageable) {
        boolean hasRange = range != null && !range.isBlank();
        boolean hasCustomDates = startDate != null || endDate != null;

        if (hasRange && hasCustomDates) {
            throw new InvalidFilterException("Los parámetros 'range' y 'startDate'/'endDate' son mutuamente excluyentes.");
        }

        LocalDate finalStart = startDate;
        LocalDate finalEnd = endDate;

        if (hasRange) {
            finalEnd = LocalDate.now();
            finalStart = switch (range) {
                case "last_week" -> finalEnd.minusWeeks(1);
                case "last_month" -> finalEnd.minusMonths(1);
                case "last_3_months" -> finalEnd.minusMonths(3);
                default -> throw new InvalidFilterException("Rango no válido: " + range);
            };
        } else {
            // Validar que si se usa rango personalizado, ambas fechas existan
            if ((startDate == null || endDate == null) && category == null) {
                throw new InvalidFilterException("Debe proporcionar un rango válido, ambas fechas (inicio y fin) o una categoría.");
            }
            if (startDate != null && endDate == null) {
                throw new InvalidFilterException("Falta la fecha de fin (endDate).");
            }
            if (startDate == null && endDate != null) {
                throw new InvalidFilterException("Falta la fecha de inicio (startDate).");
            }
        }

        // Validación segura de orden de fechas
        if (finalStart != null && finalEnd != null && finalStart.isAfter(finalEnd)) {
            throw new InvalidFilterException("La fecha de inicio no puede ser posterior a la fecha fin.");
        }

        // Consulta optimizada delegando lógica al repositorio
        Page<Expense> expenses;
        if (category != null && finalStart != null && finalEnd != null) {
            expenses = expenseRepository.findByUserEmailAndCategoryAndExpenseDateBetween(userEmail, category, finalStart, finalEnd, pageable);
        } else if (category != null) {
            expenses = expenseRepository.findByUserEmailAndCategory(userEmail, category, pageable);
        } else {
            expenses = expenseRepository.findByUserEmailAndExpenseDateBetween(userEmail, finalStart, finalEnd, pageable);
        }

        Map<String, Object> metaData = Map.of(
            "range", hasRange ? range : "custom",
            "startDate", finalStart != null ? finalStart : "N/A",
            "endDate", finalEnd != null ? finalEnd : "N/A",
            "category", category != null ? category : "N/A"
        );

        return buildExpenseListResponse(expenses, metaData);
    }

    // Helper metod para evitar duplicación de código al construir respuestas paginadas
    private ExpenseListResponse buildExpenseListResponse(Page<Expense> expenses, Map<String, Object> extraMeta) {
        List<ExpenseResponse> data = expenses.stream().map(ExpenseResponse::new).toList();
        ExpenseListResponse.Meta meta = new ExpenseListResponse.Meta(
            expenses.getTotalElements(), 
            expenses.getTotalPages(), 
            expenses.getNumber(), 
            expenses.getSize(), 
            expenses.isLast(), 
            extraMeta
        );
        return new ExpenseListResponse(data, meta);
    }
}
