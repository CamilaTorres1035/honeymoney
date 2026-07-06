package com.camss.honeymoney.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.camss.honeymoney.model.Category;
import com.camss.honeymoney.model.Expense;

public record ExpenseResponse(
    Long id,
    BigDecimal amount,
    Category category,
    String description,
    LocalDate expensDate,
    Instant createdAt,
    Instant updatedAt
) {
    // Constructor compacto de mapeo
    public ExpenseResponse(Expense expense) {
        this(
            expense.getId(), 
            expense.getAmount(), 
            expense.getCategory(), 
            expense.getDescription(), 
            expense.getExpenseDate(), 
            expense.getCreatedAt(), 
            expense.getUpdatedAt()
        );
    }
}
