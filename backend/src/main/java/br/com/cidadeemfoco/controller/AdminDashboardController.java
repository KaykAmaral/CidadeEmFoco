package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.AdminOccurrenceMetricsResponse;
import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Validated
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    public AdminDashboardController(AdminDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/occurrences")
    @Operation(summary = "Consultar indicadores de ocorrencias da cidade administrada")
    public AdminOccurrenceMetricsResponse occurrenceMetrics(
            Authentication authentication,
            @RequestParam(required = false) OccurrenceCategory category,
            @RequestParam(required = false) OccurrenceType type,
            @RequestParam(required = false) OccurrenceStatus status,
            @RequestParam(required = false) @Size(max = 100) String neighborhood,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo
    ) {
        return dashboardService.occurrenceMetrics(authentication.getName(), new OccurrenceFilter(
                category, type, status, neighborhood, createdFrom, createdTo
        ));
    }
}
