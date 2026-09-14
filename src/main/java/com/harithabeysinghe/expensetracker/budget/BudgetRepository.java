package com.harithabeysinghe.expensetracker.budget;

import com.harithabeysinghe.expensetracker.budget.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {
    List<BudgetEntity> findByUserIdAndYearAndMonth(UUID userId, int year, int month);

    Optional<BudgetEntity> findByUserIdAndCategoryIdAndYearAndMonth(UUID userId, UUID categoryId, int year, int month);
}

