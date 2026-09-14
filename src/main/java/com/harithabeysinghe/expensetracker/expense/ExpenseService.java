package com.harithabeysinghe.expensetracker.expense;

import com.harithabeysinghe.expensetracker.category.CategoryService;
import com.harithabeysinghe.expensetracker.common.dto.PageResponse;
import com.harithabeysinghe.expensetracker.common.error.NotFoundException;
import com.harithabeysinghe.expensetracker.expense.dto.ExpenseRequest;
import com.harithabeysinghe.expensetracker.expense.dto.ExpenseResponse;
import com.harithabeysinghe.expensetracker.expense.entity.ExpenseEntity;
import com.harithabeysinghe.expensetracker.user.UserService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class ExpenseService {
    private final ExpenseRepository expenses;
    private final UserService users;
    private final CategoryService categories;

    public ExpenseService(ExpenseRepository expenses, UserService users, CategoryService categories) {
        this.expenses = expenses;
        this.users = users;
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public PageResponse<ExpenseResponse> list(UUID userId, LocalDate from, LocalDate to, UUID categoryId,
                                               int page, int size) {
        if (from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException("from must not be after to");
        var pageable = PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 100),
                Sort.by(Sort.Order.desc("expenseDate"), Sort.Order.desc("createdAt")));
        return PageResponse.from(expenses.findAll(specification(userId, from, to, categoryId), pageable), this::map);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(UUID userId, UUID id) { return map(requireOwned(userId, id)); }

    @Transactional
    public ExpenseResponse create(UUID userId, ExpenseRequest request) {
        var category = categories.requireActiveVisible(userId, request.categoryId());
        var expense = new ExpenseEntity(users.require(userId), category, request.amount(), request.expenseDate(),
                normalizeDescription(request.description()));
        return map(expenses.save(expense));
    }

    @Transactional
    public ExpenseResponse update(UUID userId, UUID id, ExpenseRequest request) {
        var expense = requireOwned(userId, id);
        var category = categories.requireActiveVisible(userId, request.categoryId());
        expense.update(category, request.amount(), request.expenseDate(), normalizeDescription(request.description()));
        return map(expense);
    }

    @Transactional
    public void delete(UUID userId, UUID id) { expenses.delete(requireOwned(userId, id)); }

    private ExpenseEntity requireOwned(UUID userId, UUID id) {
        return expenses.findByIdAndUserId(id, userId).orElseThrow(() -> new NotFoundException("Expense not found"));
    }

    private Specification<ExpenseEntity> specification(UUID userId, LocalDate from, LocalDate to, UUID categoryId) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(builder.equal(root.get("user").get("id"), userId));
            if (from != null) predicates.add(builder.greaterThanOrEqualTo(root.get("expenseDate"), from));
            if (to != null) predicates.add(builder.lessThanOrEqualTo(root.get("expenseDate"), to));
            if (categoryId != null) predicates.add(builder.equal(root.get("category").get("id"), categoryId));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private String normalizeDescription(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private ExpenseResponse map(ExpenseEntity expense) {
        return new ExpenseResponse(expense.getId(), expense.getCategory().getId(), expense.getCategory().getName(),
                expense.getAmount(), expense.getExpenseDate(), expense.getDescription(), expense.getCreatedAt(),
                expense.getUpdatedAt());
    }
}

