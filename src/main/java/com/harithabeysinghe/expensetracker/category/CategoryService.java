package com.harithabeysinghe.expensetracker.category;

import com.harithabeysinghe.expensetracker.category.dto.CategoryResponse;
import com.harithabeysinghe.expensetracker.category.entity.CategoryEntity;
import com.harithabeysinghe.expensetracker.common.error.ConflictException;
import com.harithabeysinghe.expensetracker.common.error.NotFoundException;
import com.harithabeysinghe.expensetracker.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categories;
    private final UserService users;

    public CategoryService(CategoryRepository categories, UserService users) {
        this.categories = categories;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listVisible(UUID userId, boolean includeArchived) {
        return categories.findByOwnerIdOrOwnerIsNullOrderByNameAsc(userId).stream()
                .filter(category -> includeArchived || !category.isArchived())
                .map(this::map).toList();
    }

    @Transactional
    public CategoryResponse createPersonal(UUID userId, String rawName) {
        var name = normalizeName(rawName);
        rejectVisibleDuplicate(userId, name, null);
        return map(categories.save(new CategoryEntity(users.require(userId), name)));
    }

    @Transactional
    public CategoryResponse renamePersonal(UUID userId, UUID id, String rawName) {
        var category = categories.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        var name = normalizeName(rawName);
        rejectVisibleDuplicate(userId, name, id);
        category.rename(name);
        return map(category);
    }

    @Transactional
    public void archivePersonal(UUID userId, UUID id) {
        categories.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new NotFoundException("Category not found")).archive();
    }

    @Transactional
    public CategoryResponse createGlobal(String rawName) {
        var name = normalizeName(rawName);
        if (categories.existsByNameIgnoreCase(name)) throw duplicate();
        return map(categories.save(new CategoryEntity(null, name)));
    }

    @Transactional
    public CategoryResponse renameGlobal(UUID id, String rawName) {
        var category = categories.findByIdAndOwnerIsNull(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        var name = normalizeName(rawName);
        if (categories.existsByNameIgnoreCaseAndIdNot(name, id)) throw duplicate();
        category.rename(name);
        return map(category);
    }

    @Transactional
    public void archiveGlobal(UUID id) {
        categories.findByIdAndOwnerIsNull(id)
                .orElseThrow(() -> new NotFoundException("Category not found")).archive();
    }

    @Transactional(readOnly = true)
    public CategoryEntity requireActiveVisible(UUID userId, UUID id) {
        var category = categories.findById(id).orElseThrow(() -> new NotFoundException("Category not found"));
        var visible = category.isGlobal() || category.getOwner().getId().equals(userId);
        if (!visible || category.isArchived()) throw new NotFoundException("Active category not found");
        return category;
    }

    private void rejectVisibleDuplicate(UUID userId, String name, UUID excludedId) {
        var duplicateGlobal = excludedId == null
                ? categories.existsByOwnerIsNullAndNameIgnoreCase(name)
                : categories.existsByOwnerIsNullAndNameIgnoreCaseAndIdNot(name, excludedId);
        var duplicatePersonal = excludedId == null
                ? categories.existsByOwnerIdAndNameIgnoreCase(userId, name)
                : categories.existsByOwnerIdAndNameIgnoreCaseAndIdNot(userId, name, excludedId);
        if (duplicateGlobal || duplicatePersonal) throw duplicate();
    }

    private String normalizeName(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private ConflictException duplicate() { return new ConflictException("A visible category with this name already exists"); }

    private CategoryResponse map(CategoryEntity category) {
        return new CategoryResponse(category.getId(), category.getName(), category.isGlobal(), category.isArchived());
    }
}
