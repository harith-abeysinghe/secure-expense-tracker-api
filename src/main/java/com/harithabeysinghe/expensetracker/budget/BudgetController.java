package com.harithabeysinghe.expensetracker.budget;

import com.harithabeysinghe.expensetracker.auth.CurrentUser;
import com.harithabeysinghe.expensetracker.budget.dto.BudgetRequest;
import com.harithabeysinghe.expensetracker.budget.dto.BudgetResponse;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@StandardApiErrors
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService service;
    private final CurrentUser currentUser;

    @GetMapping
    @Operation(summary = "List budgets for a calendar month")
    List<BudgetResponse> list(@RequestParam int year, @RequestParam int month, Authentication auth) {
        return service.list(currentUser.id(auth), year, month);
    }

    @PutMapping("/{year}/{month}/categories/{categoryId}")
    @Operation(summary = "Create or replace a category budget")
    BudgetResponse upsert(@PathVariable int year, @PathVariable int month, @PathVariable UUID categoryId,
                          @Valid @RequestBody BudgetRequest request, Authentication auth) {
        return service.upsert(currentUser.id(auth), categoryId, year, month, request.amount());
    }

    @DeleteMapping("/{year}/{month}/categories/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a category budget")
    void delete(@PathVariable int year, @PathVariable int month, @PathVariable UUID categoryId, Authentication auth) {
        service.delete(currentUser.id(auth), categoryId, year, month);
    }
}
