package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.AdminOccurrenceMetricsResponse;
import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private OccurrenceRepository occurrenceRepository;

    @Mock
    private UserRepository userRepository;

    private AdminDashboardService dashboardService;
    private City city;
    private User admin;

    @BeforeEach
    void setUp() {
        city = new City("Praia Grande", "SP", "3541000");
        ReflectionTestUtils.setField(city, "id", 1L);
        admin = new User("Admin", "admin@example.com", "hash", UserRole.ADMIN, city);
        dashboardService = new AdminDashboardService(
                occurrenceRepository,
                userRepository,
                "America/Sao_Paulo"
        );
    }

    @Test
    void shouldCalculateOccurrenceMetricsForAdminsCity() {
        Instant firstDay = Instant.parse("2026-10-01T12:00:00Z");
        Instant secondDay = Instant.parse("2026-10-02T12:00:00Z");
        Occurrence manuallyResolved = occurrence(OccurrenceType.ALAGAMENTO, firstDay, 3);
        resolve(manuallyResolved, firstDay.plusSeconds(120 * 60), false);
        Occurrence automaticallyResolved = occurrence(OccurrenceType.VENTOS_FORTES, firstDay, 1);
        resolve(automaticallyResolved, firstDay.plusSeconds(60 * 60), true);
        Occurrence open = occurrence(OccurrenceType.ALAGAMENTO, secondDay, 2);
        Occurrence notConfirmed = occurrence(OccurrenceType.ALAGAMENTO, secondDay, 1);
        notConfirmed.changeStatus(OccurrenceStatus.NAO_CONFIRMADA);

        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(occurrenceRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(manuallyResolved, automaticallyResolved, open, notConfirmed));

        AdminOccurrenceMetricsResponse response = dashboardService.occurrenceMetrics(
                "ADMIN@EXAMPLE.COM",
                new OccurrenceFilter(null, null, null, null, null, null)
        );

        assertThat(response.city().ibgeCode()).isEqualTo("3541000");
        assertThat(response.totalCases()).isEqualTo(4);
        assertThat(response.totalReports()).isEqualTo(7);
        assertThat(response.openCases()).isEqualTo(1);
        assertThat(response.resolvedCases()).isEqualTo(2);
        assertThat(response.notConfirmedCases()).isEqualTo(1);
        assertThat(response.automaticallyResolvedCases()).isEqualTo(1);
        assertThat(response.resolutionRatePercent()).isEqualByComparingTo("66.67");
        assertThat(response.averageResolutionTimeMinutes()).isEqualTo(90);
        assertThat(response.casesByStatus()).containsEntry(OccurrenceStatus.RESOLVIDA, 2L);
        assertThat(response.casesByType()).containsEntry(OccurrenceType.ALAGAMENTO, 3L);
        assertThat(response.casesByNeighborhood().getFirst().name()).isEqualTo("Boqueirao");
        assertThat(response.dailyEvolution()).hasSize(2);
        assertThat(response.dailyEvolution().getFirst().registeredCases()).isEqualTo(2);
    }

    @Test
    void shouldReturnZeroRateAndNullAverageWithoutConfirmedOrResolvedCases() {
        Occurrence notConfirmed = occurrence(
                OccurrenceType.ALAGAMENTO,
                Instant.parse("2026-10-01T12:00:00Z"),
                1
        );
        notConfirmed.changeStatus(OccurrenceStatus.NAO_CONFIRMADA);
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(occurrenceRepository.findAll(any(Specification.class))).thenReturn(List.of(notConfirmed));

        AdminOccurrenceMetricsResponse response = dashboardService.occurrenceMetrics(
                "admin@example.com",
                new OccurrenceFilter(null, null, null, null, null, null)
        );

        assertThat(response.resolutionRatePercent()).isEqualByComparingTo("0.00");
        assertThat(response.averageResolutionTimeMinutes()).isNull();
    }

    @Test
    void shouldRejectInvertedPeriodBeforeQueryingOccurrences() {
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        OccurrenceFilter filter = new OccurrenceFilter(
                null,
                null,
                null,
                null,
                Instant.parse("2026-10-10T00:00:00Z"),
                Instant.parse("2026-10-01T00:00:00Z")
        );

        assertThatThrownBy(() -> dashboardService.occurrenceMetrics("admin@example.com", filter))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("O inicio do periodo deve ser anterior ao fim");
        verify(occurrenceRepository, never()).findAll(any(Specification.class));
    }

    private Occurrence occurrence(OccurrenceType type, Instant createdAt, int strength) {
        User citizen = new User("Cidadao", "citizen" + type + strength + "@example.com", "hash", UserRole.CITIZEN, city);
        Occurrence occurrence = new Occurrence(
                OccurrenceCategory.EVENTO_NATURAL,
                type,
                "Evento observado",
                PerceivedRisk.MEDIO,
                new BigDecimal("-24.005000"),
                new BigDecimal("-46.402000"),
                "Boqueirao",
                null,
                citizen
        );
        ReflectionTestUtils.setField(occurrence, "createdAt", createdAt);
        ReflectionTestUtils.setField(occurrence, "updatedAt", createdAt);
        ReflectionTestUtils.setField(occurrence, "strength", strength);
        return occurrence;
    }

    private void resolve(Occurrence occurrence, Instant resolvedAt, boolean automatically) {
        if (automatically) {
            occurrence.resolveAutomatically();
        } else {
            occurrence.changeStatus(OccurrenceStatus.RESOLVIDA);
        }
        ReflectionTestUtils.setField(occurrence, "resolvedAt", resolvedAt);
    }
}
