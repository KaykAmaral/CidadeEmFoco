package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.entity.CitizenXpEvent;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.CitizenRankingPeriod;
import br.com.cidadeemfoco.enums.CitizenXpEventType;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.repository.CitizenXpEventRepository;
import br.com.cidadeemfoco.repository.CitizenXpRankingProjection;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    @Mock
    private CitizenXpEventRepository xpEventRepository;
    @Mock
    private OccurrenceRepository occurrenceRepository;
    @Mock
    private UserRepository userRepository;

    private GamificationService service;
    private City city;
    private User citizen;

    @BeforeEach
    void setUp() {
        service = new GamificationService(
                xpEventRepository,
                occurrenceRepository,
                userRepository,
                10,
                20,
                100,
                ZONE_ID,
                Clock.fixed(Instant.parse("2026-11-13T15:00:00Z"), ZONE_ID)
        );
        city = new City("Praia Grande", "SP", "3541000");
        ReflectionTestUtils.setField(city, "id", 1L);
        citizen = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN, city);
        ReflectionTestUtils.setField(citizen, "id", 10L);
    }

    @Test
    void shouldAwardReportXpOnlyOnce() {
        Occurrence report = occurrence(100L, citizen);
        when(xpEventRepository.existsByUserIdAndOccurrenceIdAndEventType(
                10L, 100L, CitizenXpEventType.REPORT_CREATED
        )).thenReturn(false, true);

        service.awardReportCreated(report);
        service.awardReportCreated(report);

        ArgumentCaptor<CitizenXpEvent> captor = ArgumentCaptor.forClass(CitizenXpEvent.class);
        verify(xpEventRepository).save(captor.capture());
        assertThat(captor.getValue().getPoints()).isEqualTo(10);
        assertThat(captor.getValue().getEventType()).isEqualTo(CitizenXpEventType.REPORT_CREATED);
    }

    @Test
    void shouldAwardResolutionBonusToEveryCaseContributor() {
        Occurrence root = occurrence(100L, citizen);
        User bia = new User("Bia", "bia@example.com", "hash", UserRole.CITIZEN, city);
        ReflectionTestUtils.setField(bia, "id", 11L);
        Occurrence contribution = occurrence(101L, bia);
        when(occurrenceRepository.findByGroupRootIdAndCityIdOrderByCreatedAtAsc(100L, 1L))
                .thenReturn(List.of(contribution));

        service.awardCaseResolved(root);

        ArgumentCaptor<CitizenXpEvent> captor = ArgumentCaptor.forClass(CitizenXpEvent.class);
        verify(xpEventRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(event -> {
            assertThat(event.getEventType()).isEqualTo(CitizenXpEventType.CASE_RESOLVED);
            assertThat(event.getPoints()).isEqualTo(20);
        });
    }

    @Test
    void shouldReturnCitizenLevelProgress() {
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(xpEventRepository.totalXpByUserId(10L)).thenReturn(235L);

        var response = service.findMine("ANA@EXAMPLE.COM");

        assertThat(response.totalXp()).isEqualTo(235);
        assertThat(response.level()).isEqualTo(3);
        assertThat(response.xpInCurrentLevel()).isEqualTo(35);
        assertThat(response.xpForNextLevel()).isEqualTo(65);
    }

    @Test
    void shouldReturnMonthlyRankingOnlyForRequesterCity() {
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(xpEventRepository.rankByCityAndPeriod(
                1L,
                Instant.parse("2026-11-01T03:00:00Z"),
                Instant.parse("2026-12-01T03:00:00Z")
        )).thenReturn(List.of(ranking(11L, "Bia", 80L), ranking(10L, "Ana", 50L)));
        when(xpEventRepository.totalXpByUserId(11L)).thenReturn(180L);
        when(xpEventRepository.totalXpByUserId(10L)).thenReturn(250L);

        var response = service.findCitizenRanking("ana@example.com", CitizenRankingPeriod.MONTH);

        assertThat(response.city().ibgeCode()).isEqualTo("3541000");
        assertThat(response.ranking()).extracting(item -> item.name())
                .containsExactly("Bia", "Ana");
        assertThat(response.ranking().getFirst().xpEarnedInPeriod()).isEqualTo(80);
        assertThat(response.ranking().getFirst().level()).isEqualTo(2);
        verify(xpEventRepository, never()).findAll();
    }

    private Occurrence occurrence(Long id, User user) {
        Occurrence occurrence = new Occurrence(
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
        ReflectionTestUtils.setField(occurrence, "id", id);
        return occurrence;
    }

    private CitizenXpRankingProjection ranking(Long userId, String name, Long totalXp) {
        return new CitizenXpRankingProjection() {
            public Long getUserId() { return userId; }
            public String getName() { return name; }
            public Long getTotalXp() { return totalXp; }
        };
    }
}
