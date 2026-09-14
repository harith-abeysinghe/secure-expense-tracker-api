package com.harithabeysinghe.expensetracker.report;

import com.harithabeysinghe.expensetracker.budget.BudgetRepository;
import com.harithabeysinghe.expensetracker.budget.entity.BudgetEntity;
import com.harithabeysinghe.expensetracker.category.entity.CategoryEntity;
import com.harithabeysinghe.expensetracker.expense.ExpenseRepository;
import com.harithabeysinghe.expensetracker.expense.entity.ExpenseEntity;
import com.harithabeysinghe.expensetracker.report.dto.BudgetStatus;
import com.harithabeysinghe.expensetracker.user.UserService;
import com.harithabeysinghe.expensetracker.user.dto.UserResponse;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import com.harithabeysinghe.expensetracker.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {
    @Mock ExpenseRepository expenses;
    @Mock BudgetRepository budgets;
    @Mock UserService users;
    ReportService service;
    UserEntity user;
    CategoryEntity groceries;

    @BeforeEach
    void setUp() {
        service = new ReportService(expenses, budgets, users);
        user = new UserEntity("user@example.com", "hash", "User", "USD", UserRole.USER);
        groceries = new CategoryEntity(null, "Groceries");
        when(users.get(user.getId())).thenReturn(new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                "USD", UserRole.USER, UserStatus.ACTIVE, null));
    }

    @Test
    void reportsWithinAndOverBudget() {
        when(expenses.findByUserIdAndExpenseDateBetween(eq(user.getId()), any(), any())).thenReturn(List.of(
                new ExpenseEntity(user, groceries, new BigDecimal("50.00"), LocalDate.of(2026, 9, 2), null)));
        when(budgets.findByUserIdAndYearAndMonth(user.getId(), 2026, 9)).thenReturn(List.of(
                new BudgetEntity(user, groceries, 2026, 9, new BigDecimal("100.00"))));
        var within = service.monthly(user.getId(), 2026, 9);
        assertEquals(BudgetStatus.WITHIN_BUDGET, within.status());
        assertEquals(new BigDecimal("50.00"), within.remaining());

        when(budgets.findByUserIdAndYearAndMonth(user.getId(), 2026, 9)).thenReturn(List.of(
                new BudgetEntity(user, groceries, 2026, 9, new BigDecimal("25.00"))));
        assertEquals(BudgetStatus.OVER_BUDGET, service.monthly(user.getId(), 2026, 9).status());
    }

    @Test
    void reportsUnbudgetedAndEmptyMonths() {
        when(expenses.findByUserIdAndExpenseDateBetween(eq(user.getId()), any(), any())).thenReturn(List.of(
                new ExpenseEntity(user, groceries, new BigDecimal("10.00"), LocalDate.of(2026, 9, 2), null)));
        when(budgets.findByUserIdAndYearAndMonth(user.getId(), 2026, 9)).thenReturn(List.of());
        var unbudgeted = service.monthly(user.getId(), 2026, 9);
        assertEquals(BudgetStatus.NO_BUDGET, unbudgeted.status());
        assertNull(unbudgeted.categories().getFirst().budget());

        when(expenses.findByUserIdAndExpenseDateBetween(eq(user.getId()), any(), any())).thenReturn(List.of());
        var empty = service.monthly(user.getId(), 2026, 9);
        assertTrue(empty.categories().isEmpty());
        assertEquals(BigDecimal.ZERO, empty.totalSpent());
    }

    @Test
    void treatsAnExplicitZeroBudgetAsABudget() {
        when(expenses.findByUserIdAndExpenseDateBetween(eq(user.getId()), any(), any())).thenReturn(List.of(
                new ExpenseEntity(user, groceries, BigDecimal.ONE, LocalDate.of(2026, 9, 2), null)));
        when(budgets.findByUserIdAndYearAndMonth(user.getId(), 2026, 9)).thenReturn(List.of(
                new BudgetEntity(user, groceries, 2026, 9, BigDecimal.ZERO)));
        assertEquals(BudgetStatus.OVER_BUDGET, service.monthly(user.getId(), 2026, 9).status());
    }
}
