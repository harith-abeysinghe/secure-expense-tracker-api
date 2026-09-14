package com.harithabeysinghe.expensetracker.user;

import com.harithabeysinghe.expensetracker.auth.CurrentUser;
import com.harithabeysinghe.expensetracker.common.dto.PageResponse;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import com.harithabeysinghe.expensetracker.user.dto.UpdateUserStatusRequest;
import com.harithabeysinghe.expensetracker.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@StandardApiErrors
public class AdminUserController {
    private final UserService service;
    private final CurrentUser currentUser;

    public AdminUserController(UserService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

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
