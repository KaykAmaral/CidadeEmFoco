package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.OccurrenceResponse;
import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.entity.Occurrence;
import br.com.cidadeemfoco.entity.City;
import br.com.cidadeemfoco.entity.User;
import br.com.cidadeemfoco.enums.OccurrenceCategory;
import br.com.cidadeemfoco.enums.OccurrenceType;
import br.com.cidadeemfoco.enums.PerceivedRisk;
import br.com.cidadeemfoco.enums.UserRole;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.repository.OccurrenceRepository;
import br.com.cidadeemfoco.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
    private ImageStorageService imageStorageService;

    @Mock
    private UserRepository userRepository;


    private OccurrenceImageService occurrenceImageService;

    @BeforeEach
    void setUp() {
        occurrenceImageService = new OccurrenceImageService(occurrenceRepository, imageStorageService, userRepository);
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
        when(userRepository.findByEmailIgnoreCase("ANA@example.com")).thenReturn(Optional.of(occurrence.getUser()));
        when(occurrenceRepository.findByIdAndCityId(10L, 1L)).thenReturn(Optional.of(occurrence));
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
        User another = user("Outra", "outra-pessoa@example.com");
        when(userRepository.findByEmailIgnoreCase("outra-pessoa@example.com")).thenReturn(Optional.of(another));
        when(occurrenceRepository.findByIdAndCityId(10L, 1L)).thenReturn(Optional.of(occurrence));

        assertThatThrownBy(() -> occurrenceImageService.upload(
                "outra-pessoa@example.com",
                10L,
                new MockMultipartFile("file", new byte[]{1})
        )).isInstanceOf(AccessDeniedException.class);

        verify(imageStorageService, never()).store(any());
    }

    @Test
    void shouldReturnNotFoundWhenOccurrenceHasNoImage() {
        Occurrence occurrence = occurrence();
        when(userRepository.findByEmailIgnoreCase("ana@example.com")).thenReturn(Optional.of(occurrence.getUser()));
        when(occurrenceRepository.findByIdAndCityId(10L, 1L)).thenReturn(Optional.of(occurrence));

        assertThatThrownBy(() -> occurrenceImageService.load("ana@example.com", 10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("A ocorrencia nao possui imagem");
    }

    private Occurrence occurrence() {
        User citizen = user("Ana", "ana@example.com");
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

    private User user(String name, String email) {
        City city = new City("Praia Grande", "SP", "3541000");
        ReflectionTestUtils.setField(city, "id", 1L);
        return new User(name, email, "hash", UserRole.CITIZEN, city);
    }
}
