package com.harithabeysinghe.expensetracker.expense;

import com.harithabeysinghe.expensetracker.category.CategoryService;
import com.harithabeysinghe.expensetracker.category.entity.CategoryEntity;
import com.harithabeysinghe.expensetracker.common.error.NotFoundException;
import com.harithabeysinghe.expensetracker.expense.dto.ExpenseRequest;
import com.harithabeysinghe.expensetracker.expense.entity.ExpenseEntity;
import com.harithabeysinghe.expensetracker.user.UserService;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {
    @Mock
    ExpenseRepository repository;
    @Mock
    UserService users;
    @Mock
    CategoryService categories;
    ExpenseService service;
    UserEntity user;
    CategoryEntity category;
    ExpenseRequest request;

    @BeforeEach
    void setUp() {
        service = new ExpenseService(repository, users, categories);
        user = new UserEntity("user@example.com", "hash", "User", "USD", UserRole.USER);
        category = new CategoryEntity(null, "Food");
        request = new ExpenseRequest(category.getId(), new BigDecimal("12.50"), LocalDate.of(2026, 9, 14), " Lunch ");
    }

    @Test
    void createsGetsUpdatesAndDeletesOwnedExpense() {
        when(users.require(user.getId())).thenReturn(user);
        when(categories.requireActiveVisible(user.getId(), category.getId())).thenReturn(category);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var created = service.create(user.getId(), request);
        assertEquals("Lunch", created.description());

        var entity = new ExpenseEntity(user, category, BigDecimal.ONE, LocalDate.now(), null);
        when(repository.findByIdAndUserId(entity.getId(), user.getId())).thenReturn(Optional.of(entity));
        assertEquals(entity.getId(), service.get(user.getId(), entity.getId()).id());
        assertEquals(new BigDecimal("12.50"), service.update(user.getId(), entity.getId(), request).amount());
        service.delete(user.getId(), entity.getId());
        verify(repository).delete(entity);
        assertThrows(NotFoundException.class, () -> service.get(user.getId(), java.util.UUID.randomUUID()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void listsWithBoundsAndRejectsInvertedDates() {
        var entity = new ExpenseEntity(user, category, BigDecimal.ONE, LocalDate.now(), "x");
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(entity)));
        assertEquals(1, service.list(user.getId(), null, null, null, -2, 1000).totalElements());
        assertEquals(1, service.list(user.getId(), LocalDate.now(), LocalDate.now(), category.getId(), 0, 20).totalElements());
        assertThrows(IllegalArgumentException.class, () -> service.list(user.getId(), LocalDate.now(),
                LocalDate.now().minusDays(1), null, 0, 20));
    }
}

