package com.harithabeysinghe.expensetracker.report;

import com.harithabeysinghe.expensetracker.auth.CurrentUser;
import com.harithabeysinghe.expensetracker.common.openapi.StandardApiErrors;
import com.harithabeysinghe.expensetracker.report.dto.MonthlyReport;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@StandardApiErrors
public class ReportController {
    private final ReportService service;
    private final CurrentUser currentUser;

    public ReportController(ReportService service, CurrentUser currentUser) {
        this.service = service;
        this.currentUser = currentUser;
    }

    @GetMapping("/monthly/{year}/{month}")
    @Operation(summary = "Get monthly spending and budget performance")
    MonthlyReport monthly(@PathVariable int year, @PathVariable int month, Authentication auth) {
        return service.monthly(currentUser.id(auth), year, month);
    }
}
