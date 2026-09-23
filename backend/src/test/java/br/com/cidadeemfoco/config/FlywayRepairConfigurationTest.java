package br.com.cidadeemfoco.config;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class FlywayRepairConfigurationTest {

    @Test
    void shouldRepairBeforeMigrating() {
        Flyway flyway = mock(Flyway.class);
        FlywayMigrationStrategy strategy = new FlywayRepairConfiguration().repairThenMigrateStrategy();

        strategy.migrate(flyway);

        InOrder orderedCalls = inOrder(flyway);
        orderedCalls.verify(flyway).repair();
        orderedCalls.verify(flyway).migrate();
        orderedCalls.verifyNoMoreInteractions();
    }
}
