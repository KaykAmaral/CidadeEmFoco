package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.config.AutoClosureProperties;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.*;
import br.com.cidadeemfoco.repository.OccurrenceAutoClosureRepository;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnabledIfEnvironmentVariable(named = "MAP_TEST_DB_URL", matches = ".+")
@SpringBootTest(properties = {
        "spring.datasource.url=${MAP_TEST_DB_URL}",
        "spring.datasource.username=${MAP_TEST_DB_USERNAME:root}",
        "spring.datasource.password=${MAP_TEST_DB_PASSWORD:}",
        "app.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "app.occurrences.auto-close.enabled=true",
        "app.occurrences.auto-close.validity=PT2H",
        "app.occurrences.auto-close.check-interval=PT24H"
})
@Import(OccurrenceAutoClosureIntegrationTest.FixedClock.class)
@Transactional
class OccurrenceAutoClosureIntegrationTest {
    private static final Instant NOW = Instant.parse("2026-09-28T12:00:00Z");
    @Autowired private OccurrenceRepository occurrences;
    @Autowired private OccurrenceAutoClosureRepository audit;
    @Autowired private UserRepository users;
    @Autowired private OccurrenceAutoClosureService automation;
    @Autowired private OccurrenceService manual;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @Autowired private PlatformTransactionManager transactionManager;

    @TestConfiguration
    static class FixedClock {
        @Bean @Primary
        Clock testClock() { return Clock.fixed(NOW, ZoneOffset.UTC); }
    }

    @ParameterizedTest
    @CsvSource({
            "ALAGAMENTO, REGISTRADA, 7200, true",
            "ENCHENTE, EM_ANALISE, 7201, true",
            "VENTOS_FORTES, EM_ATENDIMENTO, 10000, true",
            "CHUVA_INTENSA, REGISTRADA, 10000, true",
            "QUEDA_GRANIZO, REGISTRADA, 10000, true",
            "RESSACA_MARITIMA, REGISTRADA, 10000, true",
            "ALAGAMENTO, REGISTRADA, 7199, false",
            "ALAGAMENTO, RESOLVIDA, 10000, false",
            "ALAGAMENTO, NAO_CONFIRMADA, 10000, false",
            "QUEDA_ARVORE, REGISTRADA, 10000, false",
            "DESLIZAMENTO, REGISTRADA, 10000, false",
            "INCENDIO, REGISTRADA, 10000, false",
            "OUTRO, REGISTRADA, 10000, false",
            "BURACO_RUA, REGISTRADA, 10000, false",
            "FALTA_ILUMINACAO, EM_ATENDIMENTO, 10000, false"
    })
    void shouldCloseOnlyEligibleExpiredOccurrencesAndPreserveData(OccurrenceType type, OccurrenceStatus status,
                                                                 long ageSeconds, boolean eligible) {
        Long id = fixture(type, status, ageSeconds);
        Instant createdAt = manual.findById(id).createdAt();
        assertThat(automation.closeExpiredOccurrences()).isEqualTo(eligible ? 1 : 0);
        entityManager.clear();
        var result = manual.findById(id);
        assertThat(result.status()).isEqualTo(eligible ? OccurrenceStatus.RESOLVIDA : status);
        assertThat(result.automaticallyResolved()).isEqualTo(eligible);
        assertThat(result.description()).isEqualTo("Evento para teste");
        assertThat(result.createdAt()).isEqualTo(createdAt);
        assertThat(result.neighborhood()).isEqualTo("Boqueirão");
        assertThat(result.imageUrls()).hasSize(1);
        assertThat(audit.count()).isEqualTo(eligible ? 1 : 0);
        if (eligible) {
            assertThat(result.resolvedAt()).isEqualTo(NOW);
            var record = audit.findAll().getFirst();
            assertThat(record.getClosedAt()).isEqualTo(NOW);
            assertThat(record.getPreviousStatus()).isEqualTo(status);
            assertThat(record.getValiditySeconds()).isEqualTo(7200);
        }
        assertThat(automation.closeExpiredOccurrences()).isZero();
        entityManager.clear();
        assertThat(manual.findById(id).resolvedAt()).isEqualTo(result.resolvedAt());
        assertThat(occurrences.existsById(id)).isTrue();
    }

    @Test
    void shouldRespectConfiguredTypesAndValidity() {
        Long flood = fixture(OccurrenceType.ALAGAMENTO, OccurrenceStatus.REGISTRADA, 10000);
        Long hail = fixture(OccurrenceType.QUEDA_GRANIZO, OccurrenceStatus.REGISTRADA, 10000);
        var configured = new OccurrenceAutoClosureService(occurrences, audit,
                new AutoClosureProperties(Set.of(OccurrenceType.QUEDA_GRANIZO), Duration.ofHours(3), Duration.ofMinutes(1), 100),
                Clock.fixed(NOW, ZoneOffset.UTC));
        assertThat(configured.closeExpiredOccurrences()).isZero();
        configured = new OccurrenceAutoClosureService(occurrences, audit,
                new AutoClosureProperties(Set.of(OccurrenceType.QUEDA_GRANIZO), Duration.ofHours(1), Duration.ofMinutes(1), 100),
                Clock.fixed(NOW, ZoneOffset.UTC));
        assertThat(configured.closeExpiredOccurrences()).isEqualTo(1);
        entityManager.clear();
        assertThat(manual.findById(flood).status()).isEqualTo(OccurrenceStatus.REGISTRADA);
        assertThat(manual.findById(hail).status()).isEqualTo(OccurrenceStatus.RESOLVIDA);
    }

    @Test
    void shouldPreserveAuditAndAdministratorControlAfterReopening() {
        Long id = fixture(OccurrenceType.ALAGAMENTO, OccurrenceStatus.REGISTRADA, 10000);
        automation.closeExpiredOccurrences();
        manual.updateStatus(id, OccurrenceStatus.EM_ATENDIMENTO);
        assertThat(automation.closeExpiredOccurrences()).isZero();
        entityManager.clear();
        assertThat(manual.findById(id).resolvedAt()).isNull();
        assertThat(manual.findById(id).automaticallyResolved()).isFalse();
        assertThat(audit.count()).isEqualTo(1);
        manual.updateStatus(id, OccurrenceStatus.RESOLVIDA);
        entityManager.clear();
        assertThat(manual.findById(id).resolvedAt()).isNotNull();
        assertThat(manual.findById(id).automaticallyResolved()).isFalse();
        assertThat(audit.count()).isEqualTo(1);
    }

    @Test
    void shouldProcessBoundedBatchesWithoutRepeatingClosures() {
        fixture(OccurrenceType.ALAGAMENTO, OccurrenceStatus.REGISTRADA, 10000);
        fixture(OccurrenceType.ALAGAMENTO, OccurrenceStatus.REGISTRADA, 10000);
        var batch = new OccurrenceAutoClosureService(occurrences, audit,
                new AutoClosureProperties(Set.of(OccurrenceType.ALAGAMENTO), Duration.ofHours(1), Duration.ofMinutes(1), 1),
                Clock.fixed(NOW, ZoneOffset.UTC));
        assertThat(batch.closeExpiredOccurrences()).isEqualTo(1);
        assertThat(batch.closeExpiredOccurrences()).isEqualTo(1);
        assertThat(batch.closeExpiredOccurrences()).isZero();
        assertThat(audit.count()).isEqualTo(2);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldCloseOnlyOnceWhenTwoWorkersRunConcurrently() throws Exception {
        var transaction = new TransactionTemplate(transactionManager);
        Long id = transaction.execute(ignored -> fixture(OccurrenceType.ALAGAMENTO, OccurrenceStatus.REGISTRADA, 10000));
        var workers = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try {
            java.util.concurrent.Callable<Integer> run = () -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Workers did not start");
                return automation.closeExpiredOccurrences();
            };
            var first = workers.submit(run);
            var second = workers.submit(run);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(first.get(30, TimeUnit.SECONDS) + second.get(30, TimeUnit.SECONDS)).isEqualTo(1);
            assertThat(manual.findById(id).automaticallyResolved()).isTrue();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM occurrence_auto_closures WHERE occurrence_id = ?", Long.class, id))
                    .isEqualTo(1);
        } finally {
            start.countDown();
            workers.shutdownNow();
            workers.awaitTermination(60, TimeUnit.SECONDS);
            deleteCommittedFixture(transaction, id);
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldRollbackClosureAndAuditTogether() {
        var transaction = new TransactionTemplate(transactionManager);
        Long id = transaction.execute(ignored -> fixture(OccurrenceType.ALAGAMENTO, OccurrenceStatus.REGISTRADA, 10000));
        try {
            assertThatThrownBy(() -> transaction.executeWithoutResult(ignored -> {
                assertThat(automation.closeExpiredOccurrences()).isEqualTo(1);
                throw new IllegalStateException("Simulated transaction failure");
            })).isInstanceOf(IllegalStateException.class);
            var unchanged = manual.findById(id);
            assertThat(unchanged.status()).isEqualTo(OccurrenceStatus.REGISTRADA);
            assertThat(unchanged.resolvedAt()).isNull();
            assertThat(unchanged.automaticallyResolved()).isFalse();
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM occurrence_auto_closures WHERE occurrence_id = ?", Long.class, id))
                    .isZero();
        } finally {
            deleteCommittedFixture(transaction, id);
        }
    }

    private void deleteCommittedFixture(TransactionTemplate transaction, Long id) {
        transaction.executeWithoutResult(ignored -> {
            Long userId = jdbc.queryForObject("SELECT user_id FROM occurrences WHERE id = ?", Long.class, id);
            jdbc.update("DELETE FROM occurrence_auto_closures WHERE occurrence_id = ?", id);
            jdbc.update("DELETE FROM occurrences WHERE id = ?", id);
            jdbc.update("DELETE FROM users WHERE id = ?", userId);
        });
    }

    private Long fixture(OccurrenceType type, OccurrenceStatus status, long ageSeconds) {
        User citizen = users.saveAndFlush(new User("Teste", UUID.randomUUID() + "@example.com", "hash", UserRole.CITIZEN));
        var category = OccurrenceCategory.EVENTO_NATURAL.allows(type)
                ? OccurrenceCategory.EVENTO_NATURAL : OccurrenceCategory.INFRAESTRUTURA_URBANA;
        Occurrence occurrence = occurrences.saveAndFlush(new Occurrence(category, type, "Evento para teste",
                PerceivedRisk.BAIXO, new BigDecimal("-24.005"), new BigDecimal("-46.402"), "Boqueirão", null, citizen));
        occurrence.addImage("preserved-test-image.jpg");
        occurrences.flush();
        jdbc.update("UPDATE occurrences SET status = ?, created_at = ?, resolved_at = ? WHERE id = ?", status.name(),
                Timestamp.from(NOW.minusSeconds(ageSeconds)),
                status == OccurrenceStatus.RESOLVIDA ? Timestamp.from(NOW.minusSeconds(100)) : null, occurrence.getId());
        entityManager.clear();
        return occurrence.getId();
    }
}
