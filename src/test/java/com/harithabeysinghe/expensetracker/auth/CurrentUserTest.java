package com.harithabeysinghe.expensetracker.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrentUserTest {
    @Test
    void readsUuidSubject() {
        var id = UUID.randomUUID();
        assertEquals(id, new CurrentUser().id(new TestingAuthenticationToken(id.toString(), null)));
    }
}

