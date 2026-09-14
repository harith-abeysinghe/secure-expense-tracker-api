package com.harithabeysinghe.expensetracker.expense.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(UUID id, UUID categoryId, String categoryName, BigDecimal amount,
                              LocalDate expenseDate, String description, Instant createdAt, Instant updatedAt) {}

