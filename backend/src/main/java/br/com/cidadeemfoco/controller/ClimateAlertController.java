package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.ClimateAlertResponse;
import br.com.cidadeemfoco.service.ClimateAlertService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class ClimateAlertController {

    private final ClimateAlertService climateAlertService;

    public ClimateAlertController(ClimateAlertService climateAlertService) {
        this.climateAlertService = climateAlertService;
    }

    @GetMapping("/active")
    public List<ClimateAlertResponse> findCurrentlyActive() {
        return climateAlertService.findCurrentlyActive();
    }
}
