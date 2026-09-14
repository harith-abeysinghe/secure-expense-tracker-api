package com.harithabeysinghe.expensetracker.user;

import com.harithabeysinghe.expensetracker.auth.CurrentUser;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import com.harithabeysinghe.expensetracker.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@StandardApiErrors
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final CurrentUser currentUser;

    @GetMapping("/me")
    @Operation(summary = "Get the authenticated user's profile")
    UserResponse me(Authentication authentication) {
        return service.get(currentUser.id(authentication));
    }
}
