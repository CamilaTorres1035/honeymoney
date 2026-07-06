package com.camss.honeymoney.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.camss.honeymoney.model.Category;

public record ExpenseResponse(
    Long id,
    BigDecimal amount,
    Category category,
    String description,
    LocalDate expensDate,
    Instant createdAt,
    Instant updatedAt
) {}
