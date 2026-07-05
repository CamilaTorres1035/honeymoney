package com.camss.honeymoney.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.camss.honeymoney.model.Expense;
import com.camss.honeymoney.model.User;

public interface ExpenseRepository extends JpaRepository<Expense, Long>{
    List<Expense> findByUser(User user);
    List<Expense> findByUserAndExpenseDateBetween(User user, LocalDate starDate, LocalDate endDate);
}
