package com.camss.honeymoney.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.camss.honeymoney.dto.ExpenseListResponse;
import com.camss.honeymoney.dto.ExpenseRequest;
import com.camss.honeymoney.dto.ExpenseResponse;
import com.camss.honeymoney.dto.ExpenseUpdateRequest;
import com.camss.honeymoney.exception.InvalidFilterException;
import com.camss.honeymoney.exception.ResourceNotFoundException;
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
    public ExpenseResponse create(ExpenseRequest request, String userEmail){
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        Expense expense = request.toEntity();
        if (expense.getDescription() != null) {
            String trimmed = expense.getDescription().trim();
            expense.setDescription(trimmed.isEmpty() ? null : trimmed);
        }
        expense.setCreatedAt(Instant.now());
        expense.setUpdatedAt(Instant.now());
        expense.setUser(user);

        Expense savedExpense = expenseRepository.save(expense);

        return new ExpenseResponse(savedExpense);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest request, String userEmail){
        Expense expense = expenseRepository.findByIdAndUserEmail(id, userEmail).orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado o no autorizado"));

        if (request.amount() != null){expense.setAmount(request.amount());}
        if (request.category() != null){expense.setCategory(request.category());}
        if (request.description() != null) {
            String trimmed = request.description().trim();
            expense.setDescription(trimmed.isEmpty() ? null : trimmed);
        }
        if (request.expenseDate() != null){expense.setExpenseDate(request.expenseDate());}

        expense.setUpdatedAt(Instant.now());

        Expense updatedExpense = expenseRepository.save(expense);

        return new ExpenseResponse(updatedExpense);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id, String userEmail){
        Expense expense = expenseRepository.findByIdAndUserEmail(id, userEmail).orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado o no autorizado"));
        return new ExpenseResponse(expense);
    }

    @Transactional(readOnly=true)
    public ExpenseListResponse findAll(String userEmail){
        List<Expense> expenses = expenseRepository.findByUserEmail(userEmail);
        List<ExpenseResponse> data = expenses.stream().map(ExpenseResponse::new).toList();

        ExpenseListResponse.Meta meta = new ExpenseListResponse.Meta(data.size(), Map.of());

        return new ExpenseListResponse(data, meta);
    }

    @Transactional
    public void delete(Long id, String userEmail){
        Expense expense = expenseRepository.findByIdAndUserEmail(id, userEmail).orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado o no autorizado"));
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly=true)
    public ExpenseListResponse filterExpenses(String userEmail, String range, LocalDate startDate, LocalDate endDate){
        LocalDate finalStart = startDate;
        LocalDate finalEnd = endDate;

        if (range != null && !range.isBlank()){
            finalEnd = LocalDate.now();
            finalStart = switch (range) {
                case "last_week" -> finalEnd.minusWeeks(1);
                case "last_month" -> finalEnd.minusMonths(1);
                case "last_3_months" -> finalEnd.minusMonths(3);
                default -> throw new InvalidFilterException("Rango no válido: " + range);
            };
        }

        else if (finalStart == null || finalEnd == null) {
            throw new InvalidFilterException("Debe proporcionar un rango válido o ambas fechas (inicio y fin).");
        }

        if (finalStart.isAfter(finalEnd)) {
            throw new InvalidFilterException("La fecha de inicio no puede ser posterior a la fecha fin.");
        }

        List<Expense> expenses = expenseRepository.findByUserEmailAndExpenseDateBetween(userEmail, finalStart, finalEnd);
        List<ExpenseResponse> data = expenses.stream().map(ExpenseResponse::new).toList();

        ExpenseListResponse.Meta meta = new ExpenseListResponse.Meta(expenses.size(), Map.of("range", range != null ? range : "custom", "startDate", finalStart, "endDate", finalEnd));

        return new ExpenseListResponse(data, meta);
    }
}
