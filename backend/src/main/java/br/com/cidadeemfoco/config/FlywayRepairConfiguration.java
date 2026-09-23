package br.com.cidadeemfoco.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class FlywayRepairConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayRepairConfiguration.class);

    @Bean
    @ConditionalOnProperty(name = "app.database.flyway-repair-on-start", havingValue = "true")
    FlywayMigrationStrategy repairThenMigrateStrategy() {
        return flyway -> {
            LOGGER.warn("FLYWAY_REPAIR_ON_START esta ativo: reparando o historico antes das migrations");
            flyway.repair();
            flyway.migrate();
        };
    }
}
