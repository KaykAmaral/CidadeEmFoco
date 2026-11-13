package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.entity.CitizenXpEvent;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.CitizenXpEventType;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CitizenXpEventRepositoryTest {

    @Autowired
    private CitizenXpEventRepository xpEventRepository;
    @Autowired
    private OccurrenceRepository occurrenceRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CityRepository cityRepository;

    @Test
    void shouldSumXpAndRankOnlyCitizensFromRequestedCity() {
        City praiaGrande = cityRepository.save(new City("Praia Grande", "SP", "3541000"));
        User ana = userRepository.save(new User(
                "Ana", "ana-ranking@example.com", "hash", UserRole.CITIZEN, praiaGrande
        ));
        Occurrence report = occurrenceRepository.saveAndFlush(occurrence(ana));
        xpEventRepository.saveAndFlush(new CitizenXpEvent(
                ana, report, CitizenXpEventType.REPORT_CREATED, 10
        ));
        xpEventRepository.saveAndFlush(new CitizenXpEvent(
                ana, report, CitizenXpEventType.CASE_RESOLVED, 20
        ));

        assertThat(xpEventRepository.totalXpByUserId(ana.getId())).isEqualTo(30);
        var ranking = xpEventRepository.rankByCityAndPeriod(
                praiaGrande.getId(),
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(60)
        );
        assertThat(ranking).hasSize(1);
        assertThat(ranking.getFirst().getName()).isEqualTo("Ana");
        assertThat(ranking.getFirst().getTotalXp()).isEqualTo(30);
    }

    private Occurrence occurrence(User user) {
        return new Occurrence(
                OccurrenceCategory.EVENTO_NATURAL,
                OccurrenceType.ALAGAMENTO,
                "Via alagada",
                PerceivedRisk.ALTO,
                new BigDecimal("-24.005000"),
                new BigDecimal("-46.402000"),
                "Boqueirao",
                null,
                user
        );
    }
}
