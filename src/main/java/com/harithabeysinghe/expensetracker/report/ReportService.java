package com.harithabeysinghe.expensetracker.report;

import com.harithabeysinghe.expensetracker.budget.BudgetRepository;
import com.harithabeysinghe.expensetracker.budget.BudgetService;
import com.harithabeysinghe.expensetracker.expense.ExpenseRepository;
import com.harithabeysinghe.expensetracker.report.dto.BudgetStatus;
import com.harithabeysinghe.expensetracker.report.dto.CategoryReport;
import com.harithabeysinghe.expensetracker.report.dto.MonthlyReport;
import com.harithabeysinghe.expensetracker.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ExpenseRepository expenses;
    private final BudgetRepository budgets;
    private final UserService users;

    @Transactional(readOnly = true)
    public MonthlyReport monthly(UUID userId, int year, int month) {
        BudgetService.validateMonth(year, month);
        var yearMonth = YearMonth.of(year, month);
        var rows = new LinkedHashMap<UUID, MutableCategory>();

        expenses.findByUserIdAndExpenseDateBetween(userId, yearMonth.atDay(1), yearMonth.atEndOfMonth())
                .forEach(expense -> rows.computeIfAbsent(expense.getCategory().getId(), id ->
                        new MutableCategory(id, expense.getCategory().getName()))
                        .spent = rows.get(expense.getCategory().getId()).spent.add(expense.getAmount()));

        budgets.findByUserIdAndYearAndMonth(userId, year, month)
                .forEach(budget -> rows.computeIfAbsent(budget.getCategory().getId(), id ->
                        new MutableCategory(id, budget.getCategory().getName()))
                        .budget = budget.getAmount());

        var categoryReports = rows.values().stream()
                .sorted(Comparator.comparing(row -> row.name))
                .map(MutableCategory::toReport)
                .toList();
        var totalSpent = categoryReports.stream().map(CategoryReport::spent).reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalBudget = categoryReports.stream().map(CategoryReport::budget).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var remaining = totalBudget.subtract(totalSpent);
        var hasBudget = categoryReports.stream().anyMatch(report -> report.budget() != null);
        var status = !hasBudget ? BudgetStatus.NO_BUDGET
                : remaining.signum() < 0 ? BudgetStatus.OVER_BUDGET : BudgetStatus.WITHIN_BUDGET;
        return new MonthlyReport(year, month, users.get(userId).currency(), totalSpent, totalBudget, remaining,
                status, categoryReports);
    }

    private static final class MutableCategory {
        private final UUID id;
        private final String name;
        private BigDecimal spent = BigDecimal.ZERO;
        private BigDecimal budget;

        private MutableCategory(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        private CategoryReport toReport() {
            if (budget == null) return new CategoryReport(id, name, spent, null, null, BudgetStatus.NO_BUDGET);
            var remaining = budget.subtract(spent);
            var status = remaining.signum() < 0 ? BudgetStatus.OVER_BUDGET : BudgetStatus.WITHIN_BUDGET;
            return new CategoryReport(id, name, spent, budget, remaining, status);
        }
    }
}
