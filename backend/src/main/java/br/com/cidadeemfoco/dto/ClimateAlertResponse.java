package br.com.cidadeemfoco.dto;

import br.com.cidadeemfoco.entity.ClimateAlert;
import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;

import java.time.Instant;

public record ClimateAlertResponse(
        Long id,
        String title,
        ClimateAlertType type,
        AlertSeverity severity,
        String description,
        Instant startAt,
        Instant endAt,
        boolean active,
        Instant createdAt,
        String disclaimer
) {

    private static final String DISCLAIMER =
            "Este alerta e demonstrativo e nao substitui informacoes oficiais.";

    public static ClimateAlertResponse from(ClimateAlert alert) {
        return new ClimateAlertResponse(
                alert.getId(),
                alert.getTitle(),
                alert.getType(),
                alert.getSeverity(),
                alert.getDescription(),
                alert.getStartAt(),
                alert.getEndAt(),
                alert.isActive(),
                alert.getCreatedAt(),
                DISCLAIMER
        );
    }
}
