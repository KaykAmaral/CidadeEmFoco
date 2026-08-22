package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalImageStorageServiceTest {

    @TempDir
    private Path temporaryDirectory;

    @Test
    void shouldStoreAndLoadJpegUsingGeneratedFilename() throws Exception {
        LocalImageStorageService storageService = new LocalImageStorageService(temporaryDirectory.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nome-controlado-pelo-usuario.exe",
                "application/octet-stream",
                new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01}
        );

        StoredImage stored = storageService.store(file);
        StoredImage loaded = storageService.load(stored.filename());

        assertThat(stored.filename()).endsWith(".jpg").doesNotContain("nome-controlado");
        assertThat(loaded.mediaType().toString()).isEqualTo("image/jpeg");
        assertThat(loaded.resource().getContentAsByteArray()).containsExactly(file.getBytes());
    }

    @Test
    void shouldRejectContentThatIsNotSupportedImage() {
        LocalImageStorageService storageService = new LocalImageStorageService(temporaryDirectory.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "arquivo.txt",
                "text/plain",
                "nao e uma imagem".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> storageService.store(file))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("JPEG, PNG ou WebP");
    }

    @Test
    void shouldRejectPathTraversal() {
        LocalImageStorageService storageService = new LocalImageStorageService(temporaryDirectory.toString());

        assertThatThrownBy(() -> storageService.load("../arquivo.jpg"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Nome de imagem invalido");
    }
}
