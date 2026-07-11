package com.camss.honeymoney.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.camss.honeymoney.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long>{
    Page<Expense> findByUserEmail(String email, Pageable pageable);
    Page<Expense> findByUserEmailAndExpenseDateBetween(String email, LocalDate starDate, LocalDate endDate, Pageable pageable);

    Optional<Expense> findByIdAndUserEmail(Long id, String email);
}
