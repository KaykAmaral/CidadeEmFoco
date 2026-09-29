package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.*;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@EnabledIfEnvironmentVariable(named = "MAP_TEST_DB_URL", matches = ".+")
@SpringBootTest(properties = {
        "spring.datasource.url=${MAP_TEST_DB_URL}",
        "spring.datasource.username=${MAP_TEST_DB_USERNAME:root}",
        "spring.datasource.password=${MAP_TEST_DB_PASSWORD:}",
        "app.security.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
})
@AutoConfigureMockMvc
@Import(OccurrenceMapIntegrationTest.FixedMapClock.class)
@Transactional
class OccurrenceMapIntegrationTest {
    @Autowired private OccurrenceRepository occurrences;
    @Autowired private UserRepository users;
    @Autowired private OccurrenceService service;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @Autowired private MockMvc mvc;

    @TestConfiguration
    static class FixedMapClock {
        @Bean
        @Primary
        MapVisibilityPolicy fixedMapVisibilityPolicy() {
            return new MapVisibilityPolicy(Duration.ofHours(2), Duration.ofSeconds(5),
                    Clock.fixed(Instant.parse("2026-09-28T12:00:00Z"), ZoneOffset.UTC));
        }
    }

    @ParameterizedTest
    @CsvSource(nullValues = "NULL", value = {
            "REGISTRADA, NULL, true",
            "EM_ANALISE, NULL, true",
            "EM_ATENDIMENTO, NULL, true",
            "NAO_CONFIRMADA, NULL, true",
            "RESOLVIDA, 2026-09-28T11:59:00Z, true",
            "RESOLVIDA, 2026-09-28T10:00:00.000001Z, true",
            "RESOLVIDA, 2026-09-28T10:00:00Z, false",
            "RESOLVIDA, 2026-09-27T12:00:00Z, false",
            "RESOLVIDA, NULL, false"
    })
    void shouldApplyVisibilityInDatabaseWithoutRemovingHistory(OccurrenceStatus status, String resolvedAt, boolean visible) {
        Long id = saveOccurrence(status, resolvedAt);
        var filter = new OccurrenceFilter(null, null, null, null);
        var map = service.findForMap(filter, id);

        assertThat(map.occurrences()).hasSize(visible ? 1 : 0);
        assertThat(map.refreshAfterMillis()).isEqualTo(5000);
        assertThat(service.findById(id).status()).isEqualTo(status);
        assertThat(service.findAll(filter)).extracting(value -> value.id()).contains(id);
        assertThat(occurrences.existsById(id)).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldExposeAuthenticatedMapWithFiltersAndNoCache() throws Exception {
        Long id = saveOccurrence(OccurrenceStatus.REGISTRADA, null);
        mvc.perform(get("/api/occurrences/map")
                        .param("occurrenceId", id.toString())
                        .param("category", "EVENTO_NATURAL")
                        .param("type", "ALAGAMENTO")
                        .param("neighborhood", "boque")
                        .param("status", "REGISTRADA")
                        .param("createdFrom", "2020-01-01T00:00:00Z")
                        .param("createdBefore", "2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.occurrences[0].id").value(id))
                .andExpect(jsonPath("$.refreshAfterMillis").value(5000));
        mvc.perform(get("/api/occurrences/map").param("neighborhood", "Bairro inexistente"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.occurrences").isEmpty());
        mvc.perform(get("/api/occurrences/map").param("createdBefore", "2020-01-01T00:00:00Z"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.occurrences").isEmpty());
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mvc.perform(get("/api/occurrences/map")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldShowAnExpiredOccurrenceAgainWhenReopened() {
        Long id = saveOccurrence(OccurrenceStatus.RESOLVIDA, "2026-09-27T12:00:00Z");
        service.updateStatus(id, OccurrenceStatus.EM_ATENDIMENTO);
        entityManager.clear();
        assertThat(service.findById(id).resolvedAt()).isNull();
        assertThat(service.findForMap(new OccurrenceFilter(null, null, null, null), id).occurrences()).hasSize(1);
    }

    private Long saveOccurrence(OccurrenceStatus status, String resolvedAt) {
        User citizen = users.saveAndFlush(new User("Teste de mapa", "map-test@example.com", "test-hash", UserRole.CITIZEN));
        Occurrence occurrence = occurrences.saveAndFlush(new Occurrence(
                OccurrenceCategory.EVENTO_NATURAL, OccurrenceType.ALAGAMENTO, "Registro para teste de integração",
                PerceivedRisk.BAIXO, new BigDecimal("-24.005"), new BigDecimal("-46.402"), "Boqueirão", null, citizen));
        jdbc.update("UPDATE occurrences SET status = ?, resolved_at = ? WHERE id = ?", status.name(),
                resolvedAt == null ? null : Timestamp.from(Instant.parse(resolvedAt)), occurrence.getId());
        entityManager.clear();
        return occurrence.getId();
    }
}
