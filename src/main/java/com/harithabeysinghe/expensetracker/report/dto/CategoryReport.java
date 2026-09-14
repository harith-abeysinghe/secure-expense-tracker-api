package com.harithabeysinghe.expensetracker.report.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryReport(UUID categoryId, String categoryName, BigDecimal spent, BigDecimal budget,
                             BigDecimal remaining, BudgetStatus status) {
}

