package com.harithabeysinghe.expensetracker.budget.entity;

import com.harithabeysinghe.expensetracker.category.entity.CategoryEntity;
import com.harithabeysinghe.expensetracker.common.entity.AuditableEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budgets")
public class BudgetEntity extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "budget_year", nullable = false)
    private int year;

    @Column(name = "budget_month", nullable = false)
    private int month;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    protected BudgetEntity() {}

    public BudgetEntity(UserEntity user, CategoryEntity category, int year, int month, BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.category = category;
        this.year = year;
        this.month = month;
        this.amount = amount;
    }

    public UUID getId() { return id; }
    public UserEntity getUser() { return user; }
    public CategoryEntity getCategory() { return category; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}

