package com.harithabeysinghe.expensetracker.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record MonthlyReport(int year, int month, String currency, BigDecimal totalSpent, BigDecimal totalBudget,
                            BigDecimal remaining, BudgetStatus status, List<CategoryReport> categories) {}

