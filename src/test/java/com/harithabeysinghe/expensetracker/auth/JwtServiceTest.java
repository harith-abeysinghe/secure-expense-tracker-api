package com.harithabeysinghe.expensetracker.auth;

import com.harithabeysinghe.expensetracker.config.AppProperties;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @Mock JwtEncoder encoder;

    @Test
    void issuesExpectedClaims() {
        var properties = new AppProperties(
                new AppProperties.Jwt("12345678901234567890123456789012", "https://issuer.example", "audience", Duration.ofMinutes(15), Duration.ofDays(30)),
                new AppProperties.Cors(List.of("http://localhost")), new AppProperties.BootstrapAdmin("", "", "Admin", "USD"));
        var user = new UserEntity("admin@example.com", "hash", "Admin", "USD", UserRole.ADMIN);
        when(encoder.encode(any())).thenReturn(Jwt.withTokenValue("signed-token")
                .header("alg", "HS256").subject(user.getId().toString()).issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(900)).build());

        assertEquals("signed-token", new JwtService(encoder, properties).issue(user));
        var captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(encoder).encode(captor.capture());
        var claims = captor.getValue().getClaims();
        assertEquals("https://issuer.example", claims.getIssuer().toString());
        assertEquals(List.of("audience"), claims.getAudience());
        assertEquals("ADMIN", claims.getClaim("role"));
        assertEquals("admin@example.com", claims.getClaim("email"));
        assertNotNull(claims.getId());
    }
}
