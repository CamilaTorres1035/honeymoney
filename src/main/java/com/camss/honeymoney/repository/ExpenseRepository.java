package com.camss.honeymoney.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.camss.honeymoney.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long>{
    List<Expense> findByUserEmail(String email);
    List<Expense> findByUserEmailAndExpenseDateBetween(String email, LocalDate starDate, LocalDate endDate);

    Optional<Expense> findByIdAndUserEmail(Long id, String email);
}
