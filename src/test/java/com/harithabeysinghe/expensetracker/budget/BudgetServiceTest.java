package com.harithabeysinghe.expensetracker.budget;

import com.harithabeysinghe.expensetracker.budget.entity.BudgetEntity;
import com.harithabeysinghe.expensetracker.category.CategoryService;
import com.harithabeysinghe.expensetracker.category.entity.CategoryEntity;
import com.harithabeysinghe.expensetracker.common.error.NotFoundException;
import com.harithabeysinghe.expensetracker.user.UserService;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {
    @Mock
    BudgetRepository repository;
    @Mock
    UserService users;
    @Mock
    CategoryService categories;
    BudgetService service;
    UserEntity user;
    CategoryEntity category;

    @BeforeEach
    void setUp() {
        service = new BudgetService(repository, users, categories);
        user = new UserEntity("u@example.com", "hash", "User", "USD", UserRole.USER);
        category = new CategoryEntity(null, "Food");
    }

    @Test
    void validatesMonth() {
        assertThrows(IllegalArgumentException.class, () -> BudgetService.validateMonth(1999, 1));
        assertThrows(IllegalArgumentException.class, () -> BudgetService.validateMonth(2201, 1));
        assertThrows(IllegalArgumentException.class, () -> BudgetService.validateMonth(2026, 13));
        assertDoesNotThrow(() -> BudgetService.validateMonth(2026, 12));
    }

    @Test
    void createsUpdatesListsAndDeletesBudgets() {
        when(categories.requireActiveVisible(user.getId(), category.getId())).thenReturn(category);
        when(users.require(user.getId())).thenReturn(user);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.findByUserIdAndCategoryIdAndYearAndMonth(user.getId(), category.getId(), 2026, 9))
                .thenReturn(Optional.empty());
        assertEquals(new BigDecimal("100.00"), service.upsert(user.getId(), category.getId(), 2026, 9,
                new BigDecimal("100.00")).amount());

        var existing = new BudgetEntity(user, category, 2026, 9, BigDecimal.ONE);
        when(repository.findByUserIdAndCategoryIdAndYearAndMonth(user.getId(), category.getId(), 2026, 9))
                .thenReturn(Optional.of(existing));
        assertEquals(BigDecimal.TEN, service.upsert(user.getId(), category.getId(), 2026, 9, BigDecimal.TEN).amount());
        when(repository.findByUserIdAndYearAndMonth(user.getId(), 2026, 9)).thenReturn(List.of(existing));
        assertEquals(1, service.list(user.getId(), 2026, 9).size());
        service.delete(user.getId(), category.getId(), 2026, 9);
        verify(repository).delete(existing);

        when(repository.findByUserIdAndCategoryIdAndYearAndMonth(user.getId(), category.getId(), 2026, 10))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.delete(user.getId(), category.getId(), 2026, 10));
    }
}

