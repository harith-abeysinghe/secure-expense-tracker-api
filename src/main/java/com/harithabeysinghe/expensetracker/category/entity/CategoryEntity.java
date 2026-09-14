package com.harithabeysinghe.expensetracker.category.entity;

import com.harithabeysinghe.expensetracker.common.entity.AuditableEntity;
import com.harithabeysinghe.expensetracker.user.entity.UserEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "categories")
public class CategoryEntity extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false)
    private boolean archived;

    protected CategoryEntity() {}

    public CategoryEntity(UserEntity owner, String name) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.name = name;
    }

    public UUID getId() { return id; }
    public UserEntity getOwner() { return owner; }
    public String getName() { return name; }
    public boolean isArchived() { return archived; }
    public boolean isGlobal() { return owner == null; }
    public void rename(String name) { this.name = name; }
    public void archive() { this.archived = true; }
}

