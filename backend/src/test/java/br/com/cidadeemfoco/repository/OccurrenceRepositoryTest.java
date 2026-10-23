package br.com.cidadeemfoco.repository;

import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OccurrenceRepositoryTest {

    @Autowired
    private OccurrenceRepository occurrenceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CityRepository cityRepository;

    @Test
    void shouldKeepActiveOccurrenceVisibleOnMap() {
        Occurrence active = saveOccurrence("active@example.com");

        List<Occurrence> visible = occurrenceRepository.findVisibleOnMap(
                active.getCity().getId(),
                OccurrenceStatus.RESOLVIDA,
                Instant.now().minus(Duration.ofHours(24))
        );

        assertThat(visible).extracting(Occurrence::getId).contains(active.getId());
    }

    @Test
    void shouldKeepRecentlyResolvedOccurrenceVisibleOnMap() {
        Occurrence recentlyResolved = saveOccurrence("recent@example.com");
        recentlyResolved.changeStatus(OccurrenceStatus.RESOLVIDA);
        occurrenceRepository.saveAndFlush(recentlyResolved);

        List<Occurrence> visible = occurrenceRepository.findVisibleOnMap(
                recentlyResolved.getCity().getId(),
                OccurrenceStatus.RESOLVIDA,
                Instant.now().minus(Duration.ofHours(24))
        );

        assertThat(visible).extracting(Occurrence::getId).contains(recentlyResolved.getId());
    }

    @Test
    void shouldHideResolvedOccurrenceOlderThanMapVisibilityPeriod() {
        Occurrence expired = saveOccurrence("expired@example.com");
        expired.changeStatus(OccurrenceStatus.RESOLVIDA);
        ReflectionTestUtils.setField(expired, "resolvedAt", Instant.now().minus(Duration.ofHours(25)));
        occurrenceRepository.saveAndFlush(expired);

        List<Occurrence> visible = occurrenceRepository.findVisibleOnMap(
                expired.getCity().getId(),
                OccurrenceStatus.RESOLVIDA,
                Instant.now().minus(Duration.ofHours(24))
        );

        assertThat(visible).extracting(Occurrence::getId).doesNotContain(expired.getId());
        assertThat(occurrenceRepository.findById(expired.getId())).isPresent();
    }

    @Test
    void shouldReturnOnlyOccurrencesFromRequestedCityOnMap() {
        Occurrence praiaGrande = saveOccurrence(
                "praia-grande@example.com",
                "Praia Grande",
                "3541000"
        );
        Occurrence santos = saveOccurrence(
                "santos@example.com",
                "Santos",
                "3548500"
        );

        List<Occurrence> visible = occurrenceRepository.findVisibleOnMap(
                praiaGrande.getCity().getId(),
                OccurrenceStatus.RESOLVIDA,
                Instant.now().minus(Duration.ofHours(24))
        );

        assertThat(visible).extracting(Occurrence::getId)
                .contains(praiaGrande.getId())
                .doesNotContain(santos.getId());
    }

    private Occurrence saveOccurrence(String email) {
        return saveOccurrence(email, "Praia Grande", "SP", "3541000");
    }

    private Occurrence saveOccurrence(String email, String cityName, String ibgeCode) {
        return saveOccurrence(email, cityName, "SP", ibgeCode);
    }

    private Occurrence saveOccurrence(String email, String cityName, String stateCode, String ibgeCode) {
        City city = cityRepository.save(new City(cityName, stateCode, ibgeCode));
        User citizen = userRepository.save(new User("Cidadao", email, "hash", UserRole.CITIZEN, city));
        Occurrence occurrence = new Occurrence(
                OccurrenceCategory.EVENTO_NATURAL,
                OccurrenceType.ALAGAMENTO,
                "Via alagada",
                PerceivedRisk.ALTO,
                new BigDecimal("-24.005000"),
                new BigDecimal("-46.402000"),
                "Boqueirao",
                null,
                citizen
        );
        return occurrenceRepository.saveAndFlush(occurrence);
    }
}
