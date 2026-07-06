package com.camss.honeymoney.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.camss.honeymoney.model.Category;

public record ExpenseUpdateRequest(
    BigDecimal amount,
    Category category,
    String description,
    LocalDate expensDate
) {}
