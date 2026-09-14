package com.harithabeysinghe.expensetracker.user;

import com.harithabeysinghe.expensetracker.auth.AuthService;
import com.harithabeysinghe.expensetracker.config.AppProperties;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties properties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        var admin = properties.bootstrapAdmin();
        if (!StringUtils.hasText(admin.email()) && !StringUtils.hasText(admin.password())) return;
        if (!StringUtils.hasText(admin.email()) || !StringUtils.hasText(admin.password()) || admin.password().length() < 12) {
            throw new IllegalStateException("ADMIN_EMAIL and an ADMIN_PASSWORD of at least 12 characters must be supplied together");
        }
        var email = AuthService.normalizeEmail(admin.email());
        users.findByEmailIgnoreCase(email).ifPresentOrElse(existing -> {
            if (existing.getRole() != UserRole.ADMIN) {
                throw new IllegalStateException("ADMIN_EMAIL already belongs to a non-admin account");
            }
        }, () -> users.save(new UserEntity(email, passwordEncoder.encode(admin.password()), admin.displayName().trim(),
                AuthService.normalizeCurrency(admin.currency()), UserRole.ADMIN)));
    }
}
