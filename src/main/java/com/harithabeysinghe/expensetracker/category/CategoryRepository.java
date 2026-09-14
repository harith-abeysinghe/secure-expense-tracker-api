package com.harithabeysinghe.expensetracker.category;

import com.harithabeysinghe.expensetracker.category.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findByOwnerIdOrOwnerIsNullOrderByNameAsc(UUID ownerId);

    Optional<CategoryEntity> findByIdAndOwnerId(UUID id, UUID ownerId);

    Optional<CategoryEntity> findByIdAndOwnerIsNull(UUID id);

    boolean existsByOwnerIdAndNameIgnoreCase(UUID ownerId, String name);

    boolean existsByOwnerIsNullAndNameIgnoreCase(String name);

    boolean existsByOwnerIdAndNameIgnoreCaseAndIdNot(UUID ownerId, String name, UUID id);

    boolean existsByOwnerIsNullAndNameIgnoreCaseAndIdNot(String name, UUID id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
