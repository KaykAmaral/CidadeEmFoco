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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
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
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(occurrenceRepository.save(any(Occurrence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OccurrenceResponse response = occurrenceService.create(1L, validRequest());

        assertThat(response.category()).isEqualTo(OccurrenceCategory.EVENTO_NATURAL);
        assertThat(response.type()).isEqualTo(OccurrenceType.ALAGAMENTO);
        assertThat(response.status()).isEqualTo(OccurrenceStatus.REGISTRADA);
        verify(occurrenceRepository).save(any(Occurrence.class));
    }

    @Test
    void shouldRejectOccurrenceCreatedByAdmin() {
        User admin = new User("Admin", "admin@example.com", "hash", UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> occurrenceService.create(1L, validRequest()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Somente cidadaos podem registrar ocorrencias");
        verify(occurrenceRepository, never()).save(any());
    }

    @Test
    void shouldRejectTypeFromAnotherCategory() {
        User citizen = new User("Ana", "ana@example.com", "hash", UserRole.CITIZEN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
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

        assertThatThrownBy(() -> occurrenceService.create(1L, request))
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
                "boqueirao"
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
        when(userRepository.existsById(1L)).thenReturn(true);
        when(occurrenceRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of());

        List<OccurrenceResponse> responses = occurrenceService.findByUser(1L);

        assertThat(responses).isEmpty();
        verify(occurrenceRepository).findAll(any(Specification.class), any(Sort.class));
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
}
