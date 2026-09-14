package com.harithabeysinghe.expensetracker.user;

import com.harithabeysinghe.expensetracker.config.AppProperties;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapTest {
    @Mock UserRepository users;
    @Mock PasswordEncoder encoder;
    final DefaultApplicationArguments args = new DefaultApplicationArguments();

    @Test
    void skipsWhenUnsetAndRejectsPartialOrWeakConfiguration() {
        bootstrap("", "").run(args);
        verifyNoInteractions(users);

        assertThrows(IllegalStateException.class, () -> bootstrap("admin@example.com", "").run(args));
        assertThrows(IllegalStateException.class, () -> bootstrap("admin@example.com", "short").run(args));
    }

    @Test
    void createsMissingAdminAndLeavesExistingAccountUntouched() {
        when(encoder.encode("administrator-password")).thenReturn("encoded");
        bootstrap(" ADMIN@example.com ", "administrator-password").run(args);
        verify(users).findByEmailIgnoreCase("admin@example.com");
        verify(users).save(any(UserEntity.class));

        reset(users, encoder);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(java.util.Optional.of(
                new UserEntity("admin@example.com", "hash", "Admin", "USD", com.harithabeysinghe.expensetracker.user.entity.UserRole.ADMIN)));
        bootstrap("admin@example.com", "administrator-password").run(args);
        verify(users, never()).save(any());
        verifyNoInteractions(encoder);

        reset(users, encoder);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(java.util.Optional.of(
                new UserEntity("admin@example.com", "hash", "User", "USD", com.harithabeysinghe.expensetracker.user.entity.UserRole.USER)));
        assertThrows(IllegalStateException.class,
                () -> bootstrap("admin@example.com", "administrator-password").run(args));
    }

    private AdminBootstrap bootstrap(String email, String password) {
        var properties = new AppProperties(
                new AppProperties.Jwt("12345678901234567890123456789012", "issuer", "audience", Duration.ofMinutes(15), Duration.ofDays(30)),
                new AppProperties.Cors(List.of("http://localhost")),
                new AppProperties.BootstrapAdmin(email, password, "Administrator", "usd"));
        return new AdminBootstrap(users, encoder, properties);
    }
}
