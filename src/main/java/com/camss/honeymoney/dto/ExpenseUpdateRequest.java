package com.camss.honeymoney.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import com.camss.honeymoney.model.Category;

import jakarta.validation.constraints.Positive;

public record ExpenseUpdateRequest(
    @Positive(message = "El monto debe ser mayor a cero")
    BigDecimal amount,
    Category category,
    Optional<String> description,
    LocalDate expenseDate
) {}
