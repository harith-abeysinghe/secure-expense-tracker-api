package com.harithabeysinghe.expensetracker.auth;

import com.harithabeysinghe.expensetracker.auth.entity.RefreshTokenEntity;
import com.harithabeysinghe.expensetracker.common.error.UnauthorizedException;
import com.harithabeysinghe.expensetracker.config.AppProperties;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import com.harithabeysinghe.expensetracker.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    @Mock
    RefreshTokenRepository repository;
    RefreshTokenService service;
    UserEntity user;

    @BeforeEach
    void setUp() {
        var properties = new AppProperties(
                new AppProperties.Jwt("12345678901234567890123456789012", "issuer", "audience", Duration.ofMinutes(15), Duration.ofDays(30)),
                new AppProperties.Cors(List.of("http://localhost")), new AppProperties.BootstrapAdmin("", "", "Admin", "USD"));
        service = new RefreshTokenService(repository, properties);
        user = new UserEntity("user@example.com", "hash", "User", "USD", UserRole.USER);
    }

    @Test
    void issueAndRotateActiveToken() {
        var raw = service.issue(user);
        assertFalse(raw.isBlank());
        verify(repository).save(any(RefreshTokenEntity.class));

        var existing = new RefreshTokenEntity(user, UUID.randomUUID(), RefreshTokenService.hash("old"), Instant.now().plusSeconds(60));
        when(repository.findByTokenHash(RefreshTokenService.hash("old"))).thenReturn(Optional.of(existing));
        var rotation = service.rotate("old");
        assertSame(user, rotation.user());
        assertFalse(rotation.refreshToken().isBlank());
        assertTrue(existing.isRevoked());
        verify(repository, times(2)).save(any(RefreshTokenEntity.class));
    }

    @Test
    void missingRevokedExpiredAndDisabledTokensAreRejected() {
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());
        assertThrows(UnauthorizedException.class, () -> service.rotate("missing"));

        var family = UUID.randomUUID();
        var revoked = new RefreshTokenEntity(user, family, RefreshTokenService.hash("revoked"), Instant.now().plusSeconds(60));
        revoked.revoke(Instant.now(), "replacement");
        when(repository.findByTokenHash(RefreshTokenService.hash("revoked"))).thenReturn(Optional.of(revoked));
        assertThrows(UnauthorizedException.class, () -> service.rotate("revoked"));
        verify(repository).revokeFamily(eq(family), any());

        var expired = new RefreshTokenEntity(user, family, RefreshTokenService.hash("expired"), Instant.now().minusSeconds(1));
        when(repository.findByTokenHash(RefreshTokenService.hash("expired"))).thenReturn(Optional.of(expired));
        assertThrows(UnauthorizedException.class, () -> service.rotate("expired"));

        user.setStatus(UserStatus.DISABLED);
        var disabled = new RefreshTokenEntity(user, family, RefreshTokenService.hash("disabled"), Instant.now().plusSeconds(60));
        when(repository.findByTokenHash(RefreshTokenService.hash("disabled"))).thenReturn(Optional.of(disabled));
        assertThrows(UnauthorizedException.class, () -> service.rotate("disabled"));
    }

    @Test
    void logoutIsIdempotentAndRevokesKnownFamily() {
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());
        service.revokeFamily("unknown");
        verify(repository, never()).revokeFamily(any(), any());

        var family = UUID.randomUUID();
        var token = new RefreshTokenEntity(user, family, RefreshTokenService.hash("known"), Instant.now().plusSeconds(60));
        when(repository.findByTokenHash(RefreshTokenService.hash("known"))).thenReturn(Optional.of(token));
        service.revokeFamily("known");
        verify(repository).revokeFamily(eq(family), any());
    }
}

