package br.com.cidadeemfoco.controller;

import br.com.cidadeemfoco.dto.WhatsappNotificationResponse;
import br.com.cidadeemfoco.enums.WhatsappNotificationStatus;
import br.com.cidadeemfoco.service.WhatsappNotificationService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/whatsapp-notifications")
public class AdminWhatsappNotificationController {

    private final WhatsappNotificationService notificationService;

    public AdminWhatsappNotificationController(WhatsappNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<WhatsappNotificationResponse> findAll(
            @RequestParam(required = false) WhatsappNotificationStatus status
    ) {
        return notificationService.findAll(status);
    }

    @GetMapping("/{id}")
    public WhatsappNotificationResponse findById(@PathVariable @Positive Long id) {
        return notificationService.findById(id);
    }
}
