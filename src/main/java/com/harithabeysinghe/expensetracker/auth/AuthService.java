package com.harithabeysinghe.expensetracker.auth;

import com.harithabeysinghe.expensetracker.auth.dto.*;
import com.harithabeysinghe.expensetracker.common.error.ConflictException;
import com.harithabeysinghe.expensetracker.common.error.UnauthorizedException;
import com.harithabeysinghe.expensetracker.config.AppProperties;
import com.harithabeysinghe.expensetracker.user.UserRepository;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import com.harithabeysinghe.expensetracker.user.entity.UserStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokens;
    private final AppProperties properties;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService,
                       RefreshTokenService refreshTokens, AppProperties properties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokens = refreshTokens;
        this.properties = properties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        var email = normalizeEmail(request.email());
        if (users.existsByEmailIgnoreCase(email)) throw new ConflictException("Email is already registered");
        var user = users.save(new UserEntity(email, passwordEncoder.encode(request.password()),
                request.displayName().trim(), normalizeCurrency(request.currency()), UserRole.USER));
        return response(user, refreshTokens.issue(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        var user = users.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (user.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        return response(user, refreshTokens.issue(user));
    }

    public AuthResponse refresh(String rawToken) {
        var rotation = refreshTokens.rotate(rawToken);
        return response(rotation.user(), rotation.refreshToken());
    }

    public void logout(String rawToken) { refreshTokens.revokeFamily(rawToken); }

    private AuthResponse response(UserEntity user, String refreshToken) {
        return new AuthResponse(jwtService.issue(user), refreshToken, "Bearer",
                properties.jwt().accessTokenTtl().toSeconds());
    }

    public static String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }

    public static String normalizeCurrency(String value) {
        var code = value.trim().toUpperCase(Locale.ROOT);
        try {
            return Currency.getInstance(code).getCurrencyCode();
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("currency must be a valid ISO-4217 code");
        }
    }
}
