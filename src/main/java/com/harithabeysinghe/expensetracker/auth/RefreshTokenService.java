package com.harithabeysinghe.expensetracker.auth;

import com.harithabeysinghe.expensetracker.auth.entity.RefreshTokenEntity;
import com.harithabeysinghe.expensetracker.common.error.UnauthorizedException;
import com.harithabeysinghe.expensetracker.config.AppProperties;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repository;
    private final AppProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    @Transactional
    public String issue(UserEntity user) {
        return create(user, UUID.randomUUID());
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public Rotation rotate(String rawToken) {
        var now = Instant.now();
        var token = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (token.isRevoked()) {
            repository.revokeFamily(token.getFamilyId(), now);
            throw new UnauthorizedException("Refresh token reuse detected");
        }
        if (token.isExpired(now) || token.getUser().getStatus() != UserStatus.ACTIVE) {
            repository.revokeFamily(token.getFamilyId(), now);
            throw new UnauthorizedException("Refresh token is expired or the account is disabled");
        }
        var rawReplacement = randomToken();
        var replacementHash = hash(rawReplacement);
        token.revoke(now, replacementHash);
        repository.save(new RefreshTokenEntity(token.getUser(), token.getFamilyId(), replacementHash,
                now.plus(properties.jwt().refreshTokenTtl())));
        return new Rotation(token.getUser(), rawReplacement);
    }

    @Transactional
    public void revokeFamily(String rawToken) {
        repository.findByTokenHash(hash(rawToken))
                .ifPresent(token -> repository.revokeFamily(token.getFamilyId(), Instant.now()));
    }

    private String create(UserEntity user, UUID familyId) {
        var raw = randomToken();
        repository.save(new RefreshTokenEntity(user, familyId, hash(raw),
                Instant.now().plus(properties.jwt().refreshTokenTtl())));
        return raw;
    }

    private String randomToken() {
        var bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record Rotation(UserEntity user, String refreshToken) {
    }
}
