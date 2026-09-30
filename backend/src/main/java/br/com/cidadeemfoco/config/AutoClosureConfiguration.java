package br.com.cidadeemfoco.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.time.Clock;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(AutoClosureProperties.class)
@ConditionalOnProperty(name = "app.occurrences.auto-close.enabled", havingValue = "true")
public class AutoClosureConfiguration {
    @Bean
    Clock autoClosureClock() {
        return Clock.systemUTC();
    }
}
