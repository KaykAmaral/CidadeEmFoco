package br.com.cidadeemfoco.entity;

import br.com.cidadeemfoco.enums.AlertSeverity;
import br.com.cidadeemfoco.enums.ClimateAlertType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClimateAlertTest {

    @Test
    void shouldStartInactiveAndAllowActivation() {
        ClimateAlert alert = new ClimateAlert(
                "Alerta de chuva",
                ClimateAlertType.CHUVA_INTENSA,
                AlertSeverity.ALTA,
                "Alerta demonstrativo",
                Instant.parse("2026-08-21T12:00:00Z"),
                Instant.parse("2026-08-21T18:00:00Z")
        );

        assertThat(alert.isActive()).isFalse();

        alert.activate();
        assertThat(alert.isActive()).isTrue();

        alert.deactivate();
        assertThat(alert.isActive()).isFalse();
    }

    @Test
    void shouldRejectInvalidPeriod() {
        Instant start = Instant.parse("2026-08-21T18:00:00Z");
        Instant end = Instant.parse("2026-08-21T12:00:00Z");

        assertThatThrownBy(() -> new ClimateAlert(
                "Periodo invalido",
                ClimateAlertType.OUTRO,
                AlertSeverity.BAIXA,
                "Alerta demonstrativo",
                start,
                end
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("posterior");
    }
}
