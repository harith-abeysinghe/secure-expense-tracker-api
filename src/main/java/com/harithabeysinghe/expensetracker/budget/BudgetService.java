package com.harithabeysinghe.expensetracker.budget;

import com.harithabeysinghe.expensetracker.budget.dto.BudgetResponse;
import com.harithabeysinghe.expensetracker.budget.entity.BudgetEntity;
import com.harithabeysinghe.expensetracker.category.CategoryService;
import com.harithabeysinghe.expensetracker.common.error.NotFoundException;
import com.harithabeysinghe.expensetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgets;
    private final UserService users;
    private final CategoryService categories;

    public static void validateMonth(int year, int month) {
        if (year < 2000 || year > 2200) throw new IllegalArgumentException("year must be between 2000 and 2200");
        try {
            YearMonth.of(year, month);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> list(UUID userId, int year, int month) {
        validateMonth(year, month);
        return budgets.findByUserIdAndYearAndMonth(userId, year, month).stream()
                .sorted(Comparator.comparing(budget -> budget.getCategory().getName()))
                .map(this::map).toList();
    }

    @Transactional
    public BudgetResponse upsert(UUID userId, UUID categoryId, int year, int month, BigDecimal amount) {
        validateMonth(year, month);
        var category = categories.requireActiveVisible(userId, categoryId);
        var budget = budgets.findByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, year, month)
                .map(existing -> {
                    existing.setAmount(amount);
                    return existing;
                })
                .orElseGet(() -> new BudgetEntity(users.require(userId), category, year, month, amount));
        return map(budgets.save(budget));
    }

    @Transactional
    public void delete(UUID userId, UUID categoryId, int year, int month) {
        validateMonth(year, month);
        var budget = budgets.findByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, year, month)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
        budgets.delete(budget);
    }

    private BudgetResponse map(BudgetEntity budget) {
        return new BudgetResponse(budget.getId(), budget.getCategory().getId(), budget.getCategory().getName(),
                budget.getYear(), budget.getMonth(), budget.getAmount());
    }
}
