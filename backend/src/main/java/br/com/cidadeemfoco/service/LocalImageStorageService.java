package br.com.cidadeemfoco.service;

import br.com.cidadeemfoco.dto.StoredImage;
import br.com.cidadeemfoco.exception.BusinessRuleException;
import br.com.cidadeemfoco.exception.ResourceNotFoundException;
import br.com.cidadeemfoco.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalImageStorageService implements ImageStorageService {

    private final Path storageDirectory;

    public LocalImageStorageService(
            @Value("${app.storage.occurrence-images-directory}") String storageDirectory
    ) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public StoredImage store(MultipartFile file) {
        ImageFileValidator.ValidatedImage image = ImageFileValidator.validate(file);

        try {
            Files.createDirectories(storageDirectory);

            String filename = UUID.randomUUID() + image.extension();
            Path target = safePath(filename);
            Files.write(target, image.content(), StandardOpenOption.CREATE_NEW);
            return new StoredImage(filename, image.mediaType(), new FileSystemResource(target));
        } catch (IOException exception) {
            throw new StorageException("Nao foi possivel armazenar a imagem", exception);
        }
    }

    @Override
    public StoredImage load(String filename) {
        Path imagePath = safePath(filename);
        if (!Files.isRegularFile(imagePath) || !Files.isReadable(imagePath)) {
            throw new ResourceNotFoundException("Imagem da ocorrencia nao encontrada");
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        return new StoredImage(filename, ImageFileValidator.mediaTypeFromFormat(extension), new FileSystemResource(imagePath));
    }

    @Override
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

}
