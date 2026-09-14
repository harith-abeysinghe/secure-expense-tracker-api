package com.harithabeysinghe.expensetracker.user.dto;

import com.harithabeysinghe.expensetracker.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(@NotNull UserStatus status) {}

