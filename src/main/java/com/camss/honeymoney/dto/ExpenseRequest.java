package com.camss.honeymoney.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.camss.honeymoney.model.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExpenseRequest(
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    BigDecimal amount,
    @NotBlank(message = "La categoria es obligatoria")
    Category category,
    String description,
    @NotNull(message = "La fecha del gasto es obligatoria")
    LocalDate expensDate
) {}
