package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OccurrenceImageServiceTest {

    @Mock
    private OccurrenceRepository occurrenceRepository;

    @Mock
    private LocalImageStorageService imageStorageService;


    private OccurrenceImageService occurrenceImageService;

    @BeforeEach
    void setUp() {
        occurrenceImageService = new OccurrenceImageService(occurrenceRepository, imageStorageService);
    }

    @Test
    void shouldUploadImageOnlyForOccurrenceOwner() {
        Occurrence occurrence = occurrence();
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[]{1});
        StoredImage storedImage = new StoredImage(
                "nova-imagem.jpg",
                MediaType.IMAGE_JPEG,
                new ByteArrayResource(new byte[]{1})
        );
        when(occurrenceRepository.findById(10L)).thenReturn(Optional.of(occurrence));
        when(imageStorageService.store(file)).thenReturn(storedImage);
        when(occurrenceRepository.saveAndFlush(occurrence)).thenReturn(occurrence);

        OccurrenceResponse response = occurrenceImageService.upload("ANA@example.com", 10L, file);

        assertThat(response.status()).isNotNull();
        assertThat(occurrence.getImages()).hasSize(1);
        assertThat(occurrence.getImages().getFirst().getImagePath()).isEqualTo("nova-imagem.jpg");
    }

    @Test
    void shouldRejectUploadFromAnotherCitizen() {
        Occurrence occurrence = occurrence();
        when(occurrenceRepository.findById(10L)).thenReturn(Optional.of(occurrence));

        assertThatThrownBy(() -> occurrenceImageService.upload(
                "outra-pessoa@example.com",
                10L,
                new MockMultipartFile("file", new byte[]{1})
        )).isInstanceOf(AccessDeniedException.class);

        verify(imageStorageService, never()).store(any());
    }

    @Test
    void shouldReturnNotFoundWhenOccurrenceHasNoImage() {
        when(occurrenceRepository.findById(10L)).thenReturn(Optional.of(occurrence()));

        assertThatThrownBy(() -> occurrenceImageService.load(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("A ocorrencia nao possui imagem");
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
