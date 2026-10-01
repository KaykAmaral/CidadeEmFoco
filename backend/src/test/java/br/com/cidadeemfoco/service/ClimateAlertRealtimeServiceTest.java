package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.event.ClimateAlertsChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClimateAlertRealtimeServiceTest {

    @Test
    void shouldRegisterRealtimeConnectionAndKeepItAfterNotification() {
        ClimateAlertRealtimeService service = new ClimateAlertRealtimeService(Duration.ofMinutes(30));

        SseEmitter emitter = service.subscribe();
        service.handleAlertsChanged(new ClimateAlertsChangedEvent());

        assertThat(emitter).isNotNull();
        assertThat(service.activeConnections()).isEqualTo(1);
        emitter.complete();
    }

    @Test
    void shouldRejectInvalidConnectionTimeout() {
        assertThatThrownBy(() -> new ClimateAlertRealtimeService(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positivo");
    }
}
