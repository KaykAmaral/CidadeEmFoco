package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.CreateOccurrenceRequest;
import br.com.cidadeemfoco.dto.OccurrenceFilter;
import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceStatus;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OccurrenceServiceTest {

    @Mock
    private OccurrenceRepository occurrenceRepository;

    @Mock
    private UserRepository userRepository;

    private OccurrenceService occurrenceService;

    @BeforeEach
    void setUp() {
        occurrenceService = new OccurrenceService(occurrenceRepository, userRepository);
    }

    @Test
    void shouldCreateRegisteredOccurrenceForCitizen() {
        User citizen = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        when(occurrenceRepository.save(any(Occurrence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OccurrenceResponse response = occurrenceService.create("ana@example.com", validRequest());

        assertThat(response.category()).isEqualTo(OccurrenceCategory.EVENTO_NATURAL);
        assertThat(response.type()).isEqualTo(OccurrenceType.ALAGAMENTO);
        assertThat(response.status()).isEqualTo(OccurrenceStatus.REGISTRADA);
        verify(occurrenceRepository).save(any(Occurrence.class));
    }

    @Test
    void shouldRejectOccurrenceCreatedByAdmin() {
        User admin = new User("Admin", "admin@example.com", "hash", UserRole.ADMIN);
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> occurrenceService.create("admin@example.com", validRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Somente cidadaos podem registrar ocorrencias");
        verify(occurrenceRepository, never()).save(any());
    }

    @Test
    void shouldRejectTypeFromAnotherCategory() {
        User citizen = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(citizen));
        CreateOccurrenceRequest request = new CreateOccurrenceRequest(
                OccurrenceCategory.EVENTO_NATURAL,
                OccurrenceType.BURACO_RUA,
                "Buraco grande",
                PerceivedRisk.MEDIO,
                new BigDecimal("-24.005000"),
                new BigDecimal("-46.402000"),
                "Boqueirao",
                null
        );

        assertThatThrownBy(() -> occurrenceService.create("ana@example.com", request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("O tipo informado nao pertence a categoria selecionada");
        verify(occurrenceRepository, never()).save(any());
    }

    @Test
    void shouldReturnNotFoundWhenOccurrenceDoesNotExist() {
        when(occurrenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> occurrenceService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ocorrencia nao encontrada");
    }

    @Test
    void shouldUpdateOccurrenceStatus() {
        Occurrence occurrence = occurrence();
        when(occurrenceRepository.findById(10L)).thenReturn(Optional.of(occurrence));
        when(occurrenceRepository.saveAndFlush(occurrence)).thenReturn(occurrence);

        OccurrenceResponse response = occurrenceService.updateStatus(10L, OccurrenceStatus.EM_ATENDIMENTO);

        assertThat(response.status()).isEqualTo(OccurrenceStatus.EM_ATENDIMENTO);
        verify(occurrenceRepository).saveAndFlush(occurrence);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingOccurrence() {
        when(occurrenceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> occurrenceService.updateStatus(99L, OccurrenceStatus.RESOLVIDA))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Ocorrencia nao encontrada");
        verify(occurrenceRepository, never()).saveAndFlush(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldListOccurrencesUsingFiltersAndNewestFirst() {
        User citizen = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        Occurrence occurrence = new Occurrence(
                OccurrenceCategory.INFRAESTRUTURA_URBANA,
                OccurrenceType.BURACO_RUA,
                "Buraco grande",
                PerceivedRisk.MEDIO,
                new BigDecimal("-24.005000"),
                new BigDecimal("-46.402000"),
                "Boqueirao",
                null,
                citizen
        );
        when(occurrenceRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(occurrence));
        OccurrenceFilter filter = new OccurrenceFilter(
                OccurrenceCategory.INFRAESTRUTURA_URBANA,
                OccurrenceType.BURACO_RUA,
                OccurrenceStatus.REGISTRADA,
                "boqueirao",
                Instant.parse("2026-09-01T00:00:00Z"),
                Instant.parse("2026-09-18T23:59:59Z")
        );

        List<OccurrenceResponse> responses = occurrenceService.findAll(filter);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().type()).isEqualTo(OccurrenceType.BURACO_RUA);
        verify(occurrenceRepository).findAll(
                any(Specification.class),
                eq(Sort.by(Sort.Direction.DESC, "createdAt"))
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldListOnlyOccurrencesFromRequestedUser() {
        when(occurrenceRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of());

        List<OccurrenceResponse> responses = occurrenceService.findByUser("ANA@EXAMPLE.COM");

        assertThat(responses).isEmpty();
        verify(occurrenceRepository).findAll(any(Specification.class), any(Sort.class));
    }

    @Test
    void shouldRejectInvertedFilterPeriod() {
        OccurrenceFilter filter = new OccurrenceFilter(
                null,
                null,
                null,
                null,
                Instant.parse("2026-09-18T23:59:59Z"),
                Instant.parse("2026-09-01T00:00:00Z")
        );

        assertThatThrownBy(() -> occurrenceService.findAll(filter))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("O inicio do periodo deve ser anterior ao fim");
        verify(occurrenceRepository, never()).findAll(
                any(Specification.class),
                any(Sort.class)
        );
    }

    @Test
    void shouldListOnlyOccurrencesVisibleOnMapForTheConfiguredPeriod() {
        when(occurrenceRepository.findVisibleOnMap(eq(OccurrenceStatus.RESOLVIDA), any(Instant.class)))
                .thenReturn(List.of());
        Instant earliestExpectedCutoff = Instant.now().minus(Duration.ofHours(24));

        List<OccurrenceResponse> responses = occurrenceService.findVisibleOnMap();

        Instant latestExpectedCutoff = Instant.now().minus(Duration.ofHours(24));
        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(occurrenceRepository).findVisibleOnMap(
                eq(OccurrenceStatus.RESOLVIDA),
                cutoffCaptor.capture()
        );
        assertThat(responses).isEmpty();
        assertThat(cutoffCaptor.getValue())
                .isBetween(earliestExpectedCutoff, latestExpectedCutoff);
    }

    private CreateOccurrenceRequest validRequest() {
        return new CreateOccurrenceRequest(
                OccurrenceCategory.EVENTO_NATURAL,
                OccurrenceType.ALAGAMENTO,
                "Via alagada",
                PerceivedRisk.ALTO,
                new BigDecimal("-24.005000"),
                new BigDecimal("-46.402000"),
                "Boqueirao",
                "Avenida Presidente Costa e Silva"
        );
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
