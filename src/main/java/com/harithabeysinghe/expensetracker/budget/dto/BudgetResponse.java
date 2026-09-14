package com.harithabeysinghe.expensetracker.budget.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponse(UUID id, UUID categoryId, String categoryName, int year, int month, BigDecimal amount) {}

