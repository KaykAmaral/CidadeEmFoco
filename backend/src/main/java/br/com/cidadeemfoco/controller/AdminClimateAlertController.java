package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.ClimateAlertResponse;
import br.com.cidadeemfoco.dto.CreateClimateAlertRequest;
import br.com.cidadeemfoco.service.ClimateAlertService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/alerts")
public class AdminClimateAlertController {

    private final ClimateAlertService climateAlertService;

    public AdminClimateAlertController(ClimateAlertService climateAlertService) {
        this.climateAlertService = climateAlertService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClimateAlertResponse create(Authentication authentication, @Valid @RequestBody CreateClimateAlertRequest request) {
        return climateAlertService.create(authentication.getName(), request);
    }

    @GetMapping
    public List<ClimateAlertResponse> findAll(Authentication authentication) {
        return climateAlertService.findAll(authentication.getName());
    }

    @GetMapping("/{id}")
    public ClimateAlertResponse findById(Authentication authentication, @PathVariable @Positive Long id) {
        return climateAlertService.findById(authentication.getName(), id);
    }

    @PatchMapping("/{id}")
    public ClimateAlertResponse update(
            Authentication authentication,
            @PathVariable @Positive Long id,
            @Valid @RequestBody CreateClimateAlertRequest request
    ) {
        return climateAlertService.update(authentication.getName(), id, request);
    }

    @PatchMapping("/{id}/activate")
    public ClimateAlertResponse activate(Authentication authentication, @PathVariable @Positive Long id) {
        return climateAlertService.activate(authentication.getName(), id);
    }

    @PatchMapping("/{id}/deactivate")
    public ClimateAlertResponse deactivate(Authentication authentication, @PathVariable @Positive Long id) {
        return climateAlertService.deactivate(authentication.getName(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable @Positive Long id) {
        climateAlertService.delete(authentication.getName(), id);
    }
}
