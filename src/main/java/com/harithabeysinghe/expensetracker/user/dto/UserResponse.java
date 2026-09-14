package com.harithabeysinghe.expensetracker.user.dto;

import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import com.harithabeysinghe.expensetracker.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, String displayName, String currency, UserRole role,
                           UserStatus status, Instant createdAt) {}

