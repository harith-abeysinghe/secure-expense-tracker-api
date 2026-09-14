package com.harithabeysinghe.expensetracker.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(@Valid Jwt jwt, @Valid Cors cors, @Valid BootstrapAdmin bootstrapAdmin) {
    public record Jwt(
            @NotBlank String secret,
            @NotBlank String issuer,
            @NotBlank String audience,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration refreshTokenTtl
    ) {
        public Jwt {
            if (accessTokenTtl != null && (accessTokenTtl.isZero() || accessTokenTtl.isNegative())) {
                throw new IllegalArgumentException("access-token-ttl must be positive");
            }
            if (refreshTokenTtl != null && (refreshTokenTtl.isZero() || refreshTokenTtl.isNegative())) {
                throw new IllegalArgumentException("refresh-token-ttl must be positive");
            }
        }
    }

    public record Cors(@NotEmpty List<String> allowedOrigins) {
    }

    public record BootstrapAdmin(String email, String password, String displayName, String currency) {
    }
}
