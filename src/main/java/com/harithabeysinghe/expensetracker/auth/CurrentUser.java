package com.harithabeysinghe.expensetracker.auth;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUser {
    public UUID id(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}

