package br.com.cidadeemfoco.config;

import br.com.cidadeemfoco.service.MapVisibilityPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
public class MapConfiguration {
    @Bean
    MapVisibilityPolicy mapVisibilityPolicy(
            @Value("${app.map.resolved-retention}") String retention,
            @Value("${app.map.refresh-interval}") String refreshInterval) {
        return new MapVisibilityPolicy(Duration.parse(retention), Duration.parse(refreshInterval), Clock.systemUTC());
    }
}
