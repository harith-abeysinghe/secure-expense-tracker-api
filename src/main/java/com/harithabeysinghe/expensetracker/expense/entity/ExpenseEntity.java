package com.harithabeysinghe.expensetracker.expense.entity;

import com.harithabeysinghe.expensetracker.category.entity.CategoryEntity;
import com.harithabeysinghe.expensetracker.common.entity.AuditableEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
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

    protected ExpenseEntity() {
    }

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

}

