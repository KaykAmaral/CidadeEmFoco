package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
public class LocalImageStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final Path storageDirectory;

    public LocalImageStorageService(
            @Value("${app.storage.occurrence-images-directory}") String storageDirectory
    ) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public StoredImage store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("A imagem e obrigatoria");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessRuleException("A imagem deve ter no maximo 5 MB");
        }

        try {
            byte[] content = file.getBytes();
            ImageFormat format = identifyFormat(content);
            Files.createDirectories(storageDirectory);

            String filename = UUID.randomUUID() + format.extension;
            Path target = safePath(filename);
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
            return new StoredImage(filename, format.mediaType, new FileSystemResource(target));
        } catch (IOException exception) {
            throw new StorageException("Nao foi possivel armazenar a imagem", exception);
        }
    }

    public StoredImage load(String filename) {
        Path imagePath = safePath(filename);
        if (!Files.isRegularFile(imagePath) || !Files.isReadable(imagePath)) {
            throw new ResourceNotFoundException("Imagem da ocorrencia nao encontrada");
        }

        ImageFormat format = ImageFormat.fromFilename(filename);
        return new StoredImage(filename, format.mediaType, new FileSystemResource(imagePath));
    }

    public void deleteQuietly(String filename) {
        if (filename == null) {
            return;
        }
        try {
            Files.deleteIfExists(safePath(filename));
        } catch (IOException ignored) {
            // Uma falha de limpeza nao deve desfazer o cadastro da nova imagem.
        }
    }

    private Path safePath(String filename) {
        Path resolved = storageDirectory.resolve(filename).normalize();
        if (!resolved.startsWith(storageDirectory)) {
            throw new BusinessRuleException("Nome de imagem invalido");
        }
        return resolved;
    }

    private ImageFormat identifyFormat(byte[] content) {
        if (content.length >= 3
                && unsigned(content[0]) == 0xFF
                && unsigned(content[1]) == 0xD8
                && unsigned(content[2]) == 0xFF) {
            return ImageFormat.JPEG;
        }
        if (content.length >= 8
                && unsigned(content[0]) == 0x89
                && content[1] == 0x50
                && content[2] == 0x4E
                && content[3] == 0x47
                && content[4] == 0x0D
                && content[5] == 0x0A
                && content[6] == 0x1A
                && content[7] == 0x0A) {
            return ImageFormat.PNG;
        }
        if (content.length >= 12
                && content[0] == 'R'
                && content[1] == 'I'
                && content[2] == 'F'
                && content[3] == 'F'
                && content[8] == 'W'
                && content[9] == 'E'
                && content[10] == 'B'
                && content[11] == 'P') {
            return ImageFormat.WEBP;
        }
        throw new BusinessRuleException("Formato de imagem invalido. Use JPEG, PNG ou WebP");
    }

    private int unsigned(byte value) {
        return Byte.toUnsignedInt(value);
    }

    private enum ImageFormat {
        JPEG(".jpg", MediaType.IMAGE_JPEG),
        PNG(".png", MediaType.IMAGE_PNG),
        WEBP(".webp", MediaType.parseMediaType("image/webp"));

        private final String extension;
        private final MediaType mediaType;

        ImageFormat(String extension, MediaType mediaType) {
            this.extension = extension;
            this.mediaType = mediaType;
        }

        private static ImageFormat fromFilename(String filename) {
            String normalized = filename.toLowerCase(java.util.Locale.ROOT);
            for (ImageFormat format : values()) {
                if (normalized.endsWith(format.extension)) {
                    return format;
                }
            }
            throw new ResourceNotFoundException("Formato da imagem armazenada nao reconhecido");
        }
    }
}
