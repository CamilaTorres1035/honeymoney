package com.camss.honeymoney.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.camss.honeymoney.model.Category;
import jakarta.validation.constraints.Positive;

public record ExpenseUpdateRequest(
    @Positive(message = "El monto debe ser mayor a cero")
    BigDecimal amount,
    Category category,
    String description, // Cambiado de Optional<String> a String
    LocalDate expenseDate
) {}
