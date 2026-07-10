package com.camss.honeymoney.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.camss.honeymoney.model.Category;
import com.camss.honeymoney.model.Expense;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExpenseRequest(
        @NotNull(message = "El monto es obligatorio") @Positive(message = "El monto debe ser mayor a cero") BigDecimal amount,
        @NotNull(message = "La categoria es obligatoria") Category category,
        String description, // Opcional por defecto al no tener @NotNull ni @NotBlank
        @NotNull(message = "La fecha del gasto es obligatoria") LocalDate expenseDate) {
    public Expense toEntity() {
        Expense expense = new Expense();
        expense.setAmount(this.amount());
        expense.setCategory(this.category());
        expense.setDescription(this.description());
        expense.setExpenseDate(this.expenseDate());
        return expense;
    }
}
