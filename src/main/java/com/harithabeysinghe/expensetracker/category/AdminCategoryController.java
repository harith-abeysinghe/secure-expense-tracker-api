package com.harithabeysinghe.expensetracker.category;

import com.harithabeysinghe.expensetracker.category.dto.CategoryRequest;
import com.harithabeysinghe.expensetracker.category.dto.CategoryResponse;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/categories")
@StandardApiErrors
public class AdminCategoryController {
    private final CategoryService service;

    public AdminCategoryController(CategoryService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a global category")
    CategoryResponse create(@Valid @RequestBody CategoryRequest request) { return service.createGlobal(request.name()); }

    @PutMapping("/{id}")
    @Operation(summary = "Rename a global category")
    CategoryResponse rename(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return service.renameGlobal(id, request.name());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive a global category")
    void archive(@PathVariable UUID id) { service.archiveGlobal(id); }
}
