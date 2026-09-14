package com.harithabeysinghe.expensetracker.user;

import com.harithabeysinghe.expensetracker.auth.CurrentUser;
import com.harithabeysinghe.expensetracker.common.dto.PageResponse;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import com.harithabeysinghe.expensetracker.user.dto.UpdateUserStatusRequest;
import com.harithabeysinghe.expensetracker.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@StandardApiErrors
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService service;
    private final CurrentUser currentUser;

    @GetMapping
    @Operation(summary = "List user accounts")
    PageResponse<UserResponse> list(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Enable or disable a user account")
    UserResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateUserStatusRequest request,
                              Authentication authentication) {
        return service.updateStatus(currentUser.id(authentication), id, request.status());
    }
}
