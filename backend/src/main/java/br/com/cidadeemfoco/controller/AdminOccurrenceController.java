package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.dto.UpdateOccurrenceStatusRequest;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.service.OccurrenceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/occurrences")
public class AdminOccurrenceController {

    private final OccurrenceService occurrenceService;

    public AdminOccurrenceController(OccurrenceService occurrenceService) {
        this.occurrenceService = occurrenceService;
    }

    @GetMapping
    public List<OccurrenceResponse> findAll(
            @RequestParam(required = false) OccurrenceCategory category,
            @RequestParam(required = false) OccurrenceType type,
            @RequestParam(required = false) OccurrenceStatus status,
            @RequestParam(required = false) @Size(max = 100) String neighborhood
    ) {
        return occurrenceService.findAll(new OccurrenceFilter(category, type, status, neighborhood));
    }

    @GetMapping("/{id}")
    public OccurrenceResponse findById(@PathVariable @Positive Long id) {
        return occurrenceService.findById(id);
    }

    @PatchMapping("/{id}/status")
    public OccurrenceResponse updateStatus(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateOccurrenceStatusRequest request
    ) {
        return occurrenceService.updateStatus(id, request.status());
    }
}
