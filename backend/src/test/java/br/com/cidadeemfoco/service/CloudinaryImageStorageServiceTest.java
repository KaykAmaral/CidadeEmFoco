package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloudinaryImageStorageServiceTest {

    @Mock
    private CloudinaryClient cloudinaryClient;

    private CloudinaryImageStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new CloudinaryImageStorageService(
                cloudinaryClient,
                "cidade-em-foco",
                "cidade-em-foco/occurrences"
        );
    }

    @Test
    void shouldUploadValidatedImageAndLoadFromSecureCloudinaryUrl() throws Exception {
        when(cloudinaryClient.upload(any(byte[].class), anyString(), eq("cidade-em-foco/occurrences")))
                .thenReturn(new CloudinaryClient.UploadResult(
                        "cidade-em-foco/occurrences/abc123",
                        "jpg",
                        "https://res.cloudinary.com/cidade-em-foco/image/upload/v1/cidade-em-foco/occurrences/abc123.jpg"
                ));
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01}
        );

        StoredImage stored = storageService.store(file);
        StoredImage loaded = storageService.load(stored.storageKey());

        assertThat(stored.storageKey()).startsWith("cloudinary|");
        assertThat(loaded.filename()).isEqualTo("abc123.jpg");
        assertThat(loaded.mediaType().toString()).isEqualTo("image/jpeg");
        assertThat(loaded.resource().getURI().toString()).startsWith("https://res.cloudinary.com/");
    }

    @Test
    void shouldRejectCloudinaryReferenceFromAnotherHost() {
        String invalid = "cloudinary|folder/abc|jpg|https://example.com/abc.jpg";

        assertThatThrownBy(() -> storageService.load(invalid))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("URL de imagem do Cloudinary invalida");
    }

    @Test
    void shouldDeleteCloudinaryAssetUsingPublicId() {
        String reference = "cloudinary|cidade-em-foco/occurrences/abc123|jpg|"
                + "https://res.cloudinary.com/cidade-em-foco/image/upload/v1/abc123.jpg";

        storageService.deleteQuietly(reference);

        verify(cloudinaryClient).delete("cidade-em-foco/occurrences/abc123");
    }
}
