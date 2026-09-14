package com.harithabeysinghe.expensetracker.category;

import com.harithabeysinghe.expensetracker.auth.CurrentUser;
import com.harithabeysinghe.expensetracker.category.dto.CategoryRequest;
import com.harithabeysinghe.expensetracker.category.dto.CategoryResponse;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@StandardApiErrors
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;
    private final CurrentUser currentUser;

    @GetMapping
    @Operation(summary = "List global and personal categories")
    List<CategoryResponse> list(@RequestParam(defaultValue = "false") boolean includeArchived, Authentication auth) {
        return service.listVisible(currentUser.id(auth), includeArchived);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a personal category")
    CategoryResponse create(@Valid @RequestBody CategoryRequest request, Authentication auth) {
        return service.createPersonal(currentUser.id(auth), request.name());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Rename a personal category")
    CategoryResponse rename(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request, Authentication auth) {
        return service.renamePersonal(currentUser.id(auth), id, request.name());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Archive a personal category")
    void archive(@PathVariable UUID id, Authentication auth) {
        service.archivePersonal(currentUser.id(auth), id);
    }
}
