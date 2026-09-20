package br.com.cidadeemfoco.event;

import java.util.List;

public record ClimateAlertUnavailableEvent(List<Long> alertIds) {

    public ClimateAlertUnavailableEvent(Long alertId) {
        this(List.of(alertId));
    }
}
