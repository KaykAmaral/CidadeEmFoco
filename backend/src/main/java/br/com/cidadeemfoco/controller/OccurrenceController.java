package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.CreateOccurrenceRequest;
import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.service.OccurrenceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/occurrences")
public class OccurrenceController {

    // Temporario ate a etapa 5, quando o usuario sera obtido do token JWT.
    private static final String TEMPORARY_USER_HEADER = "X-User-Id";

    private final OccurrenceService occurrenceService;

    public OccurrenceController(OccurrenceService occurrenceService) {
        this.occurrenceService = occurrenceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OccurrenceResponse create(
            @RequestHeader(TEMPORARY_USER_HEADER) @Positive Long userId,
            @Valid @RequestBody CreateOccurrenceRequest request
    ) {
        return occurrenceService.create(userId, request);
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

    @GetMapping("/mine")
    public List<OccurrenceResponse> findMine(
            @RequestHeader(TEMPORARY_USER_HEADER) @Positive Long userId
    ) {
        return occurrenceService.findByUser(userId);
    }

    @GetMapping("/{id}")
    public OccurrenceResponse findById(@PathVariable @Positive Long id) {
        return occurrenceService.findById(id);
    }
}
