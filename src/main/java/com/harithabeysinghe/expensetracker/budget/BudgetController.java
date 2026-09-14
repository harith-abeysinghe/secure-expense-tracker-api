package com.harithabeysinghe.expensetracker.budget;

import com.harithabeysinghe.expensetracker.auth.CurrentUser;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import com.harithabeysinghe.expensetracker.budget.dto.BudgetRequest;
import com.harithabeysinghe.expensetracker.budget.dto.BudgetResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@StandardApiErrors
public class BudgetController {
    private final BudgetService service;
    private final CurrentUser currentUser;

    public BudgetController(BudgetService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

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
