package com.harithabeysinghe.expensetracker.expense;

import com.harithabeysinghe.expensetracker.auth.CurrentUser;
import com.harithabeysinghe.expensetracker.common.dto.PageResponse;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import com.harithabeysinghe.expensetracker.expense.dto.ExpenseRequest;
import com.harithabeysinghe.expensetracker.expense.dto.ExpenseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@StandardApiErrors
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService service;
    private final CurrentUser currentUser;

    @GetMapping
    @Operation(summary = "List and filter the authenticated user's expenses")
    PageResponse<ExpenseResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return service.list(currentUser.id(auth), from, to, categoryId, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an owned expense")
    ExpenseResponse get(@PathVariable UUID id, Authentication auth) {
        return service.get(currentUser.id(auth), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an expense")
    ExpenseResponse create(@Valid @RequestBody ExpenseRequest request, Authentication auth) {
        return service.create(currentUser.id(auth), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace an owned expense")
    ExpenseResponse update(@PathVariable UUID id, @Valid @RequestBody ExpenseRequest request, Authentication auth) {
        return service.update(currentUser.id(auth), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an owned expense")
    void delete(@PathVariable UUID id, Authentication auth) {
        service.delete(currentUser.id(auth), id);
    }
}
