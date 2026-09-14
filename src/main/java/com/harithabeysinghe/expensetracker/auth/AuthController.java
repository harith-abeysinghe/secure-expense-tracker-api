package com.harithabeysinghe.expensetracker.auth;

import com.harithabeysinghe.expensetracker.auth.dto.AuthResponse;
import com.harithabeysinghe.expensetracker.auth.dto.LoginRequest;
import com.harithabeysinghe.expensetracker.auth.dto.RefreshRequest;
import com.harithabeysinghe.expensetracker.auth.dto.RegisterRequest;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@SecurityRequirements
@StandardApiErrors
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a user and issue access and refresh tokens")
    AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and issue access and refresh tokens")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate a refresh token and issue a new token pair")
    AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return service.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoke the refresh-token family")
    void logout(@Valid @RequestBody RefreshRequest request) {
        service.logout(request.refreshToken());
    }
}
