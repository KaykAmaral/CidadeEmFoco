package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OccurrenceAutoResolutionServiceTest {

    @Mock
    private OccurrenceRepository occurrenceRepository;

    @Test
    void shouldResolveExpiredTemporaryNaturalOccurrences() {
        Occurrence occurrence = occurrence();
        List<Occurrence> expiredOccurrences = List.of(occurrence);
        when(occurrenceRepository.findByCategoryAndTypeInAndStatusInAndCreatedAtLessThanEqual(
                eq(OccurrenceCategory.EVENTO_NATURAL),
                anyCollection(),
                anyCollection(),
                any(Instant.class)
        )).thenReturn(expiredOccurrences);
        var service = new OccurrenceAutoResolutionService(
                occurrenceRepository,
                Duration.ofHours(6)
        );
        Instant earliestCutoff = Instant.now().minus(Duration.ofHours(6));

        int resolvedCount = service.resolveExpiredTemporaryOccurrences();

        Instant latestCutoff = Instant.now().minus(Duration.ofHours(6));
        ArgumentCaptor<Collection<OccurrenceType>> typesCaptor = collectionCaptor();
        ArgumentCaptor<Collection<OccurrenceStatus>> statusesCaptor = collectionCaptor();
        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(occurrenceRepository).findByCategoryAndTypeInAndStatusInAndCreatedAtLessThanEqual(
                eq(OccurrenceCategory.EVENTO_NATURAL),
                typesCaptor.capture(),
                statusesCaptor.capture(),
                cutoffCaptor.capture()
        );
        verify(occurrenceRepository).saveAll(expiredOccurrences);

        assertThat(resolvedCount).isEqualTo(1);
        assertThat(occurrence.getStatus()).isEqualTo(OccurrenceStatus.RESOLVIDA);
        assertThat(occurrence.getResolvedAt()).isNotNull();
        assertThat(typesCaptor.getValue()).containsExactlyInAnyOrder(
                OccurrenceType.ALAGAMENTO,
                OccurrenceType.ENCHENTE,
                OccurrenceType.VENTOS_FORTES,
                OccurrenceType.RESSACA_MARITIMA,
                OccurrenceType.CHUVA_INTENSA
        );
        assertThat(statusesCaptor.getValue()).containsExactlyInAnyOrder(
                OccurrenceStatus.REGISTRADA,
                OccurrenceStatus.EM_ANALISE,
                OccurrenceStatus.EM_ATENDIMENTO
        );
        assertThat(cutoffCaptor.getValue()).isBetween(earliestCutoff, latestCutoff);
    }

    @Test
    void shouldNotSaveWhenThereAreNoExpiredOccurrences() {
        when(occurrenceRepository.findByCategoryAndTypeInAndStatusInAndCreatedAtLessThanEqual(
                eq(OccurrenceCategory.EVENTO_NATURAL),
                anyCollection(),
                anyCollection(),
                any(Instant.class)
        )).thenReturn(List.of());
        var service = new OccurrenceAutoResolutionService(
                occurrenceRepository,
                Duration.ofHours(6)
        );

        int resolvedCount = service.resolveExpiredTemporaryOccurrences();

        assertThat(resolvedCount).isZero();
        verify(occurrenceRepository, never()).saveAll(anyCollection());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> ArgumentCaptor<Collection<T>> collectionCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(Collection.class);
    }

    private Occurrence occurrence() {
        User citizen = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        return new Occurrence(
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
    }
}
