package com.harithabeysinghe.expensetracker.expense.entity;

import com.harithabeysinghe.expensetracker.category.entity.CategoryEntity;
import com.harithabeysinghe.expensetracker.common.entity.AuditableEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public class ExpenseEntity extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(length = 500)
    private String description;

    protected ExpenseEntity() {}

    public ExpenseEntity(UserEntity user, CategoryEntity category, BigDecimal amount, LocalDate expenseDate, String description) {
        this.id = UUID.randomUUID();
        this.user = user;
        update(category, amount, expenseDate, description);
    }

    public void update(CategoryEntity category, BigDecimal amount, LocalDate expenseDate, String description) {
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
    }

    public UUID getId() { return id; }
    public UserEntity getUser() { return user; }
    public CategoryEntity getCategory() { return category; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public String getDescription() { return description; }
}

