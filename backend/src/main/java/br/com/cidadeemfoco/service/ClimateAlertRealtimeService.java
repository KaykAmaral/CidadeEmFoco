package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.event.ClimateAlertsChangedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ClimateAlertRealtimeService {

    private static final String UPDATE_EVENT = "alerts-updated";
    private static final String UPDATE_DATA = "refresh";
    private static final long RECONNECT_DELAY_MILLIS = 3_000;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final long connectionTimeoutMillis;

    public ClimateAlertRealtimeService(
            @Value("${app.alerts.realtime-connection-timeout:30m}") Duration connectionTimeout
    ) {
        if (connectionTimeout.isZero() || connectionTimeout.isNegative()) {
            throw new IllegalArgumentException("O tempo da conexao de alertas deve ser positivo");
        }
        this.connectionTimeoutMillis = connectionTimeout.toMillis();
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(connectionTimeoutMillis);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));
        sendUpdate(emitter);
        return emitter;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAlertsChanged(ClimateAlertsChangedEvent ignored) {
        notifySubscribers();
    }

    @Scheduled(fixedDelayString = "${app.alerts.realtime-refresh-interval:30s}")
    public void notifySubscribers() {
        emitters.forEach(this::sendUpdate);
    }

    int activeConnections() {
        return emitters.size();
    }

    private void sendUpdate(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .id(Long.toString(Instant.now().toEpochMilli()))
                    .name(UPDATE_EVENT)
                    .reconnectTime(RECONNECT_DELAY_MILLIS)
                    .data(UPDATE_DATA));
        } catch (IOException | IllegalStateException exception) {
            emitters.remove(emitter);
            emitter.complete();
        }
    }
}
