package br.com.cidadeemfoco.exception;

import java.time.Instant;

public class OccurrenceCooldownException extends BusinessRuleException {

    private final long retryAfterSeconds;
    private final Instant nextAllowedAt;

    public OccurrenceCooldownException(
            String message,
            long retryAfterSeconds,
            Instant nextAllowedAt
    ) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.nextAllowedAt = nextAllowedAt;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public Instant getNextAllowedAt() {
        return nextAllowedAt;
    }
}
