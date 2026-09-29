package br.com.cidadeemfoco.service;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Duration;
import static org.assertj.core.api.Assertions.*;

class MapVisibilityPolicyTest {
    @Test
    void shouldAllowImmediateRemovalAndRejectInvalidConfiguration() {
        assertThat(new MapVisibilityPolicy(Duration.ZERO, Duration.ofSeconds(1), Clock.systemUTC())
                .refreshAfterMillis()).isEqualTo(1000);
        assertThatThrownBy(() -> new MapVisibilityPolicy(Duration.ofSeconds(-1), Duration.ofSeconds(30), Clock.systemUTC()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MapVisibilityPolicy(Duration.ofHours(24), Duration.ZERO, Clock.systemUTC()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
