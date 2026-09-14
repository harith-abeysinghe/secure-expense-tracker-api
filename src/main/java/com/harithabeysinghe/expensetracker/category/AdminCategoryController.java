package com.harithabeysinghe.expensetracker.category;

import com.harithabeysinghe.expensetracker.category.dto.CategoryRequest;
import com.harithabeysinghe.expensetracker.category.dto.CategoryResponse;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/categories")
@StandardApiErrors
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a global category")
    CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return service.createGlobal(request.name());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Rename a global category")
    CategoryResponse rename(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return service.renameGlobal(id, request.name());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive a global category")
    void archive(@PathVariable UUID id) {
        service.archiveGlobal(id);
    }
}
