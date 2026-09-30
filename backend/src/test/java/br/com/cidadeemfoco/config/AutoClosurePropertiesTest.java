package br.com.cidadeemfoco.config;

import br.com.cidadeemfoco.enums.OccurrenceType;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.Set;
import static org.assertj.core.api.Assertions.*;

class AutoClosurePropertiesTest {
    @Test
    void shouldRejectEveryNonTemporaryTypeEvenIfExplicitlyConfigured() {
        for (var type : OccurrenceType.values()) {
            if (!type.isTemporary()) {
                assertThatThrownBy(() -> new AutoClosureProperties(Set.of(type), Duration.ofHours(1), Duration.ofMinutes(1), 100))
                        .isInstanceOf(IllegalArgumentException.class);
            }
        }
    }

    @Test
    void shouldRejectInvalidDurationsAndBatchSize() {
        assertThatThrownBy(() -> new AutoClosureProperties(Set.of(), Duration.ZERO, Duration.ofMinutes(1), 100))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AutoClosureProperties(Set.of(), Duration.ofHours(1), Duration.ZERO, 100))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AutoClosureProperties(Set.of(), Duration.ofHours(1), Duration.ofMinutes(1), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
