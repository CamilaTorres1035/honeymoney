package com.camss.honeymoney.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.camss.honeymoney.dto.ExpenseListResponse;
import com.camss.honeymoney.dto.ExpenseRequest;
import com.camss.honeymoney.dto.ExpenseResponse;
import com.camss.honeymoney.dto.ExpenseUpdateRequest;
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

    public ExpenseResponse create(ExpenseRequest request, String userEmail){
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        Expense expense = request.toEntity();
        expense.setCreatedAt(Instant.now());
        expense.setUpdatedAt(Instant.now());
        expense.setUser(user);

        Expense savedExpense = expenseRepository.save(expense);

        return new ExpenseResponse(savedExpense);
    }

    public ExpenseResponse update(Long id, ExpenseUpdateRequest request, String userEmail){
        Expense expense = expenseRepository.findByIdAndUserEmail(id, userEmail).orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado o no autorizado"));

        if (request.amount() != null){expense.setAmount(request.amount());}
        if (request.category() != null){expense.setCategory(request.category());}
        if (request.description() != null){expense.setDescription(request.description());}
        if (request.expenseDate() != null){expense.setExpenseDate(request.expenseDate());}

        expense.setUpdatedAt(Instant.now());

        return new ExpenseResponse(expense);
    }

    public ExpenseResponse findById(Long id, String userEmail){
        Expense expense = expenseRepository.findByIdAndUserEmail(id, userEmail).orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado o no autorizado"));
        return new ExpenseResponse(expense);
    }

    public ExpenseListResponse findAll(String userEmail){
        List<Expense> expenses = expenseRepository.findByUserEmail(userEmail);
        List<ExpenseResponse> data = expenses.stream().map(ExpenseResponse::new).toList();

        ExpenseListResponse.Meta meta = new ExpenseListResponse.Meta(data.size(), Map.of("userEmail", userEmail));

        return new ExpenseListResponse(data, meta);
    }

    public void delete(Long id, String userEmail){
        Expense expense = expenseRepository.findByIdAndUserEmail(id, userEmail).orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado o no autorizado"));
        expenseRepository.delete(expense);
    }
}
