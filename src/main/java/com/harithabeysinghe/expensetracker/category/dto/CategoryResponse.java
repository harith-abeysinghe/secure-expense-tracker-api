package com.harithabeysinghe.expensetracker.category.dto;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, boolean global, boolean archived) {}

