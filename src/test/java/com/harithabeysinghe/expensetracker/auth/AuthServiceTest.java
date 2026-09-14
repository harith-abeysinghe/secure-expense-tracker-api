package com.harithabeysinghe.expensetracker.auth;

import com.harithabeysinghe.expensetracker.auth.dto.LoginRequest;
import com.harithabeysinghe.expensetracker.auth.dto.RegisterRequest;
import com.harithabeysinghe.expensetracker.common.error.ConflictException;
import com.harithabeysinghe.expensetracker.common.error.UnauthorizedException;
import com.harithabeysinghe.expensetracker.config.AppProperties;
import com.harithabeysinghe.expensetracker.user.UserRepository;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import com.harithabeysinghe.expensetracker.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserRepository users;
    @Mock
    PasswordEncoder encoder;
    @Mock
    JwtService jwt;
    @Mock
    RefreshTokenService refresh;
    AuthService service;
    UserEntity user;

    @BeforeEach
    void setUp() {
        var properties = new AppProperties(
                new AppProperties.Jwt("12345678901234567890123456789012", "issuer", "audience", Duration.ofMinutes(15), Duration.ofDays(30)),
                new AppProperties.Cors(List.of("http://localhost")), new AppProperties.BootstrapAdmin("", "", "Admin", "USD"));
        service = new AuthService(users, encoder, jwt, refresh, properties);
        user = new UserEntity("user@example.com", "encoded", "User", "USD", UserRole.USER);
        lenient().when(jwt.issue(any())).thenReturn("access");
        lenient().when(refresh.issue(any())).thenReturn("refresh");
    }

    @Test
    void registersNormalizedUserAndRejectsDuplicatesOrInvalidCurrency() {
        when(encoder.encode("correct-horse-battery")).thenReturn("encoded");
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var result = service.register(new RegisterRequest(" USER@Example.com ", "correct-horse-battery", " User ", "usd"));
        assertEquals("access", result.accessToken());
        verify(users).existsByEmailIgnoreCase("user@example.com");

        when(users.existsByEmailIgnoreCase("taken@example.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.register(
                new RegisterRequest("taken@example.com", "correct-horse-battery", "User", "USD")));
        assertThrows(IllegalArgumentException.class, () -> AuthService.normalizeCurrency("ZZZ"));
    }

    @Test
    void loginHandlesMissingBadDisabledAndActiveAccounts() {
        when(users.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> service.login(new LoginRequest("missing@example.com", "password")));

        when(users.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "encoded")).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> service.login(new LoginRequest("user@example.com", "wrong")));

        user.setStatus(UserStatus.DISABLED);
        assertThrows(UnauthorizedException.class, () -> service.login(new LoginRequest("user@example.com", "correct")));
        user.setStatus(UserStatus.ACTIVE);
        when(encoder.matches("correct", "encoded")).thenReturn(true);
        assertEquals("refresh", service.login(new LoginRequest("user@example.com", "correct")).refreshToken());
    }

    @Test
    void refreshAndLogoutDelegateToTokenService() {
        when(refresh.rotate("old")).thenReturn(new RefreshTokenService.Rotation(user, "new"));
        assertEquals("new", service.refresh("old").refreshToken());
        service.logout("new");
        verify(refresh).revokeFamily("new");
    }
}

