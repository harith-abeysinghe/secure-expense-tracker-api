package com.harithabeysinghe.expensetracker.user;

import com.harithabeysinghe.expensetracker.common.error.ConflictException;
import com.harithabeysinghe.expensetracker.common.error.NotFoundException;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import com.harithabeysinghe.expensetracker.user.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    UserRepository repository;

    @Test
    void getsListsUpdatesAndProtectsSelf() {
        var service = new UserService(repository);
        var admin = new UserEntity("admin@example.com", "hash", "Admin", "USD", UserRole.ADMIN);
        var user = new UserEntity("user@example.com", "hash", "User", "USD", UserRole.USER);
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));
        assertEquals("user@example.com", service.get(user.getId()).email());
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));
        assertEquals(1, service.list(-1, 1000).totalElements());
        assertEquals(UserStatus.DISABLED, service.updateStatus(admin.getId(), user.getId(), UserStatus.DISABLED).status());
        assertThrows(ConflictException.class, () -> service.updateStatus(admin.getId(), admin.getId(), UserStatus.DISABLED));
        assertThrows(NotFoundException.class, () -> service.get(UUID.randomUUID()));
    }
}

