package com.harithabeysinghe.expensetracker.category;

import com.harithabeysinghe.expensetracker.category.entity.CategoryEntity;
import com.harithabeysinghe.expensetracker.common.error.ConflictException;
import com.harithabeysinghe.expensetracker.common.error.NotFoundException;
import com.harithabeysinghe.expensetracker.user.UserService;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock CategoryRepository repository;
    @Mock UserService users;
    CategoryService service;
    UserEntity user;
    CategoryEntity personal;

    @BeforeEach
    void setUp() {
        service = new CategoryService(repository, users);
        user = new UserEntity("user@example.com", "hash", "User", "USD", UserRole.USER);
        personal = new CategoryEntity(user, "Coffee");
    }

    @Test
    void listsAndFiltersArchivedCategories() {
        var global = new CategoryEntity(null, "Food");
        personal.archive();
        when(repository.findByOwnerIdOrOwnerIsNullOrderByNameAsc(user.getId())).thenReturn(List.of(global, personal));
        assertEquals(1, service.listVisible(user.getId(), false).size());
        assertEquals(2, service.listVisible(user.getId(), true).size());
    }

    @Test
    void createsRenamesAndArchivesPersonalCategories() {
        when(users.require(user.getId())).thenReturn(user);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals("Coffee Shops", service.createPersonal(user.getId(), " Coffee   Shops ").name());

        when(repository.existsByOwnerIsNullAndNameIgnoreCase("Food")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.createPersonal(user.getId(), "Food"));

        when(repository.findByIdAndOwnerId(personal.getId(), user.getId())).thenReturn(Optional.of(personal));
        assertEquals("Cafe", service.renamePersonal(user.getId(), personal.getId(), " Cafe ").name());
        service.archivePersonal(user.getId(), personal.getId());
        assertTrue(personal.isArchived());
        assertThrows(NotFoundException.class, () -> service.archivePersonal(user.getId(), UUID.randomUUID()));
    }

    @Test
    void managesGlobalCategoriesAndValidatesVisibility() {
        var global = new CategoryEntity(null, "Education");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        assertTrue(service.createGlobal("Education").global());
        when(repository.existsByNameIgnoreCase("Duplicate")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.createGlobal("Duplicate"));

        when(repository.findByIdAndOwnerIsNull(global.getId())).thenReturn(Optional.of(global));
        assertEquals("Learning", service.renameGlobal(global.getId(), "Learning").name());
        service.archiveGlobal(global.getId());
        assertTrue(global.isArchived());

        var active = new CategoryEntity(null, "Active");
        when(repository.findById(active.getId())).thenReturn(Optional.of(active));
        assertSame(active, service.requireActiveVisible(user.getId(), active.getId()));
        when(repository.findById(personal.getId())).thenReturn(Optional.of(personal));
        assertSame(personal, service.requireActiveVisible(user.getId(), personal.getId()));
        var other = new CategoryEntity(new UserEntity("other@example.com", "h", "Other", "USD", UserRole.USER), "Private");
        when(repository.findById(other.getId())).thenReturn(Optional.of(other));
        assertThrows(NotFoundException.class, () -> service.requireActiveVisible(user.getId(), other.getId()));
    }
}
