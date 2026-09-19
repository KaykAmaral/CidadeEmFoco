package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.ClimateAlertResponse;
import br.com.cidadeemfoco.service.ClimateAlertRealtimeService;
import br.com.cidadeemfoco.service.ClimateAlertService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class ClimateAlertController {

    private final ClimateAlertService climateAlertService;
    private final ClimateAlertRealtimeService climateAlertRealtimeService;

    public ClimateAlertController(
            ClimateAlertService climateAlertService,
            ClimateAlertRealtimeService climateAlertRealtimeService
    ) {
        this.climateAlertService = climateAlertService;
        this.climateAlertRealtimeService = climateAlertRealtimeService;
    }

    @GetMapping("/active")
    public List<ClimateAlertResponse> findCurrentlyActive() {
        return climateAlertService.findCurrentlyActive();
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUpdates() {
        return climateAlertRealtimeService.subscribe();
    }
}
