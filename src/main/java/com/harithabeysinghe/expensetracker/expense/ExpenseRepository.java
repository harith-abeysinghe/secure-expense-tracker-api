package com.harithabeysinghe.expensetracker.expense;

import com.harithabeysinghe.expensetracker.expense.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, UUID>, JpaSpecificationExecutor<ExpenseEntity> {
    Optional<ExpenseEntity> findByIdAndUserId(UUID id, UUID userId);

    List<ExpenseEntity> findByUserIdAndExpenseDateBetween(UUID userId, LocalDate from, LocalDate to);
}

